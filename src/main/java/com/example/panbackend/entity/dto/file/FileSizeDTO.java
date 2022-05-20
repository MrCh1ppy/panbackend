package com.example.panbackend.entity.dto.file;


public final class FileSizeDTO {
	private final double size;
	private final String sizeUnit;


	public FileSizeDTO(double size, String sizeUnit) {
		this.size = size;
		this.sizeUnit = sizeUnit;
	}

	public double getSize() {
		return size;
	}

	public String getSizeUnit() {
		return sizeUnit;
	}
}
