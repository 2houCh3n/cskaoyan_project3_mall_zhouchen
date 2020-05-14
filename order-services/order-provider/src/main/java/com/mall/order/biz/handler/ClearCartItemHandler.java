package com.mall.order.biz.handler;

import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.dto.CartProductDto;
import com.mall.shopping.ICartService;
import com.mall.shopping.dto.DeleteCheckedItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  ciggar
 * create-date: 2019/8/1-下午5:05
 * 将购物车中的缓存失效
 */
@Slf4j
@Component
public class ClearCartItemHandler extends AbstractTransHandler {

    @Autowired
    RedissonClient redissonClient;

    @Reference
    ICartService cartService;


    //是否采用异步方式执行
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;
//        List<CartProductDto> cartProductDtoList = createOrderContext.getCartProductDtoList();
//
//        //辅助map，加快匹配速度
//        Map<Long, CartProductDto> helpMap = new HashMap<>();
//        for (CartProductDto cartProductDto : cartProductDtoList) {
//            helpMap.put(cartProductDto.getProductId(), cartProductDto);
//        }
//
//        //获取用户信息
//        String userName = createOrderContext.getUserName();
//
//        //将所有的商品全部从购物车中删除
//        RMap<Object, Object> cart = redissonClient.getMap("cart");
//        List<com.mall.shopping.dto.CartProductDto> cartProductDtos = (List<com.mall.shopping.dto.CartProductDto>) cart.get(userName);
//        for (com.mall.shopping.dto.CartProductDto cartProductDto : cartProductDtos) {
//            if (helpMap.get(cartProductDto.getProductId()) != null) {
//                cartProductDtos.remove(cartProductDto);
//            }
//        }
//        cart.put(userName, cartProductDtos);
        DeleteCheckedItemRequest deleteCheckedItemRequest = new DeleteCheckedItemRequest();
        deleteCheckedItemRequest.setUserId(createOrderContext.getUserId());
        cartService.deleteCheckedItem(deleteCheckedItemRequest);
        return true;
    }
}
