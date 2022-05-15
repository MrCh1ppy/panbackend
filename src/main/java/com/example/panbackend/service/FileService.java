package com.example.panbackend.service;

import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileTreeDTO;
import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface FileService {
	Result<String> upload(FileUploadParam param,String divide);

	Result<String> fileDownLoad(HttpServletResponse response,String path,int userID,String divide);

	Result<List<FileDTO>> listPath(String path,int userID,String divide);

	Result<FileTreeDTO> getFileTree(String path,int userID,String divide);

	Result<String> fileDelete(String path,int userId,String divide);

	Result<String> shareFile(String path, int id,String divide,int num);

	Result<String> receiveFile(HttpServletResponse response, String code);
}

