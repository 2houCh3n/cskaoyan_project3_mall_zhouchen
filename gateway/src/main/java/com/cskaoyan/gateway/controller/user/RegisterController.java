package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.AbstractResponse;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.exception.ExceptionUtil;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.IRegisterService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * User：zhouchen
 * Time: 2020/5/12  16:11
 * Description:
 */
@RestController
public class RegisterController {
    @Reference
    IKaptchaService kaptchaService;

    @Reference
    IRegisterService registerService;

    /**
     * 用户注册处理器
     * @param requestData
     * @return
     */
    @PostMapping("user/register")
    @Anoymous
    public ResponseData register(@RequestBody Map requestData, HttpServletRequest request){
        String username = (String) requestData.get("userName");
        String userPwd = (String) requestData.get("userPwd");
        String captcha = (String) requestData.get("captcha");
        String email = (String) requestData.get("email");

        //第一步 验证传过来的验证码
//        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
//        String uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
//        kaptchaCodeRequest.setUuid(uuid);
//        kaptchaCodeRequest.setCode(captcha);
//        KaptchaCodeResponse kaptchaCodeResponse = kaptchaService.validateKaptchaCode(kaptchaCodeRequest);
//        if (!kaptchaCodeResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
//            //验证失败
//            return new ResponseUtil().setErrorMsg(kaptchaCodeResponse.getMsg());
//        }


        //验证完毕，再向用户表中插入记录
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
        userRegisterRequest.setUserName(username);
        userRegisterRequest.setUserPwd(userPwd);
        userRegisterRequest.setEmail(email);
        UserRegisterResponse registerResponse = null;
        try {
            registerResponse = registerService.register(userRegisterRequest);
        } catch (Exception e) {
            return new ResponseUtil().setErrorMsg(SysRetCodeConstants.SYSTEM_ERROR.getMessage());
        }
        if (!registerResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            //插入失败
            return new ResponseUtil().setErrorMsg(registerResponse.getMsg());
        }
        return new ResponseUtil().setData(null);
    }
}
