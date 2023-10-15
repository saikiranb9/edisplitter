package com.atd.microservices.core.edisplitter.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atd.microservices.core.edisplitter.exception.EDISplitterException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EDIJsonHeaderUtil {

	public static String KEY_CUSTOMERCODE = "customercode";
	public static String KEY_STANDARD = "standard";
	public static String KEY_VERSION = "version";
	public static String KEY_TYPE = "type";
	private static Pattern pattern = Pattern.compile("(^\\h*)|(\\h*$)");

	@Autowired
	private ObjectMapper objectMapper;

	public Map<String, String> extractHeaderInfoFromEDIDoc(String processedEDIJson) {
		String type, version, standard, customerCode = null;
		try {
			JsonNode rootNode = objectMapper.readTree(processedEDIJson);

			// Type
			JsonNode typeNode = rootNode
					.at("/interchanges/0/functional_groups/0/transactions/0/ST_01_TransactionSetIdentifierCode");
			if (typeNode != null && !typeNode.isMissingNode()) {
				type = trim(typeNode.textValue());
			} else {
				throw new EDISplitterException(
						"Type information(ST_01_TransactionSetIdentifierCode) not found in EDI data");
			}

			// Customer Code
			JsonNode customerCodeNode = rootNode.at("/interchanges/0/ISA_06_SenderId");
			String prefix = trim(rootNode.at("/interchanges/0/ISA_05_SenderQualifier").textValue());

			if (customerCodeNode != null && !customerCodeNode.isMissingNode()) {
				customerCode = prefix + "-" + trim(customerCodeNode.textValue());
			} else {
				throw new EDISplitterException(
						"Type information(ST_01_TransactionSetIdentifierCode) not found in EDI data");
			}

			// Version
			JsonNode versionNode = rootNode.at("/interchanges/0/functional_groups/0/GS_08_Version");
			if (versionNode != null && !versionNode.isMissingNode()) {
				version = trim(versionNode.textValue());
			} else {
				throw new EDISplitterException("Version information(GS_08_Version) not found in EDI data");
			}

			// Standard
			JsonNode standardNode = rootNode.at("/interchanges/0/functional_groups/0/GS_07_ResponsibleAgencyCode");
			if (standardNode != null && !standardNode.isMissingNode()) {
				String value = standardNode.textValue();
				standard = StringUtils.equals("X", trim(value)) ? "X12" : trim(value);
			} else {
				throw new EDISplitterException(
						"Standard information(GS_07_ResponsibleAgencyCode) not found in EDI data");
			}
		} catch (JsonProcessingException e) {
			throw new EDISplitterException("Error while extracting header values from EDI Json", e);
		}
		Map<String, String> headers = new HashMap<>();
		headers.put(KEY_CUSTOMERCODE, customerCode);
		headers.put(KEY_STANDARD, standard);
		headers.put(KEY_VERSION, version);
		headers.put(KEY_TYPE, type);
		return headers;
	}

	private static String trim(String str) {
		return pattern.matcher(str).replaceAll("");
	}

}
