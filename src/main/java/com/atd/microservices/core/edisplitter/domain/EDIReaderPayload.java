package com.atd.microservices.core.edisplitter.domain;

import java.time.ZonedDateTime;

import com.atd.microservices.core.edisplitter.domain.serdes.ZonedDateTimeDeserializer;
import com.atd.microservices.core.edisplitter.domain.serdes.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class EDIReaderPayload {

	private String data;
	@JsonDeserialize(using = ZonedDateTimeDeserializer.class)
	@JsonSerialize(using = ZonedDateTimeSerializer.class)
	private ZonedDateTime timestamp;

}
