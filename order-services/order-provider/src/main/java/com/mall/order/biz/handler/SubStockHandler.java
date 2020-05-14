package com.mall.order.biz.handler;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.BaseBusinessException;
import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.CartProductDto;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 扣减库存处理器
 * @Author： wz
 * @Date: 2019-09-16 00:03
 **/
@Component
@Slf4j
public class SubStockHandler extends AbstractTransHandler {

    @Autowired
    private StockMapper stockMapper;

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@Transactional
	public boolean handle(TransHandlerContext context) {
		CreateOrderContext createOrderContext = (CreateOrderContext) context;
		List<CartProductDto> cartProductDtoList = createOrderContext.getCartProductDtoList();
		List<Long> buyProductIds = createOrderContext.getBuyProductIds();

		if (CollectionUtils.isEmpty(buyProductIds)) {
			buyProductIds = new ArrayList<>();
			for (CartProductDto cartProductDto : cartProductDtoList) {
				buyProductIds.add(cartProductDto.getProductId());
			}
		}
		buyProductIds.sort(Long::compareTo);

		//锁定库存
		List<Stock> stocksForUpdate = stockMapper.findStocksForUpdate(buyProductIds);
		if (CollectionUtils.isEmpty(stocksForUpdate)) {
			throw new BizException("库存未初始化");
		}
		if (stocksForUpdate.size() != buyProductIds.size()) {
			throw new BizException("部分商品库存未初始化");
		}

		//扣减库存
		for (CartProductDto cartProductDto : cartProductDtoList) {
			Long productId = cartProductDto.getProductId();
			Long productNum = cartProductDto.getProductNum();

			//验证productNum是否超出我们限购的数量
			Stock stock = stockMapper.selectStock(productId);
			if (stock.getRestrictCount() < productNum) {
				throw new BizException("超出限购数量");
			}
			stock.setLockCount(productNum.intValue());
			stock.setStockCount(-productNum);
			stockMapper.updateStock(stock);
		}
		return true;
	}
}
