package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.exception.UpdateException;
import com.mall.user.IUserVerifyService;
import com.mall.user.annotation.Anoymous;

import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * User：zhouchen
 * Time: 2020/5/12  22:52
 * Description:
 */
@RestController
public class UserVerifyController {

    @Reference
    IUserVerifyService userVerifyService;

    /**
     * 对用户进行邮箱激活
     * @param uid
     * @param username
     * @param request
     * @return
     */
    @GetMapping("user/verify")
    @Anoymous
    public ResponseData verify(@RequestParam String uid, @RequestParam String username, HttpServletRequest request) {
        //第一步 对uid和username进行判空
        if (uid == null || uid.isEmpty() || username == null || username.isEmpty()) {
            return new ResponseUtil().setErrorMsg(SysRetCodeConstants.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        //激活用户，更新数据库数据
        UserVerifyRequest userVerifyRequest = new UserVerifyRequest();
        userVerifyRequest.setUuid(uid);
        userVerifyRequest.setUserName(username);
        UserVerifyResponse userVerifyResponse = null;
        try {
            userVerifyResponse = userVerifyService.verify(userVerifyRequest);
        } catch (UpdateException e) {
            //用户激活更新数据库表出现异常
            return new ResponseUtil().setErrorMsg(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getMessage());
        } catch (ArithmeticException e) {
            return new ResponseUtil().setErrorMsg(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getMessage());
        }

        if (!userVerifyResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            //激活失败
            return new ResponseUtil().setErrorMsg("激活失败");
        }
        return new ResponseUtil().setData(null);
    }
}
