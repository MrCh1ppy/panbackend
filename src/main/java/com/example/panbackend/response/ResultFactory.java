package com.example.panbackend.response;

public class ResultFactory<T> {

	private ResultFactory() {
	}

	Result<T> get(ResponseCode code, T data, String msg){
		return new Result<>(data, msg, code.getCode());
	}

	Result<T> ok(T data){
		return get(ResponseCode.SUCCESS,data,"success");
	}

	Result<T> fail(ResponseCode code,T data,String msg){
		return get(code,data,msg);
	}
}
