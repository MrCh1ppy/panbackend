package com.example.panbackend.response;

/**
 *  result
 *
 */
public final class Result<T> {
	final T value;
	final String msg;
	final int code;

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
	public static <T>Result<T> fail(ResponseCode code,String msg){
		return get(code,null,msg);
	}


	public T getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

	public int getCode() {
		return code;
	}

	public boolean isSuccess(){
		return this.getCode()==ResponseCode.SUCCESS.getCode();
	}
}


