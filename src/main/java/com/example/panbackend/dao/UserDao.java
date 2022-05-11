package com.example.panbackend.dao;

import com.example.panbackend.entity.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;

@Mapper
public interface UserDao extends JpaRepository<User,Integer> {
}
