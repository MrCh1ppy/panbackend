package com.example.panbackend;

import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import com.example.panbackend.service.impl.FileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class PanbackendApplicationTests {

	@Autowired
	FileService service;
	@Resource
	StringRedisTemplate stringRedisTemplate;

	@Test
	void contextLoads() {
	}

}
