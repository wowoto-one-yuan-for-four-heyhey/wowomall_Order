package com.xmu.wowoto.wowomall.service.impl;

import com.xmu.wowoto.wowomall.controller.OrderController;
import com.xmu.wowoto.wowomall.dao.OrderDao;
import com.xmu.wowoto.wowomall.domain.*;

import com.xmu.wowoto.wowomall.service.*;
import com.xmu.wowoto.wowomall.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 *
 * @author wowoto
 * @date 12/08/2019
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private FreightService freightService;

    @Autowired
    private LogisticsService logisticsService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户订单列表
     *
     * @param userId   用户ID
     * @param statusCode 订单信息：
     *                 1未付款，
     *                 2未发货，
     *                 3未收货，
     *                 4未评价，
     *                 5已完成订单，
     *                 6退货订单，
     *                 7换货订单
     * @param page     分页页数
     * @param limit     分页大小
     * @return 订单列表
     */
    @Override
    public List<Order> getOrders(Integer userId, Integer statusCode, Integer page, Integer limit)
    {
        List<Order> orders = orderDao.getOrdersByStatusCode(userId, statusCode, page, limit);
        return orders;
    }

    @Override

    public Order submit(Order order, List<CartItem> cartItems){
        Order newOrder = null;
        if(this.createOrderItemFromCartItem(order, cartItems)){
            cartService.clearCartItem(cartItems);
            order.setStatusCode(Order.StatusCode.NOT_PAYED.getValue());
            order.cacuGoodsPrice();

            //计算优惠促销价格
            order = discountService.caculatePrice(order);

            logger.debug("discount done");

            //计算运费
            order.setFreightPrice(freightService.caculateFreight(order));

            logger.debug("freight done");

            //物流单号
            order.setOrderSn(logisticsService.getShipSn());

            logger.debug("logistics done");

            //添加订单
            newOrder = orderDao.addOrder(order);

            //订单最终计算
            newOrder.cacuIntegral();

            //支付最终计算
            newOrder.cacuPayment();
            
            //添加payment
            for(Payment payment: newOrder.getPaymentList()){
                paymentService.createPayment(payment);
            }

            logger.debug("payment done");
        }
        return newOrder;
    }

    /**
     * 用CartItem构造OrderItem
     * @param order 订单对象
     * @param cartItems 购物车对象列表
     */
    private boolean createOrderItemFromCartItem(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>(cartItems.size());
        for (CartItem cartItem: cartItems){
            if(goodsService.deductStock(cartItem)){
                OrderItem orderItem = new OrderItem(cartItem);
                orderItems.add(orderItem);
            }else {
                return false;
            }
        }
        order.setOrderItemList(orderItems);
        return true;
    }

    /**
     * 获取用户特定订单详情
     *
     * @param orderId 订单ID
     * @return 订单列表
     */
    @Override
    public Order getOrder(Integer orderId)
    {
        Order order = orderDao.getOrderByOrderId(orderId);
        return order;
    }


    /**
     * 更改订单状态为退款(管理员操作)
     *
     * @param orderItem 订单项
     * @return 订单列表
     */
    @Override
    public OrderItem refundOrderItem(OrderItem orderItem, Order order){
        orderItem.setStatusCode(OrderItem.StatusCode.RETURN_SUCCESS.getValue());


        Payment payment = new Payment();
        payment.setActualPrice(orderItem.getPrice().negate());
        payment.setOrderId(orderItem.getOrderId());
        payment.setPayTime(LocalDateTime.now());
        payment.setGmtCreate(LocalDateTime.now());
        payment.setBeginTime(LocalDateTime.now());
        payment.setGmtModified(LocalDateTime.now());
        payment.setBeDeleted(false);

        List<Payment> orderPay = paymentService.getPaymentByOrderId(order.getId());

        payment.setEndTime(LocalDateTime.now().plusMinutes(30));
        payment.setPayChannel( orderPay.get(0).getPayChannel());
        payment.setStatusCode(1);

        Payment newPayment = paymentService.createPayment(payment);

        BigDecimal rebatePrice = order.getRebatePrice();
        BigDecimal orderItemPrice = orderItem.getPrice();
        BigDecimal goodsPrice = order.getGoodsPrice();

        if(goodsPrice.equals(0)){
            return null;
            //异常抛错
        }
        if(rebatePrice != null) {

            BigDecimal rebate = (rebatePrice.multiply(orderItemPrice)).divide(goodsPrice, 3);
            userService.updateUserRebate(order.getUserId(),rebate.intValue());
        }

        orderDao.updateOrderItem(orderItem);

        return orderItem;
    }

    /**
     * 提供给支付模块修改订单状态->支付成功  (供paymentService调用)"
     * @param oneOrder
     * @return 是否成功
     */
    @Override
    public HashMap<String,Integer> payOrder(Order oneOrder) {

        HashMap<String,Integer> result=new HashMap<>(8);
        if (Order.StatusCode.PAYED.getValue() > oneOrder.getStatusCode()) {
            List<OrderItem> orderItems = oneOrder.getOrderItemList();
            for (OrderItem item : orderItems) {
                item.setStatusCode(2);
                Integer re = orderDao.updateOrderItem(item);
                if(re != 1) {
                    result.put("orderItem", re);
                }
            }
            oneOrder.setPayTime(LocalDateTime.now());
            oneOrder.setStatusCode(Order.StatusCode.PAYED.getValue());
            Integer status = orderDao.updateOrder(oneOrder);
            result.put("order", status);
            return result;
        }
        result.put("order", -1);
        return result;
    }


    /**
     * 取消订单
     *
     * @param order
     * @return 操作结果
     */
    @Override
    public Order cancelOrder(Order order){
        order.setStatusCode(Order.StatusCode.NOT_PAYED_CANCELED.getValue());
        order.setEndTime(LocalDateTime.now());
        order.setGmtModified(LocalDateTime.now());
        for (OrderItem orderItem: order.getOrderItemList()) {
            goodsService.restoreStock(orderItem);
        }
        orderDao.updateOrder(order);
        return order;
    }

    /**
     * 删除订单
     *
     * @param order
     * @return 操作结果
     */
    @Override
    public Order deleteOrder(Order order){
        for(OrderItem orderItem: order.getOrderItemList()){
            orderItem.setBeDeleted(true);
            orderDao.updateOrderItem(orderItem);
        }
        order.setBeDeleted(true);
        order.setEndTime(LocalDateTime.now());
        orderDao.updateOrder(order);
        return order;
    }

    /**
     * 订单确认
     *
     * @param order
     */
    @Override
    public Order confirm(Order order){
        for(OrderItem orderItem: order.getOrderItemList()){
            orderItem.setStatusCode(OrderItem.StatusCode.CONFIRMED.getValue());
            orderDao.updateOrderItem(orderItem);
        }
        order.setStatusCode(Order.StatusCode.SHIPPED_CONFIRM.getValue());
        order.setConfirmTime(LocalDateTime.now());
        orderDao.updateOrder(order);
        return order;
    }

    /**
     * 订单发货
     *
     * @param order
     * @return 操作结果
     */
    @Override
    public Order shipOrder(Order order){
        for(OrderItem orderItem: order.getOrderItemList()){
            orderItem.setStatusCode(OrderItem.StatusCode.NOT_CONFIRMED.getValue());
            orderDao.updateOrderItem(orderItem);
        }
        order.setStatusCode(Order.StatusCode.SHIPPED.getValue());
        order.setShipTime(LocalDateTime.now());
        orderDao.updateOrder(order);
        return order;
    }

    /**
     * 得到一项orderItem
     * @param orderItemId
     * @return
     */
    @Override
    public OrderItem getOrderItem(Integer orderItemId)
    {
        return orderDao.getOrderItemById(orderItemId);
    }

    /**
     * 获取团购订单数量
     * @param goodId
     * @return
     */
    @Override
    public Integer getGrouponOrdersNum(Integer goodId, LocalDateTime startTime, LocalDateTime endTime) {

        return orderDao.getGrouponOrdersNumberById(goodId, startTime, endTime);
    }

    @Override
    public List<Payment> refundGrouponOrders(Integer goodId, LocalDateTime startTime, LocalDateTime endTime, Double rate) {
        List<OrderItem> orderItems = orderDao.getGrouponOrderItems(goodId, startTime, endTime);
        List<Payment> payments = new ArrayList<>();
        for(OrderItem orderItem: orderItems){
            Order order = orderDao.getOrderByOrderId(orderItem.getOrderId());
            List<OrderItem> orderItemList = order.getOrderItemList();
            OrderItem item = orderItemList.get(0);
            BigDecimal aa = item.getDealPrice();
            Integer a = aa.intValue();
            Double dealPrice = a * rate;
            BigDecimal decimal = BigDecimal.valueOf(dealPrice);
            orderItemList.get(0).setDealPrice(decimal);
            order.setIntegralPrice(decimal);

            orderDao.updateOrder(order);
            //然后去新增一条payment
            Payment payment = new Payment();
            payment.setActualPrice(decimal.subtract(aa));
            payment.setStatusCode(0);
            payment.setPayTime(LocalDateTime.now());
            payment.setOrderId(order.getId());
            paymentService.createPayment(payment);
            payments.add(payment);
        }
        return payments;
    }

    @Override
    public List<Payment> refundPresaleOrders(Integer goodId, LocalDateTime startTime, LocalDateTime endTime) {
        List<OrderItem> orderItems = orderDao.getPresaleOrderItems(goodId, startTime, endTime);
        List<Payment> payments = new ArrayList<>();
        for (OrderItem orderItem: orderItems){
            Order order = orderDao.getOrderByOrderId(orderItem.getOrderId());
            order.setPaymentList(paymentService.getPaymentByOrderId(order.getId()));
            OrderItem item = order.getOrderItemList().get(0);

            if(order.getPaymentList().size() <= 1){
                return null;
            }

            Payment payment1 = order.getPaymentList().get(0);
            Payment payment2 = order.getPaymentList().get(1);
            
            if(payment1.getStatusCode().equals(0)){
                Payment refundPayment = new Payment();
                refundPayment.setActualPrice(BigDecimal.ZERO.subtract(payment1.getActualPrice()));
                refundPayment.setStatusCode(1);
                refundPayment.setPayTime(LocalDateTime.now());
                refundPayment.setOrderId(order.getId());
                paymentService.createPayment(refundPayment);
                payments.add(refundPayment);
            }
            if(payment2.getStatusCode().equals(0)){
                Payment refundPayment = new Payment();
                refundPayment.setActualPrice(BigDecimal.ZERO.subtract(payment2.getActualPrice()));
                refundPayment.setStatusCode(1);
                refundPayment.setPayTime(LocalDateTime.now());
                refundPayment.setOrderId(order.getId());
                paymentService.createPayment(refundPayment);
                payments.add(refundPayment);
            }
        }
        return payments;
    }

    /**
     * 更新一项order
     * @param order
     * @return
     */
    @Override
    public Integer updateOrder(Order order){
        return orderDao.updateOrder(order);
    }

    /**
     * 管理员筛选订单
     * @param userId
     * @param orderSn
     * @param orderStatusArray
     * @param page
     * @param limit
     * @return
     */
    @Override
    public List<Order> getOrdersByStatusCodesAndOrderSn(Integer userId,String orderSn,
                                                        List<Short> orderStatusArray,
                                                        Integer page, Integer limit)
    {
        List<Order> orders = orderDao.getOrdersByStatusCodesAndOrderSn(userId, orderSn, orderStatusArray, page, limit);
        return orders;
    }

    /**
     * 判断一个payment是否可支付
     * @param payment
     * @return
     */
    @Override
    public boolean judgePaymentAccessible(Payment payment){
        LocalDateTime start= payment.getBeginTime();
        LocalDateTime end=payment.getEndTime();
        if(LocalDateTime.now().isAfter(start) && LocalDateTime.now().isBefore(end)){
            if(payment.getStatusCode().equals(0)){
                return true;
            }
        }
        else{
            return false;
        }
        return false;
    }

    /**
     * 取消超时订单
     * @param order
     * @return
     */
    @Override
    public Order autoCancelOrder(Order order){
        order.setStatusCode(Order.StatusCode.NOT_PAYED_SYSTEM_CANCELED.getValue());
        order.setEndTime(LocalDateTime.now());
        order.setGmtModified(LocalDateTime.now());
        orderDao.updateOrder(order);
        return order;
    }
}


