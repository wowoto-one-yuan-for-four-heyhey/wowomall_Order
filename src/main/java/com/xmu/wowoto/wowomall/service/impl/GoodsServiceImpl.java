package com.xmu.wowoto.wowomall.service.impl;

import com.xmu.wowoto.wowomall.domain.CartItem;
import com.xmu.wowoto.wowomall.domain.Product;
import com.xmu.wowoto.wowomall.service.GoodsService;
import com.xmu.wowoto.wowomall.service.RemoteGoodsService;
import com.xmu.wowoto.wowomall.util.Config;
import com.xmu.wowoto.wowomall.util.JacksonUtil;
import com.xmu.wowoto.wowomall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 15:06
 */
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    RemoteGoodsService remoteGoodsService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    Config config;

    public Product getProductById(Integer productId){
        String json = remoteGoodsService.getProductById(productId);
        return JacksonUtil.parseObject(json, "data", Product.class);
    }

    public Product updateProductById(Integer productId, Product product){
        String json = remoteGoodsService.updateProductById(productId, product);
        return JacksonUtil.parseObject(json, "data", Product.class);
    }

    @Override
    public boolean deductStock(CartItem cartItem) {
        Integer productId = cartItem.getProductId();
        Integer number = cartItem.getNumber();

        String key = "Stock_" + productId;
        Integer stock = Integer.valueOf(redisUtil.get(key));

        if(stock != null && stock - number >= 0){
            redisUtil.set(key, String.valueOf(stock - number));
            return true;
        }else{
            if(supplyStock(productId, key)){
                stock = Integer.valueOf(redisUtil.get(key));
                if(stock - number < 0){
                    return false;
                }
                redisUtil.set(key, String.valueOf(stock - number));
                return true;
            }else{
                return false;
            }
        }

    }

    public boolean supplyStock(Integer productId, String key){
        Product product = this.getProductById(productId);
        Integer dbStock = product.getSafetyStock();
        if(dbStock == null || dbStock == 0){
            return false;
        }

        Integer redisStock = config.getPreDeductStock();
        dbStock = dbStock - redisStock;
        if(dbStock < 0){
            redisStock = redisStock + dbStock;
            dbStock = 0;
        }
        redisUtil.set(key, String.valueOf(redisStock));
        product.setSafetyStock(dbStock);
        this.updateProductById(productId, product);
        return true;
    }

}

