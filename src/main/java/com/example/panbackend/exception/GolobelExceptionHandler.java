package com.example.panbackend.exception;

import com.example.panbackend.response.ResponseCode;
import com.example.panbackend.response.Result;
import com.sun.xml.txw2.output.ResultFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GolobelExceptionHandler {
	private String getErrorPosition(Exception e){
		if(e.getStackTrace().length>0){
			final StackTraceElement element = e.getStackTrace()[0];
			String fileName = element.getFileName() == null ? "error file not found" : element.getFileName();
			int lineNumber = element.getLineNumber();
			return fileName+":"+lineNumber;
		}
		return "";
	}

	private void loggerOutPut(String exceptionName,String exepitionPosition){
		String text = "/////ERROR/////" + "\nlocation"+exepitionPosition+"\nname"+exceptionName;
		log.error(text);
	}

	private Result<String>defaultHandler(Exception e, String msg, ResponseCode responseCode){
		String position = getErrorPosition(e);
		loggerOutPut(msg,position);
		return Result.fail(responseCode,msg,position);
	}

	@ExceptionHandler(value = Exception.class)
	Result<String> exceptionHandler(Exception e){
		String errorMsg = e.getMessage() != null ? e.getMessage() : "未知错误";
		return defaultHandler(e,errorMsg,ResponseCode.DEFAULT_ERROR);
	}

	@ExceptionHandler(value = ProjectException.class)
	Result<String> projectExceptionHandler(ProjectException e){
		return defaultHandler(e,e.getMessage(),e.getResponseCode());
	}
}
