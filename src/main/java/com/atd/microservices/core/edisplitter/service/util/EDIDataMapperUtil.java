package com.atd.microservices.core.edisplitter.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atd.microservices.core.edisplitter.configuration.KafkaConfigConstants;
import com.atd.microservices.core.edisplitter.domain.Acknowledgement;
import com.atd.microservices.core.edisplitter.domain.EDIConfig;
import com.atd.microservices.core.edisplitter.domain.EDIData;

@Component
public class EDIDataMapperUtil {

	@Value("${spring.application.name}")
	private String appName;
	@Autowired
	KafkaConfigConstants kafkaConfigConstants;
	@Autowired
	EDITracerUtil tracerUtil;
	
	public EDIData mapEDIData(Acknowledgement acknowledgement, EDIConfig ediConfig) {
		EDIData ediData = new EDIData();
		ediData.setTraceId(tracerUtil.getTraceId());
		ediData.setRawData(acknowledgement.getAcknowlegement());
		ediData.setLastProcessStage(appName);
		ediData.setSourceTopic(kafkaConfigConstants.KAFKA_TOPIC_INBOUND);
		ediData.setStatus("2xx");
		ediData.setType("997");
		ediData.setStandard(acknowledgement.getStandard().equals("ANSI") ? "X12" : acknowledgement.getStandard());
		ediData.setVersion(acknowledgement.getVersion());
		ediData.setSendercode(ediConfig.getSenderCode());
		ediData.setReceivercode(ediConfig.getReceiverCode());
		return ediData;
	}
}
 


