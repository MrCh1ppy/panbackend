package com.example.panbackend.controller;

import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.*;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/file")
@Slf4j
public class FileController {

	FileService fileService;

	@Autowired
	public FileController setFileService(FileService fileService) {
		this.fileService = fileService;
		return this;
	}
	@PostMapping("upload")
	public Result<String> upload(@RequestBody FileUploadParam param){
		return fileService.upload(param);
	}


	@PostMapping(value = "download")
	@ResponseBody
	public Object downLoad( HttpServletResponse response,String path){
		Result<String> result = fileService.fileDownLoad(response,path);
		if (result.getCode()!=200){
			return JSON.toJSON(result);
		}
		return null;
	}
}
