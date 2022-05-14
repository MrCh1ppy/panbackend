package com.example.panbackend.entity.dto.file;

public class FileDTO {
	private final String name;
	private final String path;
	private final double size;
	private final String sizeUnit;
	private final String type;

	public FileDTO(String name, String path, double size, String sizeUnit, String type) {
		this.name = name;
		this.path = path;
		this.size = size;
		this.sizeUnit = sizeUnit;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
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

