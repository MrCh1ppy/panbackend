package com.example.panbackend.entity.po;

import cn.dev33.satoken.stp.StpUtil;
import com.example.panbackend.entity.dto.file.FileSizeDTO;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class HistoryPo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private HistoryType dealType;
	private String fileName;
	private String path;
	private double size;
	private String sizeUnit;
	private String fileType;
	private LocalDateTime dealTime;
	private int userID;


	public static HistoryPo getInstance(String fileType, String fileName, FileSizeDTO fileSizeDTO, HistoryType dealType) {
		HistoryPo the = new HistoryPo();
		the.dealType = dealType;
		the.fileName = fileName;
		the.fileType = fileType;
		the.userID = StpUtil.getLoginIdAsInt();
		the.size= fileSizeDTO.getSize();
		the.sizeUnit=fileSizeDTO.getSizeUnit();
		the.dealTime=LocalDateTime.now();
		return the;
	}





	/**
	 * 没必要不使用，请使用 HistoryPO.getInstance(...)的静态工程类
	 * */
	public HistoryPo() {
	}


	public enum HistoryType{
		UPLOAD,
		DOWNLOAD,
		SHARE
	}
}
