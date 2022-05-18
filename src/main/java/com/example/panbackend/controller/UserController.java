package com.example.panbackend.controller;

import com.example.panbackend.entity.dto.token.TokenInfoDTO;
import com.example.panbackend.entity.param.UserLoginParam;
import com.example.panbackend.entity.param.UserRegisterParam;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

	private UserService userService;

	@Autowired
	public UserController setUserService(UserService userService) {
		this.userService = userService;
		return this;
	}

	@PostMapping("/login")
	public Result<TokenInfoDTO> login(@RequestBody UserLoginParam userLoginParam){
		return userService.login(userLoginParam);
	}

	@PostMapping("/register")
	public Result<String> register(@RequestBody UserRegisterParam param){
		return userService.register(param);
	}
}
