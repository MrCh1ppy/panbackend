package com.example.panbackend.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ProjectConst {
	@Value("${const.store.pre_path}")
	private String preStorePath;
	@Value("${const.store.divide}")
	private String divide;
}
