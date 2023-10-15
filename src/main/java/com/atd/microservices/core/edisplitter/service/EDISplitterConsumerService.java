package com.atd.microservices.core.edisplitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EDISplitterConsumerService {

	@Autowired
	private EDIProcessor ediProcessor;
	
	@Value("${spring.application.name}")
	private String appName;
	
	@Autowired
	private EDISplitterMetrics ediSplitterMetrics;

	@KafkaListener(topics = "${edisplitter.kafka.topic.inbound}", groupId = "group_edisplitter", containerFactory = "kafkaListenerContainerFactory")
	public void analyticsMessageListener(@Payload String ediSourcePayload,
										 @Header("filenamex") String ediSourcePayloadHeader) {
		try {
			log.debug("Received message size: " + ediSourcePayload.length());
			ediSplitterMetrics.increaseTotalIncomingMsgCount();
			ediProcessor.process(ediSourcePayload, ediSourcePayloadHeader);			
		} catch (Exception e) {
			log.error("Failed in processing kakfa message", e);
			ediSplitterMetrics.increaseTotalFailureCount();
		}
	}
}
