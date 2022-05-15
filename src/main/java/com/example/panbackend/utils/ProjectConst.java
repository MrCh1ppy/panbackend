package com.example.panbackend.utils;

import cn.hutool.core.util.ArrayUtil;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Setter
public class ProjectConst implements InitializingBean {
	@Value("${const.store.pre_path}")
	private String preStorePath;
	@Value("${const.store.divide}")
	private String divide;

	private Path prePath;

	@Override
	public void afterPropertiesSet() throws Exception {
		String[] split = preStorePath.split(divide);
		String[] sub = ArrayUtil.sub(split, 1, split.length);
		prePath= Paths.get(split[0], sub);
	}

	public Path getPrePath() {
		return prePath;
	}
}
