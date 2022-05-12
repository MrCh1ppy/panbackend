package com.example.panbackend.entity.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginParam {
	private final String username;
	private final String password;

	public UserLoginParam(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
