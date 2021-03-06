package com.xmu.wowoto.wowomall.service;

import com.xmu.wowoto.wowomall.domain.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/16 19:50
 */
@Service
@FeignClient("freightService")
public interface RemoteFreightService {


    /**
     * 更新一项order
     * @param order
     * @return
     */
    @PostMapping("/freightPrice")
    String caculateFreight(Order order);
}
