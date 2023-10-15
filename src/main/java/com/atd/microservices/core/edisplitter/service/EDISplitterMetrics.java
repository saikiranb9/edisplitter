package com.atd.microservices.core.edisplitter.service;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class EDISplitterMetrics {

	public static String METRIC_TOTAL_INCOMING_EDI = "edisplitter_total_incoming_edi_docs";
	public static String METRIC_TOTAL_SPLITTED_EDI = "edisplitter_total_splitted_edi_docs";
	public static String METRIC_TOTAL_FAILURE_IN_SPLITTER = "edisplitter_total_failure_in_splitter";

	MeterRegistry meterRegistry;

	public EDISplitterMetrics(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void increaseTotalIncomingMsgCount() {
		this.meterRegistry.counter(METRIC_TOTAL_INCOMING_EDI).increment();
	}

	public void increaseTotalSplittedMsgCount(double numberOfEdiDocs) {
		this.meterRegistry.counter(METRIC_TOTAL_SPLITTED_EDI).increment(numberOfEdiDocs);
	}

	public void increaseTotalTypeMsgCount(String type) {
		this.meterRegistry.counter(String.format(("edisplitter_total_incoming_%s_edi_docs"), type)).increment();
	}

	public void increaseTotalFailureCount() {
		this.meterRegistry.counter(METRIC_TOTAL_FAILURE_IN_SPLITTER).increment();
	}

}
