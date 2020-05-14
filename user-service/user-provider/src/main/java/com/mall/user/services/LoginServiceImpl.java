package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.ExceptionUtil;
import com.mall.user.ILoginService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.UserConverterMapper;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import com.mall.user.utils.JwtTokenUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: jia.xue
 * @Email: xuejia@cskaoyan.onaliyun.com
 * @Description
 **/
@Service
@Component
public class LoginServiceImpl implements ILoginService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserConverterMapper userConverterMapper;
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        UserLoginResponse response = new UserLoginResponse();
        // 验证用户名和密码不能为空
        request.requestCheck();
        //验证用户名，首先根据用户名获取该用户的信息
        Example example = new Example(Member.class);
        example.createCriteria().andEqualTo("username",request.getUserName());
        List<Member> memberList = memberMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(memberList)){
            //如果没有查到，说明该username对应的用户不存在
            response.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
            response.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            return response;
        }

        Member member = memberList.get(0);

        // 判断用户是否激活
        if (member.getIsVerified().equalsIgnoreCase("N")) {
            //该用户未激活
            response.setCode(SysRetCodeConstants.USER_ISVERFIED_ERROR.getCode());
            response.setMsg(SysRetCodeConstants.USER_ISVERFIED_ERROR.getMessage());
            return response;
        }

        // 验证密码，先将传入的密码用同样的加密方式进行加密，然后将加密后的密码同从数据库获取的密码进行比对
        String password = request.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(member.getPassword())){
            // 验证密码失败
            response.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
            response.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            return response;
        }
        // 产生JWT token
        Map map = new HashMap<String,Object>();
        map.put("uid",member.getId());
        map.put("file",member.getFile());
        map.put("username",member.getUsername());
        String token = JwtTokenUtils.builder().msg(JSON.toJSONString(map)).build().creatJwtToken();

        response = userConverterMapper.converter(member);
        response.setToken(token);
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return response;
    }

    /**
     * 验证token
     * @param checkAuthRequest
     * @return
     */
    @Override
    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) throws Exception {
        CheckAuthResponse response = new CheckAuthResponse();
        checkAuthRequest.requestCheck();
        String userInfo = null;
        try {
            userInfo = JwtTokenUtils.builder().token(checkAuthRequest.getToken()).build().freeJwt();
        } catch (Exception e) {
            return (CheckAuthResponse) ExceptionUtil.handlerException4biz(response, e);
        }
        if (StringUtils.isEmpty(userInfo)) {
            response.setCode(SysRetCodeConstants.TOKEN_VALID_FAILED.getCode());
            response.setMsg(SysRetCodeConstants.TOKEN_VALID_FAILED.getMessage());
            return  response;
        }

        response.setUserinfo(userInfo);
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        return response;
    }
}
