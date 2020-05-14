package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IHomeService;
import com.mall.shopping.dto.HomePageResponse;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User：zhouchen
 * Time: 2020/5/13  20:13
 * Description:
 */
@RestController
public class HomePageController {

    @Reference
    IHomeService homeService;

    @GetMapping("shopping/homepage")
    @Anoymous
    public ResponseData homepage() {
        HomePageResponse homepage = homeService.homepage();
        return new ResponseUtil().setData(homepage.getPanelContentItemDtos());
    }

}
