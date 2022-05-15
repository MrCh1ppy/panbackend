package com.example.panbackend.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.example.panbackend.dao.jpa.UserDao;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileTreeDTO;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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

	private Result<String> doUpload(MultipartFile file,Path path){
		String fileName = file.getOriginalFilename();
		log.info("传输文件到：{}",path);
		if(fileName==null){
			return Result.fail(ResponseCode.DEFAULT_ERROR,"文件名为空");
		}
		File dest = path.resolve(fileName).toFile();
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
		return Result.ok("成功传输，路径为"+ path);
	}

	@Override
	public Result<String> upload(FileUploadParam param,String divide) {
		if (checkSize(param.getFile(),param.getSizeLimit(),param.getSizeUnit())){
			return Result.fail(ResponseCode.LOGIC_ERROR,"文件超过大小");
		}
		Path path = pathBuilder(param.getPath(), param.getUserID(),divide);
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

	private Result<String>doDownLoad(HttpServletResponse response,Path path){
		log.info("download file:{}",path);
		File file = path.toFile();
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
	public Result<String> fileDownLoad(HttpServletResponse response,String path,int userId,String divide)  {
		Path tempPath = pathBuilder(path, userId,divide);
		return doDownLoad(response, tempPath);
	}

	@Override
	public Result<List<FileDTO>> listPath(String path, int userID,String divide) {
		Path finalPath = pathBuilder(path, userID,divide);
		Optional<List<FileDTO>> queryRes = doList(finalPath);
		return queryRes.map(Result::ok).orElseGet(() -> Result.fail(ResponseCode.INVALID_PARAMETER, "非文件夹目录或其他错误"));
	}

	@Override
	public Result<FileTreeDTO> getFileTree(String paramPath, int userID,String divide) {
		Path path = pathBuilder(paramPath, userID,divide);
		Optional<FileTreeDTO> res = doGetFileTree(path);
		return res.map(Result::ok).orElseGet(() -> Result.fail(ResponseCode.LOGIC_ERROR, "获取树失败"));
	}

	private Optional<FileTreeDTO> doGetFileTree(Path paramPath){
		log.info("打印目录为{}的文件树",paramPath);
		File rootFile = paramPath.toFile();
		if (!rootFile.exists()){
			return Optional.empty();
		}

		ArrayDeque<File> deque = new ArrayDeque<>();
		HashMap<File, FileTreeDTO> map = new HashMap<>();

		deque.push(rootFile);
		FileTreeDTO root = new FileTreeDTO(panFileUtils.getFileDTO(rootFile), new ArrayList<>());
		map.put(rootFile,root);

		while (!deque.isEmpty()){

			File temp = deque.poll();
			FileTreeDTO treeDTO = map.remove(temp);

			File[] listFiles = temp.listFiles();
			File[] files = listFiles==null?new File[0]:listFiles;

			for (File cur : files) {
				FileTreeDTO node = new FileTreeDTO(panFileUtils.getFileDTO(cur), new ArrayList<>());
				treeDTO.getFileTreeDTOList().add(node);
				if(cur.isDirectory()){
					deque.push(cur);
					map.put(cur,node);
				}
			}
		}
		return Optional.of(root);
	}

	private Optional<List<FileDTO>> doList(Path path){
		File file = path.toFile();
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


	@Override
	public Result<String> fileDelete(String path,int userId,String divide) {
		Path rootPath = pathBuilder(path,userId,divide);
		log.info("delete{}",rootPath);
		try {
			Files.walkFileTree(rootPath,new SimpleFileVisitor<Path>(){

				/**
				 * Invoked for a file in a directory.
				 *
				 * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
				 * CONTINUE}.
				 *
				 * @param file 查看的对象
				 * @param attrs 无用
				 */
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return super.visitFile(file, attrs);
				}

				/**
				 * Invoked for a directory after entries in the directory, and all of their
				 * descendants, have been visited.
				 *
				 * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
				 * CONTINUE} if the directory iteration completes without an I/O exception;
				 * otherwise this method re-throws the I/O exception that caused the iteration
				 * of the directory to terminate prematurely.
				 *
				 * @param dir 目录Path
				 * @param exc 无用
				 */
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return super.postVisitDirectory(dir, exc);
				}
			});
			return Result.ok("删除成功");
		} catch (IOException e) {
			e.printStackTrace();
			return Result.ok("删除失败");
		}
	}

	@Override
	public Result<String> shareFile(String path, int id,String divide) {
		File file = Paths.get(path).toFile();
// TODO: 2022.5.15
		return null;
	}

	private Path pathBuilder(String path,int userID,String divide){
		Optional<User> user = userDao.findById(userID);
		if(!user.isPresent()){
			throw new ProjectException("无对应用户",ResponseCode.LOGIC_ERROR);
		}
		String[] split = path.split(divide);
		String[] param = ArrayUtil.sub(split, 1, split.length);
		return projectConst
				.getPrePath()
				.resolve(Integer.toString(userID)).resolve(Paths.get(split[0], param));
	}


}
