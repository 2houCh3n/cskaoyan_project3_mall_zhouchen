package com.mall.order.services;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.commons.tool.exception.BizException;
import com.mall.order.OrderCoreService;
import com.mall.order.biz.TransOutboundInvoker;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.factory.OrderProcessPipelineFactory;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.converter.OrderConverter;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.entitys.OrderShipping;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import com.mall.user.IMemberService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.QueryMemberRequest;
import com.mall.user.dto.QueryMemberResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  ciggar
 * create-date: 2019/7/30-上午10:05
 */
@Slf4j
@Component
@Service(cluster = "failfast")
public class OrderCoreServiceImpl implements OrderCoreService {

	@Autowired
	OrderMapper orderMapper;

	@Autowired
	OrderItemMapper orderItemMapper;

	@Autowired
	OrderShippingMapper orderShippingMapper;

	@Autowired
    OrderProcessPipelineFactory orderProcessPipelineFactory;

	@Autowired
	OrderConverter orderConverter;

	@Reference
	IMemberService iMemberService;

	@Autowired
	StockMapper stockMapper;


	/**
	 * 创建订单的处理流程
	 *
	 * @param request
	 * @return
	 */
	@Override
	public CreateOrderResponse createOrder(CreateOrderRequest request) {
		CreateOrderResponse response = new CreateOrderResponse();
		try {
			//创建pipeline对象
			TransOutboundInvoker invoker = orderProcessPipelineFactory.build(request);
			//启动pipeline
			invoker.start(); //启动流程（pipeline来处理）
			//获取处理结果
			AbsTransHandlerContext context = invoker.getContext();
			//把处理结果转换为response
			response = (CreateOrderResponse) context.getConvert().convertCtx2Respond(context);
		} catch (Exception e) {
			log.error("OrderCoreServiceImpl.createOrder Occur Exception :" + e);
			ExceptionProcessorUtils.wrapperHandlerException(response, e);
		}
		return response;
	}

	/**
	 * 根据用户id获取该用户所有的订单信息
	 * @param request
	 * @return
	 */
	@Override
	public OrderListResponse getAllOrders(OrderListRequest request) {
		//开启分页查询
		PageHelper.startPage(request.getPage(), request.getSize());
		OrderListResponse response = new OrderListResponse();
		//第一步 从数据库中获取该用户的订单
		Example example = new Example(Order.class);
		example.createCriteria().andEqualTo("userId", request.getUserId());
		List<Order> orders = orderMapper.selectByExample(example);

		List<OrderDetailInfo> orderDetailInfoList = new ArrayList<>();
		//第二步 根据每一个订单信息，从数据库中获取该订单包含的商品信息，以及该订单的物流信息
		for (Order order : orders) {
			OrderDetailInfo orderDetailInfo = orderConverter.order2detail(order);
			String orderId = order.getOrderId();
			//获取订单包含的商品信息
			Example orderItemExample = new Example(OrderItem.class);
			orderItemExample.createCriteria().andEqualTo("orderId", orderId);
			List<OrderItem> orderItems = orderItemMapper.selectByExample(orderItemExample);
			orderDetailInfo.setOrderItemDto(orderConverter.item2dto(orderItems));

			//获取订单包含的物流信息
			Example orderShippingExample = new Example(OrderShipping.class);
			orderShippingExample.createCriteria().andEqualTo("orderId", orderId);
			List<OrderShipping> orderShippings = orderShippingMapper.selectByExample(orderShippingExample);
			orderDetailInfo.setOrderShippingDto(orderConverter.shipping2dto(orderShippings.get(0)));

			orderDetailInfoList.add(orderDetailInfo);
		}
		PageInfo<OrderDetailInfo> orderDetailInfoPageInfo = new PageInfo<>(orderDetailInfoList);
		response.setDetailInfoList(orderDetailInfoPageInfo.getList());
		response.setTotal(orderDetailInfoPageInfo.getTotal());
		return response;
	}

	/**
	 * 根据订单id获取该订单的详细信息
	 * @param orderDetailRequest
	 * @return
	 */
	@Override
	public OrderDetailResponse getOrderDetail(OrderDetailRequest orderDetailRequest) {
		OrderDetailResponse response = new OrderDetailResponse();
		String orderId = orderDetailRequest.getOrderId();

		//首先根据订单Id从订单表中查出该订单的详细信息
		Order order = orderMapper.selectByPrimaryKey(orderId);

		//然后根据订单id，从订单商品表中获取与之对应的所有商品信息
		Example example = new Example(OrderItem.class);
		example.createCriteria().andEqualTo("orderId", orderId);
		List<OrderItem> orderItems = orderItemMapper.selectByExample(example);

		//根据订单id，从物流信息表中获取相应的物流信息
		Example shippingExample = new Example(OrderShipping.class);
		shippingExample.createCriteria().andEqualTo("orderId", orderId);
		List<OrderShipping> orderShippings = orderShippingMapper.selectByExample(shippingExample);

		//根据userID查询username
		QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
		queryMemberRequest.setUserId(orderDetailRequest.getUserId().longValue());
		QueryMemberResponse member = iMemberService.queryMemberById(queryMemberRequest);


		//构建返回信息
		response.setUserName(member.getUsername());
		response.setOrderTotal(order.getPayment());
		response.setUserId(member.getId());
		response.setGoodsList(orderConverter.item2dto(orderItems));
		response.setTel(orderShippings.get(0).getReceiverPhone());
		response.setStatus(order.getStatus());
		response.setStreetName(orderShippings.get(0).getReceiverAddress());

		return response;
	}

	/**
	 * 根据订单id取消指定的订单
	 * @param cancelOrderRequest
	 * @return
	 */
	@Override
	public CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest) {
		CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
		String orderId = cancelOrderRequest.getOrderId();

		//第一步，置该订单状态为交易关闭
		Order order = orderMapper.selectByPrimaryKey(orderId);
		order.setStatus(5);
		orderMapper.updateByPrimaryKey(order);

		//第二步，将该订单的冻结的库存返还
		Example example = new Example(OrderItem.class);
		example.createCriteria().andEqualTo("orderId", orderId);
		List<OrderItem> orderItems = orderItemMapper.selectByExample(example);
		for (OrderItem orderItem : orderItems) {
			Stock stock = stockMapper.selectStock(orderItem.getItemId());
			stock.setStockCount(orderItem.getNum().longValue());
			stock.setLockCount(-orderItem.getNum());
			stockMapper.updateStock(stock);
			//置该订单商品状态为库存以释放
			orderItemMapper.updateStockStatus(2, orderId);
		}
		cancelOrderResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
		cancelOrderResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
		return cancelOrderResponse;
	}

	/**
	 * 删除指定订单id的订单
	 * @param deleteOrderRequest
	 * @return
	 */
	@Override
	public DeleteOrderResponse deleteOrder(DeleteOrderRequest deleteOrderRequest) {
		DeleteOrderResponse deleteOrderResponse = new DeleteOrderResponse();
		String orderId = deleteOrderRequest.getOrderId();

		//获取该订单信息
		Order order = orderMapper.selectByPrimaryKey(orderId);

		//校验该订单状态
		switch (order.getStatus()) {
			//已付款，未发货，已发货
			case 1:
			case 2:
			case 3: throw new BizException("当前订单尚未完成，无法删除");
			//未付款
			case 0:
				CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
				cancelOrderRequest.setOrderId(orderId);
				cancelOrder(cancelOrderRequest);
				return deleteOrder(deleteOrderRequest);
			//交易成功，交易关闭，交易失败，已退款
			case 4:
			case 5:
			case 6:
			case 7:
				//先在订单表中将该订单删除
				orderMapper.delete(order);
				//再在订单商品表中将该订单的商品删除
				Example itemExample = new Example(OrderItem.class);
				itemExample.createCriteria().andEqualTo("orderId", orderId);
				orderItemMapper.deleteByExample(itemExample);
				//再在物流信息表中，将该订单的物流信息删除
				Example shippingExample = new Example(OrderShipping.class);
				shippingExample.createCriteria().andEqualTo("orderId", orderId);
				orderShippingMapper.deleteByExample(shippingExample);
		}
		deleteOrderResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
		deleteOrderResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
		return deleteOrderResponse;
	}

}
