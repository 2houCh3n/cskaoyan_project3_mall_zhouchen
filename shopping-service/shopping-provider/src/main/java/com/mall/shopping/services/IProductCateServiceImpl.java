package com.mall.shopping.services;

import com.alibaba.druid.util.StringUtils;
import com.mall.shopping.IProductCateService;
import com.mall.shopping.converter.ProductCateConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.AllProductCateResponse;
import com.mall.user.constants.SysRetCodeConstants;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * User：zhouchen
 * Time: 2020/5/14  7:29
 * Description:
 */
@Service
public class IProductCateServiceImpl implements IProductCateService {

    @Autowired
    ItemCatMapper itemCatMapper;

    @Autowired
    ProductCateConverter productCateConverter;

    /**
     * 获取商品所有的种类
     * @param request
     * @return
     */
    @Override
    public AllProductCateResponse getAllProductCate(AllProductCateRequest request) {
        AllProductCateResponse allProductCateResponse = new AllProductCateResponse();
        String sort = request.getSort();

        Example example = new Example(ItemCat.class);
        //如果要求排序，设置排序方式
        if (!StringUtils.isEmpty(sort)) {
            if ("1".equals(sort)) {
                example.setOrderByClause(sort + " asc");
            }
            if ("-1".equals(sort)) {
                example.setOrderByClause(sort + " desc");
            }
        }

        //获取所有的商品种类
        List<ItemCat> itemCats = itemCatMapper.selectByExample(example);

        allProductCateResponse.setProductCateDtoList(productCateConverter.items2Dto(itemCats));
        allProductCateResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        allProductCateResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return allProductCateResponse;
    }
}
