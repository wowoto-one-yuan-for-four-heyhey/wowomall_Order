package com.xmu.wowoto.wowomall.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 18:15
 */
@Configuration
public class Config {
    /**
     * 预提库存数量
     */
    @Value("${wowomall.preDeductStock}")
    private Integer preDeductStock;

    public Integer getPreDeductStock() {
        return preDeductStock;
    }

    public void setPreDeductStock(Integer preDeductStock) {
        this.preDeductStock = preDeductStock;
    }
}
