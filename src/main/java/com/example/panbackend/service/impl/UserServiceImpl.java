package com.example.panbackend.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.panbackend.dao.jpa.UserDao;
import com.example.panbackend.entity.dto.token.TokenInfoDTO;
import com.example.panbackend.entity.param.UserLoginParam;
import com.example.panbackend.entity.param.UserRegisterParam;
import com.example.panbackend.entity.po.UserPo;
import com.example.panbackend.response.ResponseCode;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

	private UserDao userDao;
	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	public UserServiceImpl setUserDao(UserDao userDao) {
		this.userDao = userDao;
		return this;
	}

	/**
	 * register
	 *注册服务
	 * @param param param
	 * @return {@link Result}
	 * @see Result
	 * @see String
	 */
	@Override
	public Result<String> register(UserRegisterParam param) {
		String username = param.getUsername();
		Optional<UserPo> byUsername = userDao.findUserByUsername(username);
		if(byUsername.isPresent()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"用户名已存在");
		}
		UserPo user = new UserPo(
				-1,
				param.getUsername(),
				SecureUtil.md5(param.getPassword())
		);
		UserPo temp = userDao.save(user);
		if(temp.getId()!=-1){
			return Result.ok("ok");
		}
		return Result.fail(ResponseCode.LOGIC_ERROR,"创建用户失败");
	}

	/**
	 * 登录服务
	 *
	 * @param param param
	 * @return {@link Result} 返回token
	 * @see Result
	 * @see String
	 */
	@Override
	public Result<TokenInfoDTO> login(UserLoginParam param) {
		Optional<UserPo> user = userDao.findUserByUsername(param.getUsername());
		if(user.isEmpty()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"无对应用户名");
		}
		UserPo temp = user.get();
		String dealPassword = SecureUtil.md5(param.getPassword());
		if(!Objects.equals(dealPassword, temp.getPassword())){
			return Result.fail(ResponseCode.LOGIC_ERROR,"密码错误");
		}
		StpUtil.login(temp.getId());
		SaTokenInfo info = StpUtil.getTokenInfo();
		return Result.ok(TokenInfoDTO.getInstance(info));
	}
}
