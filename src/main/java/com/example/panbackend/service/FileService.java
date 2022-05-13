package com.example.panbackend.service;

import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;

import javax.servlet.http.HttpServletResponse;

public interface FileService {
	Result<String> upload(FileUploadParam param);

	Result<String> fileDownLoad(HttpServletResponse response,String path,int userID);
}
