package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IProductService;
import com.mall.shopping.dto.*;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User：zhouchen
 * Time: 2020/5/14  7:43
 * Description:
 */
@RestController
public class ProductController {

    @Reference
    IProductService productService;

    @GetMapping("shopping/product/{id}")
    @Anoymous
    public ResponseData productDetail(@PathVariable("id") Long productId) {
        ProductDetailRequest productDetailRequest = new ProductDetailRequest();
        productDetailRequest.setId(productId);
        ProductDetailResponse productDetail = productService.getProductDetail(productDetailRequest);
        return new ResponseUtil().setData(productDetail.getProductDetailDto());
    }

    @GetMapping("shopping/goods")
    @Anoymous
    public ResponseData goods(AllProductRequest request) {
        AllProductResponse allProduct = productService.getAllProduct(request);
        return new ResponseUtil().setData(allProduct);
    }

    @GetMapping("shopping/recommend")
    @Anoymous
    public ResponseData recommend() {
        RecommendResponse recommendGoods = productService.getRecommendGoods();
        return new ResponseUtil().setData(recommendGoods.getPanelContentItemDtos());
    }
}
