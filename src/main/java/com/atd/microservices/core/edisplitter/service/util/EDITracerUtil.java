package com.atd.microservices.core.edisplitter.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import brave.Tracer;

@Component
public class EDITracerUtil {
	
	@Autowired
	private Tracer tracer;

	public String getTraceId() {
		String traceId = null;
		if (tracer != null) {
			traceId = tracer.currentSpan().context().traceIdString();
		}
		return traceId;
	}

}
