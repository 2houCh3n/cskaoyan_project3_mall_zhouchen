package com.mall.shopping.services;

import com.mall.shopping.IHomeService;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.dal.entitys.Panel;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.HomePageResponse;
import com.mall.shopping.dto.PanelDto;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User：zhouchen
 * Time: 2020/5/13  20:17
 * Description:
 */
@Service
@Slf4j
@Component
public class IHomeServiceImpl implements IHomeService {

    @Autowired
    PanelMapper panelMapper;

    @Autowired
    PanelContentMapper panelContentMapper;

    @Autowired
    ContentConverter contentConverter;
    /**
     * 获取商城首页所需的数据
     * @return
     */
    @Override
    public HomePageResponse homepage() {
        HomePageResponse homePageResponse = new HomePageResponse();
        //获取所有的面板
        List<Panel> panels = panelMapper.selectAll();

        Set<PanelDto> panelDtos = new HashSet<>();
        //遍历panels，根据每一个panel的id，从panel_content表中获取对应的content
        for (Panel panel : panels) {
            List<PanelContentItem> panelContentItems = panelContentMapper.selectPanelContentAndProductWithPanelId(panel.getId());
            panel.setPanelContentItems(panelContentItems);
            panelDtos.add(contentConverter.panen2Dto(panel));
        }
        homePageResponse.setPanelContentItemDtos(panelDtos);
        homePageResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        homePageResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return homePageResponse;
    }
}
