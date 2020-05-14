package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IProductCateService;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.AllProductCateResponse;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User：zhouchen
 * Time: 2020/5/14  7:24
 * Description:
 */
@RestController
public class ProductCateController {

    @Reference
    IProductCateService productCateService;

    @GetMapping("shopping/categories")
    @Anoymous
    public ResponseData categories(@RequestParam String sort) {
        AllProductCateRequest allProductCateRequest = new AllProductCateRequest();
        allProductCateRequest.setSort(sort);
        AllProductCateResponse allProductCate = productCateService.getAllProductCate(allProductCateRequest);
        return new ResponseUtil().setData(allProductCate.getProductCateDtoList());
    }
}
