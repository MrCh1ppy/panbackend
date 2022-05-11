package com.example.panbackend.entity.po;

import javax.persistence.*;
import java.util.List;

@Entity
public class Group {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;
	@ManyToMany(
			targetEntity = User.class
	)
	List<User> manager;

	public Group(int id, List<User> manager) {
		this.id = id;
		this.manager = manager;
	}

	public Group() {
	}
}
