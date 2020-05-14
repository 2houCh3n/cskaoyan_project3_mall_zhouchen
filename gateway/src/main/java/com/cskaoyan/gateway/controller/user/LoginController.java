package com.cskaoyan.gateway.controller.user;

import com.alibaba.fastjson.JSON;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.ILoginService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * User：zhouchen
 * Time: 2020/5/12  17:24
 * Description:
 */
@RestController
public class LoginController {

    @Reference
    private IKaptchaService kaptchaService;

    @Reference
    private ILoginService loginService;

    @PostMapping("user/login")
    @Anoymous
    public ResponseData login(@RequestBody Map<String, String> requestData, HttpServletRequest request, HttpServletResponse response) {
        String username = requestData.get("userName");
        String userPwd = requestData.get("userPwd");
        String captcha = requestData.get("captcha");

       // 第一步 验证验证码
//        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
//        String uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
//        kaptchaCodeRequest.setUuid(uuid);
//        kaptchaCodeRequest.setCode(captcha);
//        KaptchaCodeResponse kaptchaCodeResponse = kaptchaService.validateKaptchaCode(kaptchaCodeRequest);
//        if (!kaptchaCodeResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
//            //验证失败
//            return new ResponseUtil().setErrorMsg(kaptchaCodeResponse.getMsg());
//        }
        //第二步 验证用户名和密码
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserName(username);
        userLoginRequest.setPassword(userPwd);
        UserLoginResponse userLoginResponse = null;
        userLoginResponse = loginService.login(userLoginRequest);
        if (!userLoginResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            //如果用户名和密码验证失败
            return new ResponseUtil().setErrorMsg(userLoginResponse.getMsg());
        }

        //第三步 产生一个合法的JWT，JWT在service中生成并注入到userLoginResponse中，这里将JWT写入cookie中，以致于下一次会自动携带
        Cookie cookie = CookieUtil.genCookie(TokenIntercepter.ACCESS_TOKEN, userLoginResponse.getToken(), "/", 3600);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return new ResponseUtil().setData(userLoginResponse);
    }

    /**
     * 用户登录验证接口
     * @param request
     * @return
     */
    @GetMapping("user/login")
    public ResponseData login(HttpServletRequest request) {
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Object parse = JSON.parse(userInfo);
        return new ResponseUtil().setData(parse);
    }

    /**
     * 用户登出接口
     * @param request
     * @return
     */
    @GetMapping("user/loginOut")
    public ResponseData loginOut(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TokenIntercepter.ACCESS_TOKEN)) {
                    cookie.setValue(null);
                    //让该cookie立马消失
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    //覆盖原来的cookie
                    response.addCookie(cookie);
                }
            }
        }
        return new ResponseUtil().setData(null);
    }
}
