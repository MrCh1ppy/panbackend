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

	private static int prePathSize=-1;
	@Autowired
	public PanFileUtils(ProjectConst projectConst) {
		this.projectConst = projectConst;
	}



	public FileDTO getFileDTO(File file){
		double size = FileUtil.size(file);
		short index=0;
		double showSize=size;
		String showSizeUnit;
		while (true){
			if(showSize<1024){
				showSizeUnit=sizeUnit[index];
				break;
			}
			index++;
			showSize/=1024;
		}
		return new FileDTO(
				FileUtil.getName(file),
				file.toPath().toString().substring(getPrePathSize()),
				showSize,
				showSizeUnit,
				file.isDirectory()?"directory":FileUtil.getType(file)
		);
	}
	@Autowired
	public PanFileUtils setProjectConst(ProjectConst projectConst) {
		this.projectConst = projectConst;
		return this;
	}

	public int getPrePathSize() {
		if(prePathSize!=-1){
			return prePathSize;
		}
		prePathSize=projectConst.getPrePath().toString().length();
		return prePathSize;
	}
}
