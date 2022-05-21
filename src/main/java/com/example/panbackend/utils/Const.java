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
	public static final String KEY_TO_FILE ="key_to_file";
	public static final String FILE_TO_KEY ="file_to_key";
	public static final String FILE_PATH ="file_path";
	public static final String FILE_TIME ="file_time";
	public static final TemporalUnit SHARE_TIME_UNIT = ChronoUnit.MINUTES;
	public static final long AIR_DROP_SIZE_LIMIT = DataSize.parse("50MB").toBytes();
	public static final Path PRE_PATH;
	public static final int AIR_DROP_USER_ID=-1;
	public static final int  AIR_DROP_TTL=30;

	private Const() {
	}

	static {
		String rootPath = System.getProperty("user.dir");
		Path pan = Path.of(rootPath).resolve("pan");
		if(!pan.toFile().exists()){
			try {
				Files.createDirectories(pan);
			} catch (IOException e) {
				throw new ProjectException("初始存储地址创建失败", ResponseCode.DEFAULT_ERROR);
			}
		}
		PRE_PATH =pan;
	}
}
