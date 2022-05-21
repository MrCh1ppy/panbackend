package com.example.panbackend.utils;

import cn.hutool.core.io.FileUtil;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileSizeDTO;

import java.io.File;

import static com.example.panbackend.utils.Const.PRE_PATH;
public final class PanFileUtils {
	private static final String[] sizeUnit={"Byte","KB","MB","GB","TB"};
	private static final int PRE_PATH_SIZE;

	private PanFileUtils() {
	}

	static {
		PRE_PATH_SIZE =PRE_PATH.toString().length();
	}

	public static FileDTO getFileDTO(File file) {
		double size = FileUtil.size(file);
		FileSizeDTO sizeDTO = getDataSize(size);
		return new FileDTO(
				FileUtil.getName(file),
				file.toPath().toString().substring(PRE_PATH_SIZE),
				sizeDTO.getSize(),
				sizeDTO.getSizeUnit(),
				file.isDirectory() ? "directory" : FileUtil.getType(file)
		);
	}

	public static FileSizeDTO getDataSize(double bytesSize){
		double showSize = bytesSize;
		String showSizeUnit;
		short index = 0;
		while (true) {
			if (showSize < 1024) {
				showSizeUnit = sizeUnit[index];
				break;
			}
			index++;
			showSize /= 1024;
		}
		return new FileSizeDTO(
				showSize,
				showSizeUnit
		);
	}


}
