package com.example.panbackend.entity.param;

import org.springframework.web.multipart.MultipartFile;


public class FileUploadParam {
	private final MultipartFile file;
	private final String path;
	private final int userID;
	private final int sizeLimit;
	private final String sizeUnit;

	public FileUploadParam(MultipartFile file, String path, int userID, int sizeLimit, String timeUnit) {
		this.file = file;
		this.path = path;
		this.userID = userID;
		this.sizeLimit = sizeLimit;
		this.sizeUnit = timeUnit;
	}


	public MultipartFile getFile() {
		return file;
	}

	public String getPath() {
		return path;
	}

	public int getUserID() {
		return userID;
	}

	public int getSizeLimit() {
		return sizeLimit;
	}

	public String getSizeUnit() {
		return sizeUnit;
	}
}
