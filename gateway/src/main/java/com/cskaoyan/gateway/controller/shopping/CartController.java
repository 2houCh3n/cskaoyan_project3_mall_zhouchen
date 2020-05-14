package com.cskaoyan.gateway.controller.shopping;

import com.alibaba.fastjson.JSON;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.ICartService;
import com.mall.shopping.dto.*;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * User：zhouchen
 * Time: 2020/5/13  14:35
 * Description:
 */
@RestController
public class CartController {

    @Reference
    ICartService cartService;

    /**
     * 获取当前用户的所有购物车信息
     * @param request
     * @return
     */
    @GetMapping("shopping/carts")
    public ResponseData getCarts(HttpServletRequest request) {
        //获取用户信息
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(userInfo);

        //通过用户信息获取购物车信息
        CartListByIdRequest cartListByIdRequest = new CartListByIdRequest();
        cartListByIdRequest.setUserId(Long.valueOf((Integer)userMap.get("uid")));
        CartListByIdResponse cartListByIdResponse = cartService.getCartListById(cartListByIdRequest);
        return new ResponseUtil().setData(cartListByIdResponse.getCartProductDtos());
    }

    /**
     * 添加商品到购物车
     * @param data
     * @return
     */
    @PostMapping("shopping/carts")
    public ResponseData addCart(@RequestBody Map data) {
        Long userId = Long.valueOf((String)data.get("userId"));
        Long productId = Long.valueOf((Integer)data.get("productId"));
        Integer productNum = Integer.valueOf((Integer)data.get("productNum"));

        //构建参数
        AddCartRequest addCartRequest = new AddCartRequest();
        addCartRequest.setItemId(productId);
        addCartRequest.setUserId(userId);
        addCartRequest.setNum(productNum);

        AddCartResponse addCartResponse = cartService.addToCart(addCartRequest);
        return new ResponseUtil().setData(addCartResponse.getMsg());
    }

    /**
     * 更新购物车中的商品
     */
    @PutMapping("shopping/carts")
    public ResponseData updateCart(@RequestBody Map data) {
        Long userId = Long.valueOf((String)data.get("userId"));
        Long productId = Long.valueOf((Integer)data.get("productId"));
        Integer productNum = (Integer) data.get("productNum");
        String checked = (String) data.get("checked");

        //构建参数
        UpdateCartNumRequest updateCartNumRequest = new UpdateCartNumRequest();
        updateCartNumRequest.setChecked(checked);
        updateCartNumRequest.setItemId(productId);
        updateCartNumRequest.setNum(productNum);
        updateCartNumRequest.setUserId(userId);

        UpdateCartNumResponse updateCartNumResponse = cartService.updateCartNum(updateCartNumRequest);
        return new ResponseUtil().setData(updateCartNumResponse.getMsg());
    }

    /**
     * 删除购物车中的商品
     */
    @DeleteMapping("shopping/carts/{uid}/{pid}")
    public ResponseData deleteCart(@PathVariable("uid") Long uid, @PathVariable("pid") Long pid) {
        //构建参数
        DeleteCartItemRequest deleteCartItemRequest = new DeleteCartItemRequest();
        deleteCartItemRequest.setUserId(uid);
        deleteCartItemRequest.setItemId(pid);

        DeleteCartItemResponse deleteCartItemResponse = cartService.deleteCartItem(deleteCartItemRequest);
        return new ResponseUtil().setData(deleteCartItemResponse.getMsg());
    }

    /**
     * 删除购物车中选中的商品
     */
    @DeleteMapping("shopping/items/{id}")
    public ResponseData deleteItems(@PathVariable("id") Long uid) {
        //构建参数
        DeleteCheckedItemRequest deleteCheckedItemRequest = new DeleteCheckedItemRequest();
        deleteCheckedItemRequest.setUserId(uid);

        DeleteCheckedItemResposne deleteCheckedItemResposne = cartService.deleteCheckedItem(deleteCheckedItemRequest);
        return new ResponseUtil().setData(deleteCheckedItemResposne.getMsg());
    }
}
