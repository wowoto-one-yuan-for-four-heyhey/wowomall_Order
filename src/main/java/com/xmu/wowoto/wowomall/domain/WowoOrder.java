package com.xmu.wowoto.wowomall.domain;

import com.xmu.wowoto.wowomall.entity.Order;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Alias("WowoOrder")
public class WowoOrder extends com.xmu.wowoto.wowomall.entity.Order {

    private WowoUser wowoUser;

    private WowoCoupon wowoCoupon;

    private List<WowoOrderItem> wowoOrderItems;

    private WowoAddress wowoAddress;

    /**
     * 计算订单的成交价格
     */
    public void cacuDealPrice(){
        //目前设计只支持一个订单中同类优惠卷只能使用一张优惠卷，一个货品只能选择使用一张优惠卷
        WowoCoupon wowoCoupon = this.getWowoCoupon();
        if (wowoCoupon != null) {
            wowoCoupon.cacuCouponPrice(this);
        }
    }

    public WowoUser getWowoUser() {
        return wowoUser;
    }

    public void setWowoUser(WowoUser wowoUser) {
        this.wowoUser = wowoUser;
    }

    public List<WowoOrderItem> getWowoOrderItems() {
        return wowoOrderItems;
    }

    public void setWowoOrderItems(List<WowoOrderItem> wowoOrderItems) {
        this.wowoOrderItems = wowoOrderItems;
    }

    public WowoAddress getWowoAddress() {
        return wowoAddress;
    }

    public void setWowoAddress(WowoAddress wowoAddress) {
        this.wowoAddress = wowoAddress;
    }

    public WowoCoupon getWowoCoupon() {
        return wowoCoupon;
    }

    public void setWowoCoupon(WowoCoupon wowoCoupon) {
        this.wowoCoupon = wowoCoupon;
    }
}