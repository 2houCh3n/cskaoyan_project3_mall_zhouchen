package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.ExceptionUtil;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.IRegisterService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author: jia.xue
 * @Email: xuejia@cskaoyan.onaliyun.com
 * @Description
 **/
@Service
@Slf4j
public class RegisterServiceImpl implements IRegisterService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserVerifyMapper userVerifyMapper;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 注册用户
     * @param registerRequest
     * @return
     * @throws Exception
     */
    @Override
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest registerRequest) throws Exception {
        UserRegisterResponse response = new UserRegisterResponse();
        try {
            //判空验证，如果为空，则抛出一个volidate异常
            registerRequest.requestCheck();
            // 验证用户名是否重复,如果重复则抛出一个volidate异常
            volidateUserNameRepeat(registerRequest);
            //验证邮箱是否重复，如果重复则抛出一个validate异常
            validateEmailRepeat(registerRequest);
        } catch (Exception e) {
            return (UserRegisterResponse) ExceptionUtil.handlerException4biz(response, e);
        }
        //1. 向用户表中插入一条记录
        Member member = new Member();
        //组织用户信息
        member.setUsername(registerRequest.getUserName());
        member.setEmail(registerRequest.getEmail());
        member.setCreated(new Date());
        member.setUpdated(new Date());
        member.setIsVerified("N");
        member.setState(1);
        // password 要加密处理
        String md5password = DigestUtils.md5DigestAsHex(registerRequest.getUserPwd().getBytes());
        member.setPassword(md5password);


        int effectRows = memberMapper.insert(member);
        if (effectRows < 1) {
            //向用户表中插入记录失败，就直接返回
            response.setCode(SysRetCodeConstants.USER_REGISTER_FAILED.getCode());
            response.setMsg(SysRetCodeConstants.USER_REGISTER_FAILED.getMessage());
            return response;
        }

        // 人工制造异常，验证事务功能
        //int i = 1 / 0;

        //2. 向用户验证表中插入一条记录
        UserVerify userVerify = new UserVerify();
        userVerify.setUsername(member.getUsername());
        String key = member.getUsername()+member.getPassword()+UUID.randomUUID().toString();
        String uuid = DigestUtils.md5DigestAsHex(key.getBytes());
        userVerify.setUuid(uuid);
        userVerify.setRegisterDate(new Date());
        userVerify.setIsExpire("N");
        userVerify.setIsVerify("N");
        int rows = userVerifyMapper.insert(userVerify);
        if (rows != 1) {
            //向用户验证表中插入记录失败
            response.setCode(SysRetCodeConstants.USER_REGISTER_VERIFY_FAILED.getCode());
            response.setMsg(SysRetCodeConstants.USER_REGISTER_VERIFY_FAILED.getMessage());
            return response;
        }

        //3. 发送用户激活邮件
        //TODO 发送用户激活邮件 激活邮件应该是一个链接 有一个接口去处理我们的用户激活 消息中间件MQ
        sendEmail(uuid,registerRequest);

        //打印日志
        log.info("用户注册成功，注册参数request:{},{}",JSON.toJSONString(registerRequest),"xxx");
        // 用户注册成功，注册参数request:registerRequest,xxx
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return response;
    }



    /**
     * 发送用户激活邮件
     * @param uuid
     * @param registerRequest
     */
    private void sendEmail(String uuid, UserRegisterRequest registerRequest) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setSubject("CSMALL用户激活");
        message.setFrom("zhouchen563x@163.com");
        message.setTo(registerRequest.getEmail());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://localhost:8080/user/verify?uid=").append(uuid).append("&username=").append(registerRequest.getUserName());
        // http://localhost:8080/user/verify?uid=xxxx&username=xxxx
        message.setText(stringBuilder.toString());

        mailSender.send(message);

    }


    // 验证用户名是否重复
    //这里有一个bug
    private void volidateUserNameRepeat(UserRegisterRequest registerRequest) {
        Example example = new Example(Member.class);
        example.createCriteria().andEqualTo("username",registerRequest.getUserName());
        /** select * from tb_member where username = #{username} **/
        List<Member> memberList = memberMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(memberList)){
            throw new ValidateException(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getCode(),SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getMessage());
        }
    }

    /**
     * 验证用户注册邮箱是否重复
     * @param registerRequest
     */
    private void validateEmailRepeat(UserRegisterRequest registerRequest) {
        Example example = new Example(Member.class);
        example.createCriteria().andEqualTo("email", registerRequest.getEmail());

        List<Member> members = memberMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(members)) {
            throw new ValidateException(SysRetCodeConstants.EMAIL_ALREADY_EXISTS.getCode(), SysRetCodeConstants.EMAIL_ALREADY_EXISTS.getMessage());
        }
    }
}
