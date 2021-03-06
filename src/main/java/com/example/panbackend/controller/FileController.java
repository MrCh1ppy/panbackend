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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                                 @RequestParam("sizeUnit")String sizeUnit,
                                 @RequestParam("divide")String divide

	){
		int id = StpUtil.getLoginIdAsInt();
		FileUploadParam param = new FileUploadParam(file, path, id, sizeLimit, sizeUnit);
		return fileService.upload(param,divide);
	}

	@PostMapping(value = "list")
	@SaCheckLogin
	public Result<List<FileDTO>>listFile(@RequestParam("path")String path,
	                                     @RequestParam("divide")String divide){
		int id = StpUtil.getLoginIdAsInt();
		return fileService.listPath(path, id,divide);
	}

	@PostMapping(value = "/download")
	@ResponseBody
	@SaCheckLogin
	public Result<String> downLoad( HttpServletResponse response,
	                                @RequestParam("path") String path,
	                                @RequestParam("divide")String divide,
                                    @RequestHeader("range")String range
	){
		int id = StpUtil.getLoginIdAsInt();
		Result<String> result = fileService.fileDownLoad(response,path,id,divide,range);
		return result.getCode()==200?null:result;
	}

	@SaCheckLogin
	@PostMapping("/tree")
	public Result<FileTreeDTO>getFileTree(@RequestParam("path") String path,
	                                      @RequestParam("divide")String divide){
		int id = StpUtil.getLoginIdAsInt();
		return fileService.getFileTree(path, id,divide);
	}

	@PostMapping("/delete")
	@SaCheckLogin
	public Result<String> delete(@RequestParam("path") String path,
	                             @RequestParam("divide")String divide){
		int id = StpUtil.getLoginIdAsInt();
		return fileService.fileDelete(path,id,divide);
	}

	@GetMapping("/share")
	@SaCheckLogin
	public Result<String> shareFile(@RequestParam("path")String path,
	                                @RequestParam("share_time")String num,
	                                @RequestParam("divide")String divide,
                                    @RequestParam("num_of_share")String numOfShare
	){
		int id = StpUtil.getLoginIdAsInt();
		int shareNum = Integer.parseInt(numOfShare);
		return fileService.shareFile(path, id, divide, Integer.parseInt(num),shareNum);
	}

	@PostMapping("/air/share")
	public Result<String> airDropShare(@RequestParam("file")MultipartFile file,
	                                   @RequestParam("num_of_share")String numOfShare){
		int shareNum = Integer.parseInt(numOfShare);
		return fileService.shareAirDrop(file, shareNum);
	}

	@ResponseBody
	@PostMapping("/receive")
	public Result<String> getShareFile(
			HttpServletResponse response,
			@RequestParam("code") String code,
			@RequestHeader("range")String range
	){
		Result<String> result = fileService.receiveFile(response, code,range);
		return result.getCode()==200?null:result;
	}

	@GetMapping("/copy")
	@SaCheckLogin
	public Result<String> fileCopy(
			@RequestParam("path")String path,
			@RequestParam("divide")String divide
	){
		int userID = StpUtil.getLoginIdAsInt();
		return fileService.copyFile(userID, path, divide);
	}

	@GetMapping("/rename")
	@SaCheckLogin
	public Result<String> fileRename(
			@RequestParam("path")String path,
			@RequestParam("divide")String divide,
			@RequestParam("name")String name
	){
		int userID = StpUtil.getLoginIdAsInt();
		return fileService.renameFile(userID,path,name,divide);
	}

	@GetMapping("/move")
	@SaCheckLogin
	public Result<String>fileMove(
			@RequestParam("path")String path,
			@RequestParam("divide")String divide,
			@RequestParam("target_path")String targetPath
	){
		int userID = StpUtil.getLoginIdAsInt();
		return fileService.moveFile(userID,path,divide,targetPath);
	}

	@GetMapping("/create/directory")
	public Result<String> createDirection(
			@RequestParam("path")String path,
			@RequestParam("divide")String divide,
			@RequestParam("directory")String dName
	){
		int userID = StpUtil.getLoginIdAsInt();
		return fileService.createDirectory(userID,path,divide,dName);
	}


}
