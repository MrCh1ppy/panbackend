package com.example.panbackend.utils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
@Setter
@Slf4j
public class ProjectConst implements InitializingBean {

	@Value("${const.share.key_file_pre}")
	private String keyToFile;

	@Value("${const.share.file_key_pre}")
	private String fileToKey;

	private Path prePath;

	private TimeUnit shareTimeUnit=TimeUnit.MINUTES;

	@Value("${const.air_drop.air_drop_ttl}")
	private int airDropTTL=60;

	private long airDropSizeLimit=DataSize.parse("50MB").toBytes();

	@Value("${const.air_drop.air_drop_user_id}")
	private int airDropUserID=-1;

	@Value("${const.share.file_time}")
	private String fileReceiveTime;

	@Value("${const.share.file_path}")
	private String filePath;

	@Override
	public void afterPropertiesSet() throws Exception {
		String rootPath = System.getProperty("user.dir");
		Path root = Path.of(rootPath).resolve("pan");
		Files.createDirectories(root);
		prePath= root;
		log.info("基础存放地址{}已装载", prePath);
		log.info("redis key到文件前缀{}已装载",this.keyToFile);
		log.info("redis 文件到key前缀{}已装载",this.fileToKey);
		log.info("空投最大文件大小{}已装载",this.airDropSizeLimit);
		log.info("空投保留时间{}min,已装载",this.airDropTTL);

	}

	public String getKeyToFile() {
		return keyToFile;
	}

	public String getFileToKey() {
		return fileToKey;
	}

	public Path getPrePath() {
		return prePath;
	}

	public TimeUnit getShareTimeUnit() {
		return shareTimeUnit;
	}

	public int getAirDropTTL() {
		return airDropTTL;
	}

	public long getAirDropSizeLimit() {
		return airDropSizeLimit;
	}

	public int getAirDropUserID() {
		return airDropUserID;
	}

	public String getFileReceiveTime() {
		return fileReceiveTime;
	}

	public String getFilePath() {
		return filePath;
	}
}
