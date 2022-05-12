package com.example.panbackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemConstant {
	@Value("file.store_path")
	String STORE_PRE_PATH="";
}
