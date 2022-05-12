package com.example.panbackend.dao.jpa;

import com.example.panbackend.entity.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User,Integer> {
	Optional<User> findUserByUsername(String userName);
}
