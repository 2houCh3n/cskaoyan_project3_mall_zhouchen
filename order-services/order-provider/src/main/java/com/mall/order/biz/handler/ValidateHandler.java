package com.mall.order.biz.handler;

import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.exception.BizException;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dto.CartProductDto;
import com.mall.user.IMemberService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.QueryMemberRequest;
import com.mall.user.dto.QueryMemberResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  ciggar
 * create-date: 2019/8/1-下午4:47
 *
 */
@Slf4j
@Component
public class ValidateHandler extends AbstractTransHandler {

    // check=false（启动检查） 是为了启动的时候不去检查注册中心该service是否存在
    @Reference(check = false)
    private IMemberService memberService;

    /**
     * 验证用户合法性
     * @return
     */

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;
        //查询该用户id对应的用户信息
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setUserId(createOrderContext.getUserId());
        QueryMemberResponse queryMemberResponse = memberService.queryMemberById(queryMemberRequest);

        if (!queryMemberResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            String username = queryMemberResponse.getUsername();
            if (!createOrderContext.getUserName().equals(username)) {
                throw new ValidateException(SysRetCodeConstants.USER_INFOR_INVALID.getCode(), SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
            }
            createOrderContext.setBuyerNickName(username);
        }
        return true;
    }
}
