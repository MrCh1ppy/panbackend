package com.example.panbackend.service.impl;

import com.example.panbackend.dao.jpa.UserDao;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.entity.po.User;
import com.example.panbackend.exception.ProjectException;
import com.example.panbackend.response.ResponseCode;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import com.example.panbackend.utils.PanFileUtils;
import com.example.panbackend.utils.ProjectConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

	UserDao userDao;

	ProjectConst projectConst;
	PanFileUtils panFileUtils;

	@Autowired
	public FileServiceImpl setUserDao(UserDao userDao) {
		this.userDao = userDao;
		return this;
	}

	private Result<String> doUpload(MultipartFile file,String path){
		String fileName = file.getOriginalFilename();
		log.info("传输文件到：{}",path);
		File dest = new File(path + "// " + fileName);
		if(!dest.getParentFile().exists()&&!dest.getParentFile().mkdirs()){
			log.warn("文件夹未创建");
			return Result.fail(ResponseCode.DEFAULT_ERROR,"文件夹创建失败");
		}
		try{
			file.transferTo(dest);
		}catch (IOException e){
			log.warn("文件传输错误");
			Result.fail(ResponseCode.DEFAULT_ERROR,"程序错误，请重上传");
		}
		return Result.ok("成功传输，路径为"+path+ projectConst.getDivide()+fileName);
	}

	@Override
	public Result<String> upload(FileUploadParam param) {
		if (checkSize(param.getFile(),param.getSizeLimit(),param.getSizeUnit())){
			return Result.fail(ResponseCode.LOGIC_ERROR,"文件超过大小");
		}
		String path = pathHelper(param.getPath(), param.getUserID());
		return doUpload(param.getFile(), path);
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

	private Result<String>doDownLoad(HttpServletResponse response,String path){
		log.info("download file:{}",path);
		File file = new File(path);
		if(!file.exists()){
			return Result.fail(ResponseCode.NOT_FOUND,"此文件不存在");
		}
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("utf-8");
		response.setContentLength((int) file.length());
		try {
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("文件名格式化错误:" + path);
			return Result.fail(ResponseCode.INVALID_PARAMETER,"文件名格式化错误");
		}
		ServletOutputStream ops = null;
		try(
				BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(file.toPath()))
		){
			byte[] bytes = new byte[10240];
			ops = response.getOutputStream();
			int i;
			while ((i=stream.read(bytes))>0){
				ops.write(bytes,0,i);
				ops.flush();
			}
		}catch (IOException e){
			log.error("{} check",e);
			return Result.fail(ResponseCode.DEFAULT_ERROR,"下载时出现异常");
		}finally {
			try {
				if(ops!=null){
					ops.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Result.ok("success");
	}

	@Override
	public Result<String> fileDownLoad(HttpServletResponse response,String path,int userId)  {
		String tempPath = pathHelper(path, userId);
		return doDownLoad(response, tempPath);
	}

	@Override
	public Result<List<FileDTO>> listPath(String path, int userID) {
		String finalPath = pathHelper(path, userID);
		Optional<List<FileDTO>> queryRes = doList(finalPath);
		return queryRes.map(Result::ok).orElseGet(() -> Result.fail(ResponseCode.INVALID_PARAMETER, "非文件夹目录或其他错误"));
	}

	private Optional<List<FileDTO>> doList(String path){
		File file = new File(path);
		if(!file.isDirectory()){
			return Optional.empty();
		}
		File[] files = file.listFiles();
		if(files==null){
			return Optional.empty();
		}
		ArrayList<FileDTO> res = new ArrayList<>(files.length);
		for (File cur : files) {
			res.add(panFileUtils.getFileDTO(cur));
		}
		return Optional.of(res);
	}

	/**
	 * 拼接路径地址使用
	 * @param path 传输过来的路径
	 * @return 完整路径
	 */
	private String pathHelper(String path,int userID){
		Optional<User> user = userDao.findById(userID);
		if(!user.isPresent()){
			throw new ProjectException("无对应用户",ResponseCode.LOGIC_ERROR);
		}
		return projectConst.getPreStorePath() +
				projectConst.getDivide() + userID +
				projectConst.getDivide() + path;
	}

	@Autowired
	public FileServiceImpl setProjectConst(ProjectConst projectConst) {
		this.projectConst = projectConst;
		return this;
	}

	@Autowired
	public FileServiceImpl setPanFileUtils(PanFileUtils panFileUtils) {
		this.panFileUtils = panFileUtils;
		return this;
	}
}
