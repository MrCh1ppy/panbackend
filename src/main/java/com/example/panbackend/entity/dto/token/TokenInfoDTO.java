package com.example.panbackend.entity.dto.token;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenInfoDTO {
	private String tokenValue;
	private String tokenName;

	public static TokenInfoDTO getInstance(SaTokenInfo info){
		return new TokenInfoDTO(
				info.getTokenValue(),
				info.getTokenName()
				);
	}

}
