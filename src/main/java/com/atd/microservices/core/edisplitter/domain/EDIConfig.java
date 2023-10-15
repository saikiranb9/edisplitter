package com.atd.microservices.core.edisplitter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EDIConfig {
    private String senderCode;
    private String receiverCode;
    private String generate997;
    private String partnerName;
}