package com.example.panbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.id.NanoId;
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
import com.example.panbackend.utils.PanFileUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
	public static final int BUFFER_SIZE = 1024 * 1024;
	public static final long BUFFER_CHOOSE_STANDER = DataSize.parse("100MB").toBytes();
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
		if(dest.exists()){
			return Result.fail(ResponseCode.LOGIC_ERROR,"同名文件已存在");
		}
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
		Path basePath = PRE_PATH.resolve(Integer.toString(param.getUserID()));
		if(!basePath.toFile().exists()){
			try {
				Files.createDirectory(basePath);
			} catch (IOException e) {
				return Result.fail(ResponseCode.LOGIC_ERROR,"创建基础路径失败");
			}
		}
		Path path = pathBuilder(param.getPath(), param.getUserID(),divide);
		Result<String> upload = doUpload(param.getFile(), path);
		if(upload.getCode()==200){
			File file = path.resolve(Objects.requireNonNull(param.getFile().getOriginalFilename())).toFile();
			getThumbnail(file);
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

	private void getThumbnail(File file) {
		if (!PanFileUtils.thumbnailAble(file)) {
			return;
		}
		String hashCode = PanFileUtils.getFileUniqueCode(file);
		String res = stringRedisTemplate.opsForValue().get(REDIS_THUMBNAIL + hashCode);
		if(res!=null){
			return;
		}
		var path = PanFileUtils.pathBuilder(THUMBNAIL_PRE_PATH, "", "", null);
		File parentFile = path.toFile();
		File targetFile = path.resolve(hashCode + "." + FileUtil.getType(file)).toFile();
		try {
			if (!parentFile.exists()){
				Path directories = Files.createDirectories(path);
				if(directories!=path){
					throw new ProjectException("创建目录失败",ResponseCode.DEFAULT_ERROR);
				}
			}
			Thumbnails.of(file)
					.size(200,200)
					.outputFormat("jpeg")
					.toFile(targetFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProjectException("缩略图生成失败",ResponseCode.DEFAULT_ERROR);
		}
		log.info("缩略图生成一个{}", path);
		stringRedisTemplate.opsForValue().set(REDIS_THUMBNAIL+PanFileUtils.getFileUniqueCode(file),path.toString());
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

	private Result<String> doDownLoad(HttpServletResponse response, Path path, String range){
		log.info("download file:{}",path);
		File file = path.toFile();
		if(!file.exists()){
			return Result.fail(ResponseCode.NOT_FOUND,NOT_EXIST);
		}
		long startByte=0;
		long endByte=file.length()-1;
		if(range!=null&&range.contains("bytes=")&&range.contains("-")){
			range=range.substring(range.lastIndexOf("=")+1).trim();
			String[] ranges = range.split("-");
			try{
				if(ranges.length==1){
					if(range.startsWith("-")){
						endByte=Long.parseLong(ranges[0]);
					}
					else if(range.endsWith("-")){
						startByte=Long.parseLong(ranges[0]);
					}
				}else if(ranges.length==2){
					startByte=Long.parseLong(ranges[0]);
					endByte=Long.parseLong(ranges[1]);
				}
			}catch (NumberFormatException e){
				startByte=0;
				endByte= file.length()-1;
			}
		}
		//要下载的长度
		long contentLength = endByte - startByte + 1;
		//文件名
		String fileName = file.getName();
		//文件类型
		String contentType = FileUtil.getType(file);
		//响应头设置
		//https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
		response.setHeader("Accept-Ranges", "bytes");
		//Content-Type 表示资源类型，如：文件类型
		response.setHeader("Content-Type", contentType);
		//Content-Disposition 表示响应内容以何种形式展示，是以内联的形式（即网页或者页面的一部分），还是以附件的形式下载并保存到本地。
		response.setHeader("Content-Disposition", "inline;filename="+fileName);
		//Content-Length 表示资源内容长度，即：文件大小
		response.setHeader("Content-Length", String.valueOf(contentLength));
		//Content-Range 表示响应了多少数据，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
		response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(contentType);
		//大文件使用直接内存的ByteBuffer减少缓冲时间
		ByteBuffer buffer=endByte-startByte>BUFFER_CHOOSE_STANDER?ByteBuffer.allocateDirect(BUFFER_SIZE):ByteBuffer.allocate(BUFFER_SIZE);
		downLoad(response, path, startByte,buffer);
		return Result.ok("");
	}

	private void downLoad(HttpServletResponse response, Path path, long startByte,ByteBuffer buffer) {
		try(
				BufferedOutputStream ops = new BufferedOutputStream(response.getOutputStream());
				RandomAccessFile targetFile = new RandomAccessFile(path.toFile(),"r")
		){
			if (buffer.hasArray()) {
				throw new ProjectException("缓存创建失败",ResponseCode.DEFAULT_ERROR);
			}
			targetFile.seek(startByte);
			FileChannel fileChannel = targetFile.getChannel();
			byte[] bufferArray = new byte[BUFFER_SIZE];
			while (true){
				int read = fileChannel.read(buffer);
				if(read==-1){
					break;
				}
				buffer.flip();
				buffer.get(bufferArray,0,buffer.capacity());
				ops.write(bufferArray);
				buffer.clear();
			}
			ops.flush();
		}catch (IOException e){
			e.printStackTrace();
			throw new ProjectException(e.getMessage(),ResponseCode.DEFAULT_ERROR);
		}
	}

	@Override
	public Result<String> fileDownLoad(HttpServletResponse response,String path,int userId,String divide,String range)  {
		Path tempPath = pathBuilder(path, userId,divide);
		Result<String> downLoad = doDownLoad(response, tempPath,range);
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
					var i = PanFileUtils.getFileUniqueCode(file);
					//防止文件删除了，分享码依旧存在
					String key = stringRedisTemplate
							.opsForValue()
							.getAndDelete(REDIS_FILE_TO_KEY + i);
					if (key != null) {
						stringRedisTemplate.delete(key);
					}
					//删除存留的缩略图
					String path = stringRedisTemplate
							.opsForValue()
							.getAndDelete(REDIS_THUMBNAIL + PanFileUtils.getFileUniqueCode(file));
					if(path!=null){
						Files.delete(Path.of(path));
					}
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
		String fileToKeyKey = REDIS_FILE_TO_KEY + PanFileUtils.getFileUniqueCode(file);
		//获取hash的key
		String hashMapKey = stringRedisTemplate.opsForValue().get(fileToKeyKey);
		if(hashMapKey==null){
			//生成key
			String key = NanoId.randomNanoId(8);
			stringRedisTemplate.opsForValue().set(REDIS_FILE_TO_KEY +PanFileUtils.getFileUniqueCode(file),key,duration);
			//构建文件信息
			HashMap<String, String> fileInfo = new HashMap<>();
			fileInfo.put(REDIS_FILE_PATH,file.getAbsolutePath());
			fileInfo.put(REDIS_FILE_TIME, Integer.toString(receiveTime));
			//提交
			stringRedisTemplate.opsForHash().putAll(REDIS_KEY_TO_FILE +key,fileInfo);
			stringRedisTemplate.expire(REDIS_FILE_TO_KEY, duration);
			//返回取件码
			return key;
		}
		//为文件到哈希的key进行续命
		stringRedisTemplate.expire(fileToKeyKey,duration);
		stringRedisTemplate.expire(REDIS_KEY_TO_FILE +hashMapKey,duration);
		//保证重设可分享的次数
		stringRedisTemplate.opsForHash().put(REDIS_KEY_TO_FILE +hashMapKey, REDIS_FILE_TIME,Integer.toString(receiveTime));
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
	public Result<String> receiveFile(HttpServletResponse response, String code,String range) {
		String numText = (String) stringRedisTemplate.opsForHash().get(REDIS_KEY_TO_FILE + code, REDIS_FILE_TIME);
		if(numText==null||numText.isBlank()){
			return Result.fail(ResponseCode.NOT_FOUND,"文件已删除或分享超时");
		}
		int receiveTime = Integer.parseInt(numText);
		//寻找对应路径
		String path = (String) stringRedisTemplate.opsForHash().get(REDIS_KEY_TO_FILE + code, REDIS_FILE_PATH);
		log.info("接收文件的路径为{}",path);
		if(path==null){
			log.error("redis 分享文件一致性出错");
			stringRedisTemplate.delete(REDIS_KEY_TO_FILE +code);
			return Result.fail(ResponseCode.DEFAULT_ERROR,"服务器内部错误");
		}
		Path filePath = Path.of(path);
		//防止高并发访问下击穿
		if(receiveTime<=0){
			stringRedisTemplate.delete(REDIS_FILE_TO_KEY + PanFileUtils.getFileUniqueCode(filePath));
			stringRedisTemplate.delete(REDIS_KEY_TO_FILE +code);
			return Result.fail(ResponseCode.LOGIC_ERROR, "文件分享次数已达上限");
		}
		receiveTime-=1;
		stringRedisTemplate.opsForHash().put(REDIS_KEY_TO_FILE +code, REDIS_FILE_TIME,Integer.toString(receiveTime));
		return doDownLoad(response, filePath,range);
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
		return PanFileUtils.pathBuilder(PRE_PATH, path, divide, userID);
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
			String res = stringRedisTemplate.opsForValue().get(REDIS_FILE_TO_KEY + PanFileUtils.getFileUniqueCode(cur));
			if(res==null){
				Files.deleteIfExists(cur.toPath());
			}
		}
	}


}
