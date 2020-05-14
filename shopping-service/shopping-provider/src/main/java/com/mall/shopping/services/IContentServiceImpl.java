package com.mall.shopping.services;

import com.mall.shopping.IContentService;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dto.NavListResponse;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * User：zhouchen
 * Time: 2020/5/13  20:58
 * Description:
 */
@Service
@Component
@Slf4j
public class IContentServiceImpl implements IContentService {
    @Autowired
    PanelContentMapper panelContentMapper;

    @Autowired
    ContentConverter contentConverter;

    /**
     * 查询所有的导航栏
     * @return
     */
    @Override
    public NavListResponse queryNavList() {
        NavListResponse navListResponse = new NavListResponse();
        //查询导航栏,panelId=0,即为导航栏
        Example example = new Example(PanelContent.class);
        example.createCriteria().andEqualTo("panelId", 0);
        List<PanelContent> panelContents = panelContentMapper.selectByExample(example);

        navListResponse.setPannelContentDtos(contentConverter.panelContents2Dto(panelContents));
        navListResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        navListResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return navListResponse;
    }
}
