package com.xmu.wowoto.wowomall.service;

import com.xmu.wowoto.wowomall.domain.CartItem;
import com.xmu.wowoto.wowomall.domain.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 15:14
 */
@Service
@FeignClient("discountService")
public interface RemoteDiscountService {

    /**
     * 更新一项order
     * @param order
     * @return
     */
    @PostMapping(value = "/discount/orders")
    String caculatePrice(Order order);

}
