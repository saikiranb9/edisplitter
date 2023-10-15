package com.atd.microservices.core.edisplitter.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.atd.microservices.core.edisplitter.service.util.EDIFileUtils;
import com.berryworks.edimodel.serial.EdiModelDeserializer;
import com.berryworks.edireader.model.EdiModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EDIModelResolver {

	@Value("${edisplitter.fusePath}")
	private String fusePath;

	private Map<String, EdiModel> ediModelCache = new HashMap<>();

	@Autowired
	private EDISplitterMetrics ediSplitterMetrics;

	public EdiModel getEDIModel(String docType, String version) {
		String key = docType + "_" + version;
		if (ediModelCache.get(key) != null) {
			return ediModelCache.get(key);
		} else {
			return createModel(key, docType, version);
		}
	}

	private EdiModel createModel(String key, String docType, String version) {
		String yamlString = null;
		EdiModel model = new EdiModel();
		try {
			FileReader fileReader = new FileReader(getFileName(docType, version.substring(2)));
			yamlString = FileCopyUtils.copyToString(fileReader);
		} catch (Exception e) {
			if (e instanceof FileNotFoundException) {
				log.warn("MODEL NOT FOUND : {}{}***VALIDATION SKIPPED***NO ACKNOWLEDGEMENT WILL BE GENERATED FOR {}", e.getMessage(), System.lineSeparator(), key);
			} else {
				log.error("Error loading model from fusepath : {}", e.getMessage());
			}
		}
		ediSplitterMetrics.increaseTotalTypeMsgCount(docType);
		if (null != yamlString) {
			try (StringReader yamlReader = new StringReader(yamlString)) {
				EdiModelDeserializer deserializer = new EdiModelDeserializer(yamlReader);
				model = deserializer.deserialize();
				ediModelCache.put(key, model);
			}
		}
		return model;
	}

	private String getFileName(String docType, String version) {
		return fusePath + "/modelresolvers/model-" + docType + "-" + version + "-e.yaml";
	}

}
