package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.commons.tool.exception.UpdateException;
import com.mall.commons.tool.utils.NumberUtils;
import com.mall.order.biz.callback.SendEmailCallback;
import com.mall.order.biz.callback.TransCallback;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dto.CartProductDto;
import com.mall.order.utils.GlobalIdGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *  ciggar
 * create-date: 2019/8/1-下午5:01
 * 初始化订单 生成订单
 */

@Slf4j
@Component
public class InitOrderHandler extends AbstractTransHandler {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;


    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @Transactional
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;

        //构建订单
        Order order = new Order();
        String orderId = UUID.randomUUID().toString();
        order.setOrderId(orderId);
        order.setUserId(createOrderContext.getUserId());
        order.setBuyerNick(createOrderContext.getBuyerNickName());
        order.setPayment(createOrderContext.getOrderTotal());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setBuyerNick(createOrderContext.getBuyerNickName());
        order.setStatus(OrderConstants.ORDER_STATUS_INIT);
        //将新构建的订单插入订单表
        try {
            orderMapper.insert(order);
        } catch (Exception e) {
            throw new UpdateException("插入失败");
        }

        //构建订单商品关联信息
//        List<Long> buyProductIdList = new ArrayList<>();
        List<CartProductDto> cartProductDtoList = createOrderContext.getCartProductDtoList();
        for (CartProductDto cartProductDto : cartProductDtoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setItemId(cartProductDto.getProductId());
            orderItem.setTitle(cartProductDto.getProductName());
            orderItem.setPrice(cartProductDto.getSalePrice().doubleValue());
            orderItem.setStatus(1);
            String substring = orderId.substring(0, orderId.length() - 6);
            String orderItemId = substring + String.valueOf((Math.random()*9+1)*100000);
            orderItem.setId(orderItemId);
            orderItem.setNum(cartProductDto.getProductNum().intValue());
            orderItem.setPicPath(cartProductDto.getProductImg());
            BigDecimal totalFee = cartProductDto.getSalePrice().multiply(new BigDecimal(cartProductDto.getProductNum()));
            orderItem.setTotalFee(totalFee.doubleValue());

//            buyProductIdList.add(cartProductDto.getProductId());
            try {
                orderItemMapper.insert(orderItem);
            } catch (Exception e) {
                throw new UpdateException("插入失败");
            }
        }
        createOrderContext.setOrderId(orderId);
        return true;
    }
}
