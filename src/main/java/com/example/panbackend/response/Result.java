package com.example.panbackend.response;

public final class Result<T> {
	T value;
	String msg;
	int code;

	public Result(T value, String msg, int code) {
		this.value = value;
		this.msg = msg;
		this.code = code;
	}


	public static <T>Result<T> get(ResponseCode code, T data, String msg){
		return new Result<>(data, msg, code.getCode());
	}

	public static <T>Result<T> ok(T data){
		return get(ResponseCode.SUCCESS,data,"success");
	}

	public static <T>Result<T> fail(ResponseCode code,T data,String msg){
		return get(code,data,msg);
	}
}


