package com.mall.shopping.services;

import com.alibaba.druid.util.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.shopping.IProductService;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.converter.ProductConverter;
import com.mall.shopping.dal.entitys.*;
import com.mall.shopping.dal.persistence.ItemDescMapper;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.*;
import com.mall.user.constants.SysRetCodeConstants;
import org.apache.dubbo.config.annotation.Service;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

/**
 * User：zhouchen
 * Time: 2020/5/14  7:46
 * Description:
 */
@Service
public class IProductServiceImpl implements IProductService {

    @Autowired
    ItemMapper itemMapper;

    @Autowired
    ItemDescMapper itemDescMapper;

    @Autowired
    ProductConverter productConverter;

    @Autowired
    PanelContentMapper panelContentMapper;

    @Autowired
    PanelMapper panelMapper;

    @Autowired
    ContentConverter contentConverter;

    /**
     * 获取商品详情
     * @param request
     * @return
     */
    @Override
    public ProductDetailResponse getProductDetail(ProductDetailRequest request) {
        ProductDetailResponse productDetailResponse = new ProductDetailResponse();
        Long productId = request.getId();
        //从item表获取指定商品的详细信息
        Item item = itemMapper.selectByPrimaryKey(productId);

        //从itemdesc表中获取该商品的详细描述
        ItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(productId);

        //构建返回数据
        ProductDetailDto productDetailDto = productConverter.item2DetailDto(item);
        List<String> images = Arrays.asList(item.getImages());
        productDetailDto.setProductImageSmall(images);
        productDetailDto.setProductImageBig(images.get(0));
        productDetailDto.setDetail(itemDesc.getItemDesc());

        productDetailResponse.setProductDetailDto(productDetailDto);
        productDetailResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        productDetailResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return productDetailResponse;
    }

    /**
     * 分页查询商品列表
     * @param request
     * @return
     */
    @Override
    public AllProductResponse getAllProduct(AllProductRequest request) {
        AllProductResponse allProductResponse = new AllProductResponse();
        String sort = request.getSort();
        Integer priceGt = request.getPriceGt();
        Integer priceLte = request.getPriceLte();
        Integer page = request.getPage();
        Integer size = request.getSize();

        //开启分页
        PageHelper.startPage(page, size);

        //开始查询商品列表
        Example example = new Example(Item.class);
        example.createCriteria().andBetween("price", new BigDecimal(priceGt), new BigDecimal(priceLte));
        if (!StringUtils.isEmpty(sort)) {
            if ("1".equals(sort)) {
                example.setOrderByClause("price desc");
            }
            if ("-1".equals(sort)) {
                example.setOrderByClause("price asc");
            }
        }
        List<Item> items = itemMapper.selectByExample(example);

        PageInfo<Item> itemPageInfo = new PageInfo<>(items);

        allProductResponse.setProductDtoList(productConverter.items2Dto(itemPageInfo.getList()));
        allProductResponse.setTotal(itemPageInfo.getTotal());
        allProductResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        allProductResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return allProductResponse;
    }

    /**
     * 获取推荐商品
     * @return
     */
    @Override
    public RecommendResponse getRecommendGoods() {
        RecommendResponse recommendResponse = new RecommendResponse();
        //推荐商品panelId为6
        //获取panel
        Panel panel = panelMapper.selectByPrimaryKey(6);

        //首先从panel-content 表中获取所有的productId
        Example example = new Example(PanelContent.class);
        example.createCriteria().andEqualTo("panelId", 6);
        List<PanelContent> panelContents = panelContentMapper.selectByExample(example);

        Set<Panel> panels = new HashSet<>();

        List<PanelContentItem> panelContentItems = contentConverter.panelContents2Item(panelContents);
        for (PanelContentItem panelContentItem : panelContentItems) {
            Item item = itemMapper.selectByPrimaryKey(panelContentItem.getProductId());
            panelContentItem.setProductName(item.getTitle());
            panelContentItem.setSalePrice(item.getPrice());
            panelContentItem.setSubTitle(item.getSellPoint());
        }
        panel.setPanelContentItems(panelContentItems);
        panels.add(panel);

        recommendResponse.setPanelContentItemDtos(contentConverter.panels2Dto(panels));
        recommendResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        recommendResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return recommendResponse;
    }
}
