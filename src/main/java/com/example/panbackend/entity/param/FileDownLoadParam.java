package com.example.panbackend.entity.param;

public class FileDownLoadParam {
	private int userID;
	private String path;

	public FileDownLoadParam(int userID, String path) {
		this.userID = userID;
		this.path = path;
	}

	public int getUserID() {
		return userID;
	}

	public String getPath() {
		return path;
	}
}
