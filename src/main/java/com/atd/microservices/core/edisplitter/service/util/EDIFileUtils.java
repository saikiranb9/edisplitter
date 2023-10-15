package com.atd.microservices.core.edisplitter.service.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EDIFileUtils {

	public static String getFileAsString(String name) {
		Resource modelResolverYaml = new ClassPathResource(name);
		String yamlString = null;
		try (Reader reader = new InputStreamReader(modelResolverYaml.getInputStream())) {
			yamlString = FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return yamlString;
	}
}
