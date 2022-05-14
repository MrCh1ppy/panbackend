package com.example.panbackend.controller;

import com.example.panbackend.entity.param.UserLoginParam;
import com.example.panbackend.entity.param.UserRegisterParam;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

	private UserService userService;

	@Autowired
	public UserController setUserService(UserService userService) {
		this.userService = userService;
		return this;
	}

	@GetMapping("/login")
	public Result<String> login(@RequestBody UserLoginParam userLoginParam){
		return userService.login(userLoginParam);
	}

	@GetMapping("/register")
	public Result<String> register(@RequestBody UserRegisterParam param){
		return userService.register(param);
	}
}
