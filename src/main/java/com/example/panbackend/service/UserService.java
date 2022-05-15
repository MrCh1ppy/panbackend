package com.example.panbackend.service;

import com.example.panbackend.entity.dto.token.TokenInfoDTO;
import com.example.panbackend.entity.param.UserLoginParam;
import com.example.panbackend.entity.param.UserRegisterParam;
import com.example.panbackend.response.Result;

public interface UserService {
	/**
	 * 注册服务
	 *
	 * @param param param
	 * @return {@link Result}
	 * @see Result
	 * @see String
	 */
	Result<String> register(UserRegisterParam param);

	/**
	 * 登录服务
	 *
	 * @param param param
	 * @return {@link Result} 返回token
	 * @see Result
	 * @see String
	 */
	Result<TokenInfoDTO> login(UserLoginParam param);
}
