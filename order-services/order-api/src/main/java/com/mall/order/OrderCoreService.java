package com.mall.order;

import com.mall.order.dto.*;

/**
 *  ciggar
 * create-date: 2019/7/30-上午9:13
 * 订单相关业务
 */
public interface OrderCoreService {

    /**
     * 创建订单
     * @param request
     * @return
     */
    CreateOrderResponse createOrder(CreateOrderRequest request);

    OrderListResponse getAllOrders(OrderListRequest request);

    OrderDetailResponse getOrderDetail(OrderDetailRequest orderDetailRequest);

    CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest);

    DeleteOrderResponse deleteOrder(DeleteOrderRequest deleteOrderRequest);
}
