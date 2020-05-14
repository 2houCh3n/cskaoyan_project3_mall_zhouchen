package com.mall.user;

import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;

public interface ILoginService {

    public UserLoginResponse login(UserLoginRequest request);

    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) throws Exception;
}
