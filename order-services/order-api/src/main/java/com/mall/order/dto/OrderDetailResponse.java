package com.mall.order.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *  ciggar
 * create-date: 2019/7/31-上午9:40
 */
@Data
public class OrderDetailResponse extends AbstractResponse{

    private String orderId;

    private BigDecimal payment;

    private Integer paymentType;

    private BigDecimal postFee;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Date paymentTime;

    private Date consignTime;

    private Date endTime;

    private Date closeTime;

    private String shippingName;

    private String shippingCode;

    private Long userId;

    private String buyerMessage;

    private String buyerNick;

    private Integer buyerComment;

    private List<OrderItemDto> orderItemDto;

    private OrderShippingDto orderShippingDto;

    /*****************************以下是我自己增加的属性*****************************/

    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 订单总金额
     */
    private BigDecimal orderTotal;
    /**
     * 该订单包含的商品信息
     */
    private List<OrderItemDto> goodsList;
    /**
     * 电话号码
     */
    private String tel;
    /**
     * 地址信息
     */
    private String streetName;
}
