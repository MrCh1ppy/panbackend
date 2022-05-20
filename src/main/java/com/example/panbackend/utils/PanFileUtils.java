package com.example.panbackend.utils;

import cn.hutool.core.io.FileUtil;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileSizeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public final class PanFileUtils {

	private ProjectConst projectConst;
	private static final String[] sizeUnit={"Byte","KB","MB","GB","TB"};
	private static int prePathSize=-1;
	@Autowired
	public PanFileUtils(ProjectConst projectConst) {
		this.projectConst = projectConst;
	}

	@Autowired
	public PanFileUtils setProjectConst(ProjectConst projectConst) {
		this.projectConst = projectConst;
		return this;
	}


	public FileDTO getFileDTO(File file) {
		double size = FileUtil.size(file);
		FileSizeDTO sizeDTO = getDataSize(size);
		return new FileDTO(
				FileUtil.getName(file),
				file.toPath().toString().substring(getPrePathSize()),
				sizeDTO.getSize(),
				sizeDTO.getSizeUnit(),
				file.isDirectory() ? "directory" : FileUtil.getType(file)
		);
	}

	public FileSizeDTO getDataSize(double bytesSize){
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

	public int getPrePathSize() {
		if(prePathSize!=-1){
			return prePathSize;
		}
		PanFileUtils.prePathSize=projectConst.getPrePath().toString().length();
		return prePathSize;
	}
}
