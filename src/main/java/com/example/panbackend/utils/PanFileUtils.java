package com.example.panbackend.utils;

import cn.hutool.core.io.FileUtil;
import com.example.panbackend.entity.dto.file.FileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public final class PanFileUtils {

	private ProjectConst projectConst;
	private static final String[] sizeUnit={"Byte","KB","MB","GB","TB"};

	private PanFileUtils() {
	}

	@Autowired
	public PanFileUtils(ProjectConst projectConst) {
		this.projectConst = projectConst;
	}

	public FileDTO getFileDTO(File file){
		long size = FileUtil.size(file);
		short index=0;
		double showSize;
		String showSizeUnit;
		while (true){
			if(size<1024){
				showSize=size/1024.0;
				showSizeUnit=sizeUnit[index];
				break;
			}
			index++;
			size/=1024;
		}
		return new FileDTO(
				FileUtil.getName(file),
				FileUtil.getCanonicalPath(file).substring(projectConst.getPreStorePath().length()),
				showSize,
				showSizeUnit,
				FileUtil.getType(file)
		);
	}
	@Autowired
	public PanFileUtils setProjectConst(ProjectConst projectConst) {
		this.projectConst = projectConst;
		return this;
	}
}
