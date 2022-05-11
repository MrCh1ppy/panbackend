package com.example.panbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import com.example.panbackend.dao.UserDao;
import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.entity.po.User;
import com.example.panbackend.exception.ProjectException;
import com.example.panbackend.response.ResponseCode;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

	UserDao userDao;

	@Autowired
	public FileServiceImpl setUserDao(UserDao userDao) {
		this.userDao = userDao;
		return this;
	}

	@Override
	public Result<String> upload(FileUploadParam param) {
		MultipartFile file = param.getFile();
		String fileName = file.getOriginalFilename();
		Optional<User> user = userDao.findById(param.getUserID());
		if(!user.isPresent()){
			throw new ProjectException("无对应用户",ResponseCode.INVALID_PARAMETER);
		}
		if (checkSize(param.getFile(),param.getSizeLimit(),param.getSizeUnit())){
			throw  new ProjectException("程序超过限制大小",ResponseCode.INVALID_PARAMETER);
		}
		File dest = new File(param.getPath() + "// " + fileName);
		if(!dest.getParentFile().exists()){
			if (dest.getParentFile().mkdirs()) {
				log.warn("文件夹未创建");
				throw new ProjectException("文件夹未创建成功",ResponseCode.DEFAULT_ERROR);
			}
			try{
				file.transferTo(dest);
			}catch (IOException e){
				log.warn("文件传输错误");
				throw new ProjectException("程序错误，请重上传",ResponseCode.DEFAULT_ERROR);
			}
		}
		return Result.ok("成功传输，路径为"+param.getPath()+" "+param.getFile().getOriginalFilename());
	}

	private static boolean checkSize(MultipartFile file,int size,String unit){
		final int K=1024;
		final int M=K*1024;
		final int G=M*1024;
		if(file.isEmpty()||size==0){
			return false;
		}
		double fileSize = file.getSize();
		switch (unit) {
			case "K":{
				fileSize/=K;
				break;
			}
			case "M":{
				fileSize/=M;
				break;
			}
			case "G":{
				fileSize/=G;
				break;
			}
			default:{
				return false;
			}
		}
		return fileSize<size;
	}

	private Result<String> fileDownLoad(HttpServletResponse response, @RequestParam("fileName")String path){
		File file = new File(path);
		if(!file.exists()){
			throw new ProjectException("文件不存在",ResponseCode.INVALID_PARAMETER);
		}
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("utf-8");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getName() );

		try(
				BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(file.toPath()))
		){
			byte[] bytes = new byte[1024];
			ServletOutputStream ops = response.getOutputStream();
			int i;
			while ((i=stream.read(bytes))!=-1){
				ops.write(bytes,0,i);
				ops.flush();
			}
		}catch (IOException e){
			log.error("{}",e);
			throw new ProjectException("download error",ResponseCode.DEFAULT_ERROR);
		}
		return Result.ok("success");
	}
}
