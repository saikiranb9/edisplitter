package com.atd.microservices.core.edisplitter.webclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.edisplitter.domain.EDIData;
import com.atd.microservices.core.edisplitter.exception.EDISplitterException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class EDIAnalyticsDataClient {

	@Autowired
	private WebClient webClient;
	
	@Value("${spring.application.name}")
	private String applicationName;	
	
	@Value("${edisplitter.ediAnalyticsDataUrl}")
	private String ediAnalyticsDataUrl;

	public Mono<EDIData> saveEDIData(Mono<EDIData> ediData) {
		try { 
			return webClient.put()
				.uri(ediAnalyticsDataUrl)
				.header("XATOM-CLIENTID", applicationName)
				.body(ediData, EDIData.class)
				.retrieve()
				.onStatus(HttpStatus::isError, exceptionFunction -> Mono.error(new EDISplitterException(
						"EDIAnalytics Data Service Save API returned Error")))
				.bodyToMono(EDIData.class);
		} catch (Exception e) {
			log.error("Error while invoking EDIAnalytics Data Service Save API", e);
			return Mono.error(new EDISplitterException(
					"Error while invoking EDIAnalytics Data Service Save API", e));
		}
	}

}