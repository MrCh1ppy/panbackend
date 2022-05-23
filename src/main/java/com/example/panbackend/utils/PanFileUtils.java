package com.example.panbackend.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.digest.MD5;
import com.example.panbackend.entity.dto.file.FileDTO;
import com.example.panbackend.entity.dto.file.FileSizeDTO;

import java.io.File;
import java.nio.file.Path;

import static com.example.panbackend.utils.Const.PRE_PATH;
public final class PanFileUtils {
	private static final String[] sizeUnit={"Byte","KB","MB","GB","TB"};
	private static final int PRE_PATH_SIZE;

	private static final String[] thumbnailAbleArray=new String[]{"png","jpg","jpeg","gif"};

	//适合的时间进行池化
	private static final ThreadLocal<MD5> MD5_DEALER = ThreadLocal.withInitial(MD5::create);

	private PanFileUtils() {
	}

	static {
		PRE_PATH_SIZE =PRE_PATH.toString().length();
	}

	public static FileDTO getFileDTO(File file) {
		double size = FileUtil.size(file);
		FileSizeDTO sizeDTO = getDataSize(size);
		return new FileDTO(
				FileUtil.getName(file),
				file.toPath().toString().substring(PRE_PATH_SIZE),
				sizeDTO.getSize(),
				sizeDTO.getSizeUnit(),
				file.isDirectory() ? "directory" : FileUtil.getType(file)
		);
	}

	public static FileSizeDTO getDataSize(double bytesSize){
		double showSize = bytesSize;
		String showSizeUnit;
		short index = 0;
		while (true) {
			if (showSize < 1024) {
				showSizeUnit = sizeUnit[index];
				break;
			}
			index++;
			showSize /= 1024;
		}
		return new FileSizeDTO(
				showSize,
				showSizeUnit
		);
	}

	public static boolean thumbnailAble(File file){
		String type = FileUtil.getType(file);
		for (String cur : thumbnailAbleArray) {
			if (type.equals(cur)){
				return true;
			}
		}
		return false;
	}

	public static Path pathBuilder(Path base,String path,String divide,Integer userID){
		String[] split = path.split(divide);
		String[] next = ArrayUtil.sub(split, 1, split.length);
		Path res = base;
		if(userID!=null){
			res=res.resolve(userID.toString());
		}
		res=res.resolve(Path.of(split[0],next));
		return res;
	}

	/**
	 * get file hash code
	 *
	 * @param file file
	 * @return {@link int}
	 */
	public static String getFileUniqueCode(File file){
		return MD5_DEALER.get().digestHex16(file);
	}

	public static String getFileUniqueCode(Path path){
		return getFileUniqueCode(path.toFile());
	}

}
