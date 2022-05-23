package com.example.panbackend.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 *  history type converter
 * 转换器用于自动将JPA不支持类型与支持类型进行转化
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {
	@Override
	public Date convertToDatabaseColumn(LocalDateTime localDateTime) {
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

	@Override
	public LocalDateTime convertToEntityAttribute(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
	}
}
