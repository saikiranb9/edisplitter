package com.atd.microservices.core.edisplitter.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atd.microservices.core.edisplitter.domain.Acknowledgement;
import com.atd.microservices.core.edisplitter.exception.EDISplitterException;
import com.berryworks.edireader.model.EdiModel;
import com.berryworks.edireader.validator.Validator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EDIValidatorService {

	@Autowired
	private EDIModelResolver ediModelResolver;

	public Acknowledgement validate(String input) throws InterruptedException, IOException, SAXException {
		String docType, version, standard, ackOutput = null;

		try (StringReader reader = new StringReader(input); StringWriter writer = new StringWriter()) {
			EdiPreview preview = new EdiPreview(reader);
			standard = preview.getStandard().toString();
			docType = preview.getDocumentType();
			version = preview.getVersion();
		}
		EdiModel model = ediModelResolver.getEDIModel(docType, version);

		/*
		 * Only generate acknowledgement if valid model resolver exists.
		 */
		if (null != model.getDocumentType()) {

			try (StringReader reader = new StringReader(input);
					StringWriter output = new StringWriter();
					StringWriter report = new StringWriter()) {
				Validator validator = new Validator(); // com.berryworks.edireader.validator.Validator
				validator.setEdiModel(model);
				validator.setEdiSource(new InputSource(reader));
				validator.setReport(report);
				validator.setOutput(output);
				validator.run();
				ackOutput = output.toString();
				log.info("output:{}", ackOutput);
				log.info("report:{}", report.toString());
				log.info("Error count:{}", validator.getComplianceErrorCount());
				if (validator.getComplianceErrorCount() > 0) {
					throw new EDISplitterException("Validation failed:" + "\n" + report.toString());
				}
			}

		}

		return new Acknowledgement(ackOutput, docType, standard, version);
	}
}