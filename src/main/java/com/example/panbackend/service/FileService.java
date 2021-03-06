package com.example.panbackend.service;

import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileTreeDTO;
import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface FileService {
	Result<String> upload(FileUploadParam param,String divide);

	Result<String> fileDownLoad(HttpServletResponse response, String path, int userID, String divide, String range);

	Result<List<FileDTO>> listPath(String path,int userID,String divide);

	Result<FileTreeDTO> getFileTree(String path,int userID,String divide);

	Result<String> fileDelete(String path,int userId,String divide);

	Result<String> shareFile(String path, int id,String divide,int num,int numOfShare);

	Result<String> receiveFile(HttpServletResponse response, String code,String range);

	Result<String> shareAirDrop(MultipartFile file,int numOfShare);

	Result<String> copyFile(int userID,String path,String divide);

	Result<String> renameFile(int userID, String path, String name, String divide);

	Result<String> moveFile(int userID, String path, String divide, String targetPath);

	Result<String> createDirectory(int userID, String path, String divide, String dName);
}

