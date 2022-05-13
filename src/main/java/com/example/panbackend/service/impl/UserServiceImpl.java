package com.example.panbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.panbackend.dao.jpa.UserDao;
import com.example.panbackend.entity.param.UserLoginParam;
import com.example.panbackend.entity.param.UserRegisterParam;
import com.example.panbackend.entity.po.User;
import com.example.panbackend.exception.ProjectException;
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

	@Override
	public Result<String> register(UserRegisterParam param) {
		String username = param.getUsername();
		Optional<User> byUsername = userDao.findUserByUsername(username);
		if(!byUsername.isPresent()){
			throw new ProjectException("用户名已存在",ResponseCode.DEFAULT_ERROR);
		}
		User user = new User(
				-1,
				param.getUsername(),
				SecureUtil.md5(param.getPassword())
		);
		User temp = userDao.save(user);
		if(temp.getId()!=-1){
			return Result.ok("ok");
		}
		throw new ProjectException("insert fail",ResponseCode.DEFAULT_ERROR);
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
	public Result<String> login(UserLoginParam param) {
		Optional<User> user = userDao.findUserByUsername(param.getUsername());
		if(!user.isPresent()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"无对应用户名");
		}
		User temp = user.get();
		String dealPassword = SecureUtil.md5(param.getPassword());
		if(!Objects.equals(dealPassword, temp.getPassword())){
			return Result.fail(ResponseCode.LOGIC_ERROR,"密码错误");
		}
		StpUtil.login(temp.getId());
		return null;
	}
}
