package com.example.panbackend.utils;

import cn.hutool.core.util.ArrayUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Setter
@Slf4j
public class ProjectConst implements InitializingBean {

	@Value("${const.share.key_file_pre}")
	private String keyToFile;

	@Value("${const.share.file_key_pre}")
	private String fileToKey;

	private Path prePath;

	@Override
	public void afterPropertiesSet() throws Exception {
		String rootPath = System.getProperty("user.dir");
		Path root = Paths.get(rootPath).resolve("pan");
		Files.createDirectories(root);
		prePath= root;
		log.info("基础存放地址{}已装载", prePath);
		log.info("redis前缀{}已装载",this.keyToFile);
		log.info("redis前缀{}已装载",this.fileToKey);
	}

	public Path getPrePath() {
		return prePath;
	}

	public String getKeyToFile() {
		return keyToFile;
	}

	public String getFileToKey() {
		return fileToKey;
	}
}
