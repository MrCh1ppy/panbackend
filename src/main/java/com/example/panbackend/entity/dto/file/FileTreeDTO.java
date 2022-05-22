package com.example.panbackend.entity.dto.file;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class FileTreeDTO {
	private FileDTO fileDTO;
	private List<FileTreeDTO> fileTreeDTOList;

	public FileTreeDTO(FileDTO fileDTO, List<FileTreeDTO> fileTreeDTOList) {
		this.fileDTO = fileDTO;
		this.fileTreeDTOList = fileTreeDTOList;
	}
}
