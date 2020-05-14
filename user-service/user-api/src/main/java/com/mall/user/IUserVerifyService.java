package com.mall.user;

import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;

public interface IUserVerifyService {

    public UserVerifyResponse verify(UserVerifyRequest request);
}
