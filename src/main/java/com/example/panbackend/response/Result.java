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
}


