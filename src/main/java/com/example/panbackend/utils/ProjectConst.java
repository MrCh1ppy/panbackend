package com.example.panbackend.utils;

import cn.hutool.core.util.ArrayUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Setter
@Slf4j
public class ProjectConst implements InitializingBean {
	@Value("${const.store.pre_path}")
	private String preStorePath;
	@Value("${const.store.divide}")
	private String divide;

	private Path prePath;

	public ProjectConst() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String[] split = preStorePath.split(divide);
		String[] sub = ArrayUtil.sub(split, 1, split.length);
		prePath= Paths.get(split[0], sub);
		log.info("基础存放地址{}已装载",prePath.toString());
	}

	public Path getPrePath() {
		return prePath;
	}
}
