package com.xmu.wowoto.wowomall.controller.vo;

import com.xmu.wowoto.wowomall.domain.Address;

import java.util.List;


/**
 *
 * @author wowoto
 * @date 12/21/2019
 */
public class SubmitOrderVo {
    public Integer getRebate() {
        return rebate;
    }

    public void setRebate(Integer rebate) {
        this.rebate = rebate;
    }

    /**
     *  用户提交的返点数
     */
    private Integer rebate;
    /**
     *  用户在购物车中选中项目的id
     */
    private List<Integer> cartItemIds;
    /**
     * 配送的地址
     */
    private Address address;
    /**
     * 优惠卷id
     */
    private Integer couponId;
    /**
     * 订单留言
     */
    private String message = "";

    @Override
    public String toString() {
        return "SubmitOrderVo{" +
                "cartItemIds=" + cartItemIds +
                ", wowoAddress=" + address +
                ", couponId=" + couponId +
                ", message='" + message + '\'' +
                '}';
    }

    public List<Integer> getCartItemIds() {
        return cartItemIds;
    }

    public void setCartItemIds(List<Integer> cartItemIds) {
        this.cartItemIds = cartItemIds;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
