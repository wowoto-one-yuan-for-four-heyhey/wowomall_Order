package com.xmu.wowoto.wowomall.service.impl;

import com.xmu.wowoto.wowomall.domain.Payment;
import com.xmu.wowoto.wowomall.service.PaymentService;
import com.xmu.wowoto.wowomall.service.RemotePaymentService;
import com.xmu.wowoto.wowomall.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 20:05
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    RemotePaymentService remotePaymentService;

    @Override
    public Payment createPayment(Payment payment) {
        String json = remotePaymentService.createPayment(payment);
        return JacksonUtil.parseObject(json, "data", Payment.class);
    }

    @Override
    public Payment payPayment(Integer id) {
        String json = remotePaymentService.payPayment(id);
        return JacksonUtil.parseObject(json, "data", Payment.class);
    }

    /**
     * 根据OrderID拿到payment
     * @param orderId
     * @return
     */
    @Override
    public List<Payment> getPaymentByOrderId(Integer orderId)
    {
        String json= remotePaymentService.getPaymentsById(orderId);
        List<LinkedHashMap> pays = JacksonUtil.parseObjectList(json,"data",LinkedHashMap.class);
        List<Payment> payments = new LinkedList<>();
        for(LinkedHashMap pay : pays) {
            payments.add(JacksonUtil.parseObject(JacksonUtil.toJson(pay),Payment.class));
        }

        return payments;
    }
}
