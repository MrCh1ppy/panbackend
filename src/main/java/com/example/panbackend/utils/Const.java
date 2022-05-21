package com.example.panbackend.utils;

import com.example.panbackend.exception.ProjectException;
import com.example.panbackend.response.ResponseCode;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public final class Const {
	public static final String REDIS_KEY_TO_FILE ="key:file:";
	public static final String REDIS_FILE_TO_KEY ="file:key:";
	public static final String REDIS_FILE_PATH ="file:path:";
	public static final String REDIS_FILE_TIME ="file:time:";
	public static final TemporalUnit SHARE_TIME_UNIT = ChronoUnit.MINUTES;
	public static final long AIR_DROP_SIZE_LIMIT = DataSize.parse("50MB").toBytes();
	public static final Path PRE_PATH;
	public static final int AIR_DROP_USER_ID=-1;
	public static final int  AIR_DROP_TTL=30;
	public static final String REDIS_THUMBNAIL="file:pic:thumbnail:";
	public static final Path THUMBNAIL_PRE_PATH;

	private Const() {
	}

	static {
		String rootPath = System.getProperty("user.dir");
		Path pan = Path.of(rootPath).resolve("pan");
		//主存储目录初始化
		Path mainStore = pan.resolve("mainStore");
		if(!mainStore.toFile().exists()){
			try {
				Files.createDirectories(mainStore);
			} catch (IOException e) {
				throw new ProjectException("初始存储地址创建失败", ResponseCode.DEFAULT_ERROR);
			}
		}
		PRE_PATH =mainStore;
		//缩略图目录初始化
		Path thumbnail = pan.resolve("thumbnail");
		if(!thumbnail.toFile().exists()){
			try {
				Files.createDirectories(thumbnail);
			} catch (IOException e) {
				throw new ProjectException("初始存储地址创建失败", ResponseCode.DEFAULT_ERROR);
			}
		}
		THUMBNAIL_PRE_PATH=thumbnail;
	}
}
