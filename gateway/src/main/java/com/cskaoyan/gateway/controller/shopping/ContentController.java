package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IContentService;
import com.mall.shopping.dto.NavListResponse;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User：zhouchen
 * Time: 2020/5/13  20:56
 * Description:
 */
@RestController
public class ContentController {

    @Reference
    IContentService contentService;

    @GetMapping("shopping/navigation")
    @Anoymous
    public ResponseData navigation() {
        NavListResponse navListResponse = contentService.queryNavList();
        return new ResponseUtil().setData(navListResponse.getPannelContentDtos());
    }
}
