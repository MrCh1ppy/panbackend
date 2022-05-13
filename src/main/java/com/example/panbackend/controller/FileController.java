package com.example.panbackend.controller;

import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

	FileService fileService;

	@Autowired
	public FileController setFileService(FileService fileService) {
		this.fileService = fileService;
		return this;
	}
	@PostMapping("/upload")
	public Result<String> upload(@RequestParam("userID")int userID,
	                             @RequestParam("file") MultipartFile file,
	                             @RequestParam("path") String path,
                                 @RequestParam("sizeLimit")int sizeLimit,
                                 @RequestParam("sizeUnit")String sizeUnit

	){
		FileUploadParam param = new FileUploadParam(
				file,
				path,
				userID,
				sizeLimit,
				sizeUnit
				);
		return fileService.upload(param);
	}


	@PostMapping(value = "/download")
	@ResponseBody
	public Result<String> downLoad( HttpServletResponse response
			,@RequestParam("path") String path
			,@RequestParam("userID") int userID){
		Result<String> result = fileService.fileDownLoad(response,path,userID);
		if(result.getCode()==200){
			return null;
		}
		return result;
	}
}
