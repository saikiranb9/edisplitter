package com.atd.microservices.core.edisplitter.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.atd.microservices.core.edisplitter.configuration.KafkaConfigConstants;
import com.atd.microservices.core.edisplitter.domain.Acknowledgement;
import com.atd.microservices.core.edisplitter.domain.EDIConfig;
import com.atd.microservices.core.edisplitter.domain.EDIReaderPayload;
import com.atd.microservices.core.edisplitter.exception.EDISplitterException;
import com.atd.microservices.core.edisplitter.webclient.EDIConfigWebClient;
import com.berryworks.edireader.splitter.EdiSplitter;
import com.berryworks.edireader.splitter.Splitter;
import com.berryworks.edireader.splitter.Splitter.Form;
import com.berryworks.edireader.splitter.SplitterResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableAsync
public class EDIProcessor {

	@Autowired
	private KafkaConfigConstants kafkaConfigConstants;

	@Autowired
	@Qualifier("ediReaderKafkaTemplate")
	private KafkaTemplate<String, EDIReaderPayload> ediReaderKafkaTemplate;

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private EDISplitterMetrics ediSplitterMetrics;

	@Autowired
	private EDIConfigWebClient ediConfigWebclient;

	@Value("${edisplitter.fusePath}")
	private String fusePath;

	@Autowired
	private EDIValidatorService ediValidatorService;

	public void process(String ediCombinedPayload, String header) {

		// Validate & Create 997 Acknowledgement
		Acknowledgement acknowledgement = null;
		try {
			acknowledgement = ediValidatorService.validate(ediCombinedPayload);
		} catch (Exception e) {
			throw new EDISplitterException("Validation Error", e);
		}

		// Split
		try (StringReader reader = new StringReader(ediCombinedPayload)) {
			Splitter splitter = new EdiSplitter();
			SplitterResult result = splitter.split(reader).selectForm(Form.EDI).toStrings();
			if (result != null && result.getInterchanges() != null) {
				ediSplitterMetrics.increaseTotalSplittedMsgCount(result.getInterchanges().size());
				// Lookup flag to see if 997 should be generated
				if (null != acknowledgement.getAcknowlegement()) {
					handle997(acknowledgement.getAcknowlegement(), result.getInterchanges().get(0), header);
				}
				for (String doc : result.getInterchanges()) {
					// Push to EDIREADER Kafka Topic
					EDIReaderPayload ediReaderPayload = new EDIReaderPayload();
					ediReaderPayload.setData(doc);
					ediReaderPayload.setTimestamp(ZonedDateTime.now());
					ediReaderKafkaTemplate.send(kafkaConfigConstants.KAFKA_TOPIC_OUTBOUND, ediReaderPayload);
				}
			}
		} catch (Exception e) {
			log.error("Error splitting the EDI data", e);
			throw new EDISplitterException("Error splitting the EDI data", e);
		}
	}

	@Async
	public void handle997(String acknowledgement, String doc, String header) {
		EDIConfig ediConfig = ediConfigWebclient.getEdiConfigByPartnerName(header).block();
		if (Boolean.parseBoolean(ediConfig.getGenerate997())) {
			// Push to GCS vis GCS Fuse
			String fileName = String.format(("%s/%s/997/997_%s_%s.edi"), fusePath, header, header.toUpperCase(),
					Instant.now().toEpochMilli());
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
				writer.write(acknowledgement);
				/*
				 * Files.write( Paths.get(fileName),
				 * acknowledgement.getBytes(StandardCharsets.UTF_8));
				 */
			} catch (Exception e) {
				throw new EDISplitterException("Error saving the ack file", e);
			}
		}
	}
}