package com.example.panbackend.service;

import com.example.panbackend.entity.param.FileUploadParam;
import com.example.panbackend.response.Result;

public interface FileService {
	Result<String> upload(FileUploadParam param);
}
