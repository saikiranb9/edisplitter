package com.atd.microservices.core.edisplitter.domain.serdes;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

	public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	@Override
	public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		try {
			String s = value.format(DATE_TIME_FORMATTER);
			gen.writeString(s);
		} catch (Exception e) {
			log.error("Error serializing date field", e.getLocalizedMessage());
			gen.writeNull();
		}
	}

}
