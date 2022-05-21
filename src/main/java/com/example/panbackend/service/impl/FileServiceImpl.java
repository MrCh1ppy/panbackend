package com.example.panbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.util.ArrayUtil;
import com.example.panbackend.dao.jpa.HistoryDao;
import com.example.panbackend.dao.jpa.UserDao;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileTreeDTO;
import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.entity.po.HistoryPo;
import com.example.panbackend.entity.po.UserPo;
import com.example.panbackend.exception.ProjectException;
import com.example.panbackend.response.ResponseCode;
import com.example.panbackend.response.Result;
import com.example.panbackend.service.FileService;
import com.example.panbackend.utils.Const;
import com.example.panbackend.utils.PanFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.panbackend.utils.Const.*;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

	public static final String NOT_EXIST = "文件不存在";
	private final UserDao userDao;

	private final StringRedisTemplate stringRedisTemplate;

	private final HistoryDao historyDao;

	@Autowired
	public FileServiceImpl(UserDao userDao, StringRedisTemplate stringRedisTemplate, HistoryDao historyDao) {
		this.userDao = userDao;
		this.stringRedisTemplate = stringRedisTemplate;
		this.historyDao = historyDao;
	}


	private Result<String> doUpload(MultipartFile file, Path path){
		String fileName = file.getOriginalFilename();
		log.info("传输文件到：{}",path);
		if(fileName==null){
			return Result.fail(ResponseCode.DEFAULT_ERROR,"文件名为空");
		}
		if(!path.toFile().isDirectory()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"非文件夹名或文件不存在");
		}
		File dest = path.resolve(fileName).toFile();
		try{
			log.info("try upload {}",dest.getAbsolutePath());
			file.transferTo(dest);
		}catch (IOException e){
			log.warn("文件传输错误");
			e.printStackTrace();
			return Result.fail(ResponseCode.DEFAULT_ERROR,"程序错误，请重上传");
		}
		return Result.ok("成功传输，路径为"+ path);
	}

	@Override
	public Result<String> upload(FileUploadParam param,String divide) {
		if (checkSize(param.getFile(),param.getSizeLimit(),param.getSizeUnit())){
			return Result.fail(ResponseCode.LOGIC_ERROR,"文件超过大小");
		}
		Path path = pathBuilder(param.getPath(), param.getUserID(),divide);
		Result<String> upload = doUpload(param.getFile(), path);
		if(upload.getCode()==200){
			File file = path.toFile();
			HistoryPo po = HistoryPo.getInstance(
					FileUtil.getType(file),
					file.getName(),
					PanFileUtils.getDataSize(FileUtil.size(file)),
					HistoryPo.HistoryType.UPLOAD
			);
			historyDao.save(po);
		}
		return upload;
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
		return fileSize>size;
	}

	private Result<String>doDownLoad(HttpServletResponse response,Path path){
		log.info("download file:{}",path);
		File file = path.toFile();
		if(!file.exists()){
			return Result.fail(ResponseCode.NOT_FOUND,NOT_EXIST);
		}
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("utf-8");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
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
			log.error("{} check",e.toString());
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
		Result<String> downLoad = doDownLoad(response, tempPath);
		if (downLoad==null) {
			File file = tempPath.toFile();
			HistoryPo po = HistoryPo.getInstance(
					FileUtil.getType(file),
					file.getName(),
					PanFileUtils.getDataSize(FileUtil.size(file)),
					HistoryPo.HistoryType.DOWNLOAD
			);
			historyDao.save(po);
		}
		return downLoad;
	}

	@Override
	public Result<List<FileDTO>> listPath(String path, int userID,String divide) {
		Path finalPath = pathBuilder(path, userID,divide);
		Optional<List<FileDTO>> queryRes = doList(finalPath);
		return queryRes.map(Result::ok).orElseGet(() -> Result.fail(ResponseCode.INVALID_PARAMETER, "非文件夹目录或其他错误"));
	}

	/**
	 * get file tree
	 *获得文件树
	 * @param paramPath paramPath
	 * @param userID userID
	 * @param divide divide
	 * @return {@link Result}
	 * @see Result
	 * @see FileTreeDTO
	 */
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
		FileTreeDTO root = new FileTreeDTO(PanFileUtils.getFileDTO(rootFile), new ArrayList<>());
		map.put(rootFile,root);

		while (!deque.isEmpty()){

			File temp = deque.poll();
			FileTreeDTO treeDTO = map.remove(temp);

			File[] listFiles = temp.listFiles();
			File[] files = listFiles==null?new File[0]:listFiles;

			for (File cur : files) {
				FileTreeDTO node = new FileTreeDTO(PanFileUtils.getFileDTO(cur), new ArrayList<>());
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
			res.add(PanFileUtils.getFileDTO(cur));
		}
		return Optional.of(res);
	}


	/**
	 * file delete
	 *递归删除文件
	 * @param path 路径
	 * @param userId 用户ID
	 * @param divide 分隔符
	 * @return {@link Result}
	 * @see Result
	 * @see String
	 */
	@Override
	public Result<String> fileDelete(String path,int userId,String divide) {
		Path rootPath = pathBuilder(path,userId,divide);
		log.info("delete{}",rootPath);
		try {
			Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {

				/**
				 * Invoked for a file in a directory.
				 *
				 * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
				 * CONTINUE}.
				 *
				 * @param file  查看的对象
				 * @param attrs 无用
				 */
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					int i = file.hashCode();
					//防止文件删除了，分享码依旧存在
					String key = stringRedisTemplate
							.opsForValue()
							.get( + i);
					if (key != null) {
						stringRedisTemplate.delete(key);
					}
					stringRedisTemplate.delete(Const.FILE_TO_KEY + i);
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


	/**
	 * share file
	 *
	 * @param stringPath stringPath
	 * @param id id
	 * @param divide divide
	 * @param leftMin num
	 * @return {@link Result}
	 * @see Result
	 * @see String
	 */
	@Override
	public Result<String> shareFile(String stringPath, int id,String divide,int leftMin,int numOfShare) {
		Path path = pathBuilder(stringPath, id, divide);
		File file = path.toFile();
		if(!file.exists()){
			return Result.fail(ResponseCode.LOGIC_ERROR, "文件不存在或已被删除");
		}
		String share = doFileShare(file, Duration.of(leftMin, ChronoUnit.MINUTES), Integer.MAX_VALUE);
		HistoryPo po = HistoryPo.getInstance(
				FileUtil.getType(file),
				file.getName(),
				PanFileUtils.getDataSize(FileUtil.size(file)),
				HistoryPo.HistoryType.DOWNLOAD
		);
		historyDao.save(po);
		return Result.ok(share);
	}


	/**
	 * do file share
	 *分享文件
	 * @param file file
	 * @param duration key保留时间
	 * @param receiveTime 文件可以接收的次数
	 * @return {@link String}
	 * @see String
	 */
	private String doFileShare(File file,Duration duration,int receiveTime){
		String fileToKeyKey = FILE_TO_KEY + file.hashCode();
		//获取hash的key
		String hashMapKey = stringRedisTemplate.opsForValue().get(fileToKeyKey);
		if(hashMapKey==null){
			//生成key
			String key = NanoId.randomNanoId(8);
			stringRedisTemplate.opsForValue().set(FILE_TO_KEY+file.hashCode(),key,duration);
			//构建文件信息
			HashMap<String, String> fileInfo = new HashMap<>();
			fileInfo.put(FILE_PATH,file.getAbsolutePath());
			fileInfo.put(FILE_TIME, Integer.toString(receiveTime));
			//提交
			stringRedisTemplate.opsForHash().putAll(KEY_TO_FILE+key,fileInfo);
			stringRedisTemplate.expire(FILE_TO_KEY, duration);
			//返回取件码
			return key;
		}
		//为文件到哈希的key进行续命
		stringRedisTemplate.expire(fileToKeyKey,duration);
		stringRedisTemplate.expire(KEY_TO_FILE+hashMapKey,duration);
		//保证重设可分享的次数
		stringRedisTemplate.opsForHash().put(KEY_TO_FILE+hashMapKey,FILE_TIME,Integer.toString(receiveTime));
		return hashMapKey;
	}

	/**
	 * receive file
	 *通过code接收文件
	 * @param response response
	 * @param code code
	 * @return {@link Result}
	 * @see Result
	 * @see String
	 */
	@Override
	public Result<String> receiveFile(HttpServletResponse response, String code) {
		String numText = (String) stringRedisTemplate.opsForHash().get(KEY_TO_FILE + code, FILE_TIME);
		if(numText==null||numText.isBlank()){
			return Result.fail(ResponseCode.NOT_FOUND,"文件已删除或分享超时");
		}
		int receiveTime = Integer.parseInt(numText);
		//寻找对应路径
		String path = (String) stringRedisTemplate.opsForHash().get(KEY_TO_FILE + code, FILE_PATH);
		log.info("接收文件的路径为{}",path);
		if(path==null){
			log.error("redis 分享文件一致性出错");
			stringRedisTemplate.delete(KEY_TO_FILE+code);
			return Result.fail(ResponseCode.DEFAULT_ERROR,"服务器内部错误");
		}
		Path filePath = Path.of(path);
		//防止高并发访问下击穿
		if(receiveTime<=0){
			stringRedisTemplate.delete(FILE_TO_KEY+ filePath.toFile().hashCode());
			stringRedisTemplate.delete(KEY_TO_FILE+code);
			return Result.fail(ResponseCode.LOGIC_ERROR, "文件分享次数已达上限");
		}
		receiveTime-=1;
		stringRedisTemplate.opsForHash().put(KEY_TO_FILE+code,FILE_TIME,Integer.toString(receiveTime));
		return doDownLoad(response, filePath);
	}

	/**
	 * path builder
	 *通过路径获得Path辅助类
	 * @param path path
	 * @param userID userID
	 * @param divide divide
	 * @return {@link Path}
	 * @see Path
	 */
	private Path pathBuilder(String path,int userID,String divide){
		Optional<UserPo> user = userDao.findById(userID);
		if(user.isEmpty()){
			throw new ProjectException("无对应用户",ResponseCode.LOGIC_ERROR);
		}
		String[] split = path.split(divide);
		String[] param = ArrayUtil.sub(split, 1, split.length);
		return PRE_PATH.resolve(Integer.toString(userID)).resolve(Path.of(split[0], param));
	}

	@Override
	public Result<String> shareAirDrop(MultipartFile multipartFile,int numOfShare){
		Path path = PRE_PATH.resolve(Integer.toString(AIR_DROP_USER_ID)).resolve(multipartFile.getName());
		long size = multipartFile.getSize();
		if(size>AIR_DROP_SIZE_LIMIT){
			return Result.fail(ResponseCode.INVALID_PARAMETER,"文件太大了♂,最大50MB");
		}
		Result<String> result = doUpload(multipartFile, path);
		if (result.getCode()!=200){
			return Result.fail(ResponseCode.DEFAULT_ERROR,"文件上传失败，无法空投");
		}
		File file = path.toFile();
		String shareCode = doFileShare(file, Duration.of(AIR_DROP_TTL, SHARE_TIME_UNIT),numOfShare);
		return Result.ok(shareCode);
	}

	@Override
	public Result<String> copyFile(int userID, String path, String divide) {
		Path filePath = pathBuilder(path, userID, divide);
		File baseFile = filePath.toFile();
		if(!baseFile.exists()){
			return Result.fail(ResponseCode.INVALID_PARAMETER, NOT_EXIST);
		}
		Path parent = filePath.getParent();
		String text = baseFile.getName();
		int repeat=1;
		StringBuilder baseName = new StringBuilder(text.substring(0, text.lastIndexOf(".")));
		File targetFile;

		while (true){
			String fileName = baseName.append("(")
					.append(repeat).append(")")
					.append(text.substring(text.lastIndexOf("."))).toString();
			File destFile = parent.resolve(fileName).toFile();
			if(!destFile.exists()){
				targetFile=destFile;
				break;
			}
			repeat+=1;
		}
		try {
			doFileCopy(baseFile,targetFile);
			return Result.ok("ok");
		} catch (IOException e) {
			e.printStackTrace();
			return Result.fail(ResponseCode.DEFAULT_ERROR,"文件复制错误");
		}
	}

	@Override
	public Result<String> renameFile(int userID, String path, String name, String divide) {
		Path filePath = pathBuilder(path, userID, divide);
		File file = filePath.toFile();
		if(!file.exists()){
			return Result.fail(ResponseCode.INVALID_PARAMETER,NOT_EXIST);
		}
		File targetFile = filePath.getParent().resolve(name).toFile();
		if(targetFile.exists()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"文件名已存在");
		}
		boolean isOK = file.renameTo(targetFile);
		if(isOK){
			return Result.ok("改名成功");
		}
		return Result.fail(ResponseCode.DEFAULT_ERROR,"改名失败");
	}

	@Override
	public Result<String> moveFile(int userID, String path, String divide, String targetPath) {
		Path filePath = pathBuilder(path, userID, divide);
		File file = filePath.toFile();
		if(!file.exists()){
			return Result.fail(ResponseCode.LOGIC_ERROR,NOT_EXIST);
		}
		Path targetFilePath = pathBuilder(targetPath, userID, divide);
		File targetFile = targetFilePath.toFile();
		if(!targetFile.exists()||!targetFile.isDirectory()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"目标路径非文件夹或不存在");
		}
		if (!file.renameTo(targetFile)) {
			return Result.fail(ResponseCode.DEFAULT_ERROR,"移动失败");
		}
		return Result.ok("移动成功");
	}

	@Override
	public Result<String> createDirectory(int userID, String path, String divide, String dName) {
		Path target = pathBuilder(path, userID, divide);
		File file = target.toFile();
		if(!file.isDirectory()){
			return Result.fail(ResponseCode.INVALID_PARAMETER,"该目录不存在或非法");
		}
		var dest = target.resolve(dName).toFile();
		if(dest.exists()){
			return Result.fail(ResponseCode.INVALID_PARAMETER,"该目录已存在");
		}
		try {
			Files.createDirectory(dest.toPath());
		} catch (IOException e) {
			return Result.fail(ResponseCode.DEFAULT_ERROR,e.getMessage());
		}
		return Result.ok("ok");
	}

	private void doFileCopy(File baseFile,File targetFile)throws IOException{
		try(
				FileInputStream baseStream = new FileInputStream(baseFile);
				FileOutputStream targetStream = new FileOutputStream(targetFile)
		){
			FileChannel baseChannel = baseStream.getChannel();
			FileChannel targetChannel = targetStream.getChannel();
			targetChannel.transferFrom(baseChannel,0,baseChannel.size());
		}
	}

	@Scheduled(fixedDelay = 120*1000)
	private void freshAirDrop() throws IOException {
		File file = PRE_PATH.resolve(Integer.toString(AIR_DROP_USER_ID)).toFile();
		if(!file.exists()){
			Files.createDirectories(file.toPath());
		}
		File[] files=file.listFiles();
		files=files==null?new File[0]:files;
		for (File cur : files) {
			int code = cur.hashCode();
			String res = stringRedisTemplate.opsForValue().get(FILE_TO_KEY + code);
			if(res==null){
				Files.deleteIfExists(cur.toPath());
			}
		}
	}


}
