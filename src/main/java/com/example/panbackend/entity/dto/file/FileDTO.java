package com.example.panbackend.entity.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileDTO {
	private final String name;
	private final String path;
	private final double size;
	private final String sizeUnit;
	private final String type;

}

