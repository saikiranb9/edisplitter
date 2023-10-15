package com.atd.microservices.core.edisplitter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Acknowledgement {
	private String acknowlegement;
	private String docType;
	private String standard;
	private String version;
}