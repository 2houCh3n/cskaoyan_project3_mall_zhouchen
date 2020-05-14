package com.cskaoyan.gateway.controller.shopping;

import com.alibaba.fastjson.JSON;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;
import com.mall.order.dto.*;
import com.mall.shopping.dto.CartProductDto;
import com.mall.shopping.dto.ProductDto;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User：zhouchen
 * Time: 2020/5/13  8:55
 * Description:
 */
@RestController
public class OrderController {

    @Reference
    private OrderCoreService orderCoreService;

    @PostMapping("shopping/order")
    public ResponseData createOrder(@RequestBody CreateOrderRequest createOrderRequest, HttpServletRequest request) {
        //获取用户id
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(userInfo);
        Long userId = Long.valueOf((Integer)userMap.get("uid"));
        createOrderRequest.setUserId(userId);

        CreateOrderResponse createOrderResponse = orderCoreService.createOrder(createOrderRequest);
        if (!createOrderResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            //说明创建订单失败
            return new ResponseUtil().setErrorMsg(createOrderResponse.getMsg());
        }
        return new ResponseUtil().setData(createOrderResponse.getOrderId());
    }

    /**
     * 查询用户的所有订单
     * @param page
     * @param size
     * @param sort
     * @param request
     * @return
     */
    @GetMapping("shopping/order")
    public ResponseData getAllOrders(@RequestParam Integer page, @RequestParam Integer size, @RequestParam String sort, HttpServletRequest request) {
        //对传入进来的参数进行校验
        if (page == null || size == null || sort == null || sort.isEmpty()) {
            //校验失败
            return new ResponseUtil().setErrorMsg(SysRetCodeConstants.REQUEST_DATA_ERROR.getMessage());
        }
        //获取用户信息，用Map封装
        String  userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(userInfo);

        OrderListRequest orderListRequest = new OrderListRequest();
        orderListRequest.setPage(page);
        orderListRequest.setSize(size);
        orderListRequest.setSort(sort);
        orderListRequest.setUserId(Long.valueOf((Integer)userMap.get("uid")));
        OrderListResponse orderListResponse = orderCoreService.getAllOrders(orderListRequest);

        Map<String, Object> resMap = new HashMap<>();
        resMap.put("data", orderListResponse.getDetailInfoList());
        return new ResponseUtil().setData(resMap);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("shopping/order/{id}")
    public ResponseData getOrderDetail(@PathVariable("id") String orderId, HttpServletRequest request) {
        //获取用户id
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(userInfo);
        Long userId = Long.valueOf((Integer)userMap.get("uid"));
        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setUserId(userId.intValue());

        orderDetailRequest.setOrderId(orderId);
        OrderDetailResponse orderDetailResponse = orderCoreService.getOrderDetail(orderDetailRequest);
        return new ResponseUtil().setData(orderDetailResponse);
    }

    /**
     * 取消订单
     */
    @PutMapping("shopping/order/{id}")
    public ResponseData cancelOrder(@PathVariable("id") String orderId) {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        CancelOrderResponse cancelOrderResponse = orderCoreService.cancelOrder(cancelOrderRequest);
        return new ResponseUtil().setData(cancelOrderResponse.getMsg());
    }

    /**
     * 删除订单
     */
    @DeleteMapping("shopping/order/{id}")
    public ResponseData deleteOrder(@PathVariable("id") String orderId) {
        DeleteOrderRequest deleteOrderRequest = new DeleteOrderRequest();
        deleteOrderRequest.setOrderId(orderId);
        DeleteOrderResponse deleteOrderResponse = orderCoreService.deleteOrder(deleteOrderRequest);
        if (deleteOrderResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil().setData(deleteOrderResponse.getMsg());
        }
        return new ResponseUtil().setErrorMsg(deleteOrderResponse.getMsg());
    }
}
