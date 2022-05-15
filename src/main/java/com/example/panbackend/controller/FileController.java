package com.example.panbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileTreeDTO;
import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.List;

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
	@SaCheckLogin
	public Result<String> upload(@RequestParam("file") MultipartFile file,
	                             @RequestParam("path") String path,
                                 @RequestParam("sizeLimit")int sizeLimit,
                                 @RequestParam("sizeUnit")String sizeUnit

	){
		int id = StpUtil.getLoginIdAsInt();
		FileUploadParam param = new FileUploadParam(file, path, id, sizeLimit, sizeUnit);
		return fileService.upload(param,"-");
	}

	@PostMapping(value = "list")
	@SaCheckLogin
	public Result<List<FileDTO>>listFile(@RequestParam("path")String path){
		int id = StpUtil.getLoginIdAsInt();
		return fileService.listPath(path, id,"-");
	}

	@PostMapping(value = "/download")
	@ResponseBody
	@SaCheckLogin
	public Result<String> downLoad( HttpServletResponse response,
	                                @RequestParam("path") String path){
		int id = StpUtil.getLoginIdAsInt();
		Result<String> result = fileService.fileDownLoad(response,path,id,"-");
		if(result.getCode()==200){
			return null;
		}
		return result;
	}

	@SaCheckLogin
	@PostMapping("/tree")
	public Result<FileTreeDTO>getFileTree(@RequestParam("path") String path){
		int id = StpUtil.getLoginIdAsInt();
		return fileService.getFileTree(path, id,"-");
	}

	@PostMapping("/delete")
	@SaCheckLogin
	public Result<String> delete(@RequestParam("path") String path){
		int id = StpUtil.getLoginIdAsInt();
		return fileService.fileDelete(path,id,"-");
	}

	@GetMapping("/share")
	@SaCheckLogin
	public Result<String> shareFile(@PathParam("path")String path){
		int id = StpUtil.getLoginIdAsInt();
		Result<String> res=fileService.shareFile(path,id,"-");
		return null;
	}
}
