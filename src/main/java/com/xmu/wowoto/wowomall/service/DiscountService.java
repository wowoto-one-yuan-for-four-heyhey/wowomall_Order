package com.xmu.wowoto.wowomall.service;

import com.xmu.wowoto.wowomall.domain.Coupon;
import org.springframework.stereotype.Service;

/**
 *
 * @author wowoto
 * @date 12/08/2019
 */
@Service
public interface DiscountService {

    /**
     * 获取指定ID的优惠券
     *
     * @param couponId   优惠券ID
     */
    Coupon findCouponById(Integer couponId);
}