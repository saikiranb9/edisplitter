package com.atd.microservices.core.edisplitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.edisplitter.service.util.EDIFileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class EDISplitterApplication {

	@Value("${spring.application.name}")
	private String appName;

	@Value("${env.host.url:#{null}}")
	private String envHostURL;
	
	@Autowired
	EDIFileUtils ediFileUtils;

	public static void main(String[] args) {
		SpringApplication.run(EDISplitterApplication.class, args);
	}

	/*
	 * @Bean public TraceableExecutorService getExecutorService(BeanFactory
	 * beanFactory) { return new TraceableExecutorService(beanFactory,
	 * Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
	 * "futureroutes"); }
	 */

	@Bean
	public WebClient defaultWebClient() {
		return WebClient.create();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
