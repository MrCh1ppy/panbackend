package com.example.panbackend.response;

import lombok.Data;

/**
 *  result
 *
 */
@Data
public final class Result<T> {
	T value;
	String msg;
	int code;

	public Result(T value, String msg, int code) {
		this.value = value;
		this.msg = msg;
		this.code = code;
	}


	/**
	 * get
	 *
	 * @param code 返回码
	 * @param data 数据
	 * @param msg 信息
	 * @return {@link Result}
	 * @see Result
	 * @see T
	 * 一般不使用，作为基本方法
	 */
	private static <T>Result<T> get(ResponseCode code, T data, String msg){
		return new Result<>(data, msg, code.getCode());
	}

	/**
	 * ok
	 *返回成功的数据
	 * @param data 数据
	 * @return {@link Result}
	 * @see Result
	 * @see T
	 */
	public static <T>Result<T> ok(T data){
		return get(ResponseCode.SUCCESS,data,"success");
	}

	/**
	 * ok
	 * 返回成功的数据
	 * @param data data
	 * @param msg msg
	 * @return {@link Result}
	 * @see Result
	 * @see T
	 */
	public static <T>Result<T> ok(T data,String msg){
		return get(ResponseCode.SUCCESS,data,msg);
	}

	/**
	 * fail
	 *
	 * @param code 返回码
	 * @param position 错误位置
	 * @param msg 附带消息
	 * @return {@link Result}
	 * @see Result
	 * 返回失败的数据
	 */
	public static Result<String> error(ResponseCode code, String position, String msg){
		return get(code,position,msg);
	}


	/**
	 * fail
	 *基础错误类型
	 * @param code code
	 * @param msg msg
	 * @return {@link Result}
	 * @see Result
	 * @see String
	 */
	public static Result<String> fail(ResponseCode code,String msg){
		return get(code,null,msg);
	}



}


