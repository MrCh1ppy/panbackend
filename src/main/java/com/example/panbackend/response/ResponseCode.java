package com.example.panbackend.response;

public enum ResponseCode {
	SUCCESS(200),
	NO_RAUTH(401),
	NOT_FOUND(404),
	DEFAULT_ERROR(403),
	INVALID_PARAMETER(402)
	;

	final int code;

	ResponseCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
