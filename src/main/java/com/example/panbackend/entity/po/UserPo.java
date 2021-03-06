package com.example.panbackend.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class UserPo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String username;
	private String password;

	public UserPo(int id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public UserPo() {
	}
}
