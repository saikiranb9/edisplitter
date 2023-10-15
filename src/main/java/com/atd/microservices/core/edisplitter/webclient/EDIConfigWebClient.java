package com.atd.microservices.core.edisplitter.webclient;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.edisplitter.domain.EDIConfig;
import com.atd.microservices.core.edisplitter.exception.EDISplitterException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class EDIConfigWebClient {

	@Autowired
	private WebClient webClient;
	
	@Value("${spring.application.name}")
	private String applicationName;
	
	@Value("${apigateway.url}")
	private String msaDomainUrl;
	
	@Value("${edisplitter.ediConfigUrl}")
	private String ediConfigUrl;
	
	public Mono<EDIConfig> getEdiConfigByPartnerName(String partnerName) {
		try { 
			return webClient.get()
				.uri(ediConfigUrl+partnerName)
				.headers(headers -> {
					headers.set("XATOM-CLIENTID", applicationName);
				})
				.retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono.error(new EDISplitterException(new Date(), "EDI Config returned no data")))
				.bodyToMono(EDIConfig.class);
		} catch (Exception e) {
			log.error("Error while querying EDI Config", e);
			return Mono.error(
					new EDISplitterException("Error while querying EDI Config", e.getCause()));
		}
	}
}
