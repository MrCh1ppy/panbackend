package com.example.panbackend.converter;

import com.example.panbackend.entity.po.HistoryPo;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


/**
 *  history type converter
 * 转换器用于自动将JPA不支持类型与支持类型进行转化
 */
@Converter(autoApply = true)
public class HistoryTypeConverter implements AttributeConverter<HistoryPo.HistoryType,String> {
	@Override
	public String convertToDatabaseColumn(HistoryPo.HistoryType historyType) {
		return historyType.toString();
	}

	@Override
	public HistoryPo.HistoryType convertToEntityAttribute(String s) {
		return HistoryPo.HistoryType.valueOf(s);
	}
}
