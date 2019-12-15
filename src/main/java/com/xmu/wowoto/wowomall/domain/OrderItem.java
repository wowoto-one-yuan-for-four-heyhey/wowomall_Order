package com.xmu.wowoto.wowomall.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: 数据库与对象模型标准组
 * @Description:订单明细对象
 * @Data:Created in 14:50 2019/12/11
 **/
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends OrderItemPo {
    private Product product;

    public OrderItem(CartItem cartItem){
        this.setNumber(cartItem.getNumber());
        Product product = cartItem.getProduct();
        this.setProduct(product);
        this.setProductId(product.getId());
        this.setPrice(product.getPrice());
        this.setDealPrice(this.getPrice());
    }
}
