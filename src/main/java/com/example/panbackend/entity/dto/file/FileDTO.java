package com.example.panbackend.entity.dto.file;

import java.nio.file.Path;

public class FileDTO {
	private final String name;
	private final Path path;
	private final double size;
	private final String sizeUnit;
	private final String type;

	public FileDTO(String name, Path path, double size, String sizeUnit, String type) {
		this.name = name;
		this.path = path;
		this.size = size;
		this.sizeUnit = sizeUnit;
		this.type = type;
	}


	public String getName() {
		return name;
	}

	/**
	 * get path
	 * 注意，此处有函数重载，原类型为nio.Path
	 *
	 * @return {@link String}
	 * @see String
	 */
	public String getPath() {
		return path.toString();
	}

	public double getSize() {
		return size;
	}

	public String getSizeUnit() {
		return sizeUnit;
	}

	public String getType() {
		return type;
	}
}

