package com.mall.user.services;

import com.mall.commons.tool.exception.UpdateException;
import com.mall.user.IUserVerifyService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author: jia.xue
 * @Email: xuejia@cskaoyan.onaliyun.com
 * @Description
 **/
@Service
@Component
public class UserVerifyServiceImpl implements IUserVerifyService {

    @Autowired
    private UserVerifyMapper userVerifyMapper;

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 激活用户
     * 1.根据uuid去查询userVerify表，获取对应的数据
     *      如果没有查到数据，那么uuid无效，直接返回激活失败
     *      如果查到数据
     * 2.将查到的数据的username同传进来的username进行比对
     *      比对失败，就返回激活失败
     *      比对成功，就更新该表相应数据的isverify字段
     * 3.将member表的isverify字段也进行更新
     * @param request
     * @return
     */
    @Override
    @Transactional
    public UserVerifyResponse verify(UserVerifyRequest request) {
        UserVerifyResponse userVerifyResponse = new UserVerifyResponse();

        //对请求数据进行判空
        request.requestCheck();

        //根据uuid去查询userVerify这个表
        Example example = new Example(UserVerify.class);
        example.createCriteria().andEqualTo("uuid",request.getUuid());
        List<UserVerify> userVerifyList = userVerifyMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userVerifyList)) {
            //没有根据uuid查询到对应的user信息
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_INVALID.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
            return userVerifyResponse;
        }
        //把两个username进行比对
        UserVerify userVerify = userVerifyList.get(0);
        String userName = request.getUserName();
        if (!userName.equals(userVerify.getUsername())) {
            //如果查询到的username和传进来的username匹配不一致
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_INVALID.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
            return userVerifyResponse;
        }
        //比对成功 改userverify的激活字段
        userVerify.setIsVerify("Y");
        int effectedRows = userVerifyMapper.updateByPrimaryKey(userVerify);
        if (effectedRows < 1){
            //更新失败
            throw new UpdateException(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getCode(),SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
        }

        //比对成功，修改member的激活字段
        //根据username从member表中获取相应的用户信息
        Example exampleMember = new Example(Member.class);
        exampleMember.createCriteria().andEqualTo("username",request.getUserName());

        List<Member> members = memberMapper.selectByExample(exampleMember);
        if (CollectionUtils.isEmpty(members)){
            //没有查询到对应的用户信息
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getMessage());
            return userVerifyResponse;
        }
        // 人工制造异常验证事务功能
        //int i = 1 / 0;

        Member member = members.get(0);
        member.setIsVerified("Y");
        int effectedRows2 = memberMapper.updateByPrimaryKey(member);
        if (effectedRows2 < 1){
            //更新失败
            throw new UpdateException(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getCode(),SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
        }
        userVerifyResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        userVerifyResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return userVerifyResponse;
    }
}
