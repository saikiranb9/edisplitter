package com.atd.microservices.core.edisplitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import com.atd.microservices.core.edisplitter.domain.EDIReaderPayload;
import com.atd.microservices.core.edisplitter.service.EDIAcknowledgement;
import com.atd.microservices.core.edisplitter.service.EDIProcessor;
import com.atd.microservices.core.edisplitter.service.EDISplitterConsumerService;
import com.atd.microservices.core.edisplitter.service.EDIValidatorService;
import com.atd.microservices.core.edisplitter.service.util.EDIFileUtils;
import com.berryworks.edireader.splitter.EdiSplitter;
import com.berryworks.edireader.splitter.Splitter;
import com.berryworks.edireader.splitter.Splitter.Form;
import com.berryworks.edireader.splitter.SplitterResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
		"kafka.bootstrap.server.url=null",
		"kafka.security.protocol=null",
		"kafka.max.request.size=null",
		"edisplitter.kafka.topic.inbound=TEST_TOPIC",
		"edisplitter.kafka.topic.outbound=TEST_TOPICS",
		"edisplitter.fusePath=testFuseLocation",
		"ssl.truststore.password=null",
		"ssl.truststore.location=null",
		"kafka.analytic.topic=null",
		"apigateway.url=https://develop-edi.gcp.atd-us.com",
        "edisplitter.ediConfigUrl=https://develop-edi.gcp.atd-us.com/ediconfig/partner/",
        "edisplitter.ediAnalyticsDataUrl=https://develop-edi.gcp.atd-us.com/edianalyticsdata/"})
public class EDIProcessorTest {
	
	@MockBean
	private EDISplitterConsumerService ediReaderConsumerService;
   
	@MockBean
	private EDIProcessor ediProcessor;
	
	@MockBean
	@Qualifier("ediReaderKafkaTemplate")
	private KafkaTemplate<String, EDIReaderPayload> ediReaderKafkaTemplate;
	
	@Autowired
	private EDIValidatorService ediValidatorService;
    	
    @Test
    public void testSplit() throws InterruptedException, IOException, SAXException {
    	Resource resource = new ClassPathResource("edieader_incoming_payload.edi");
    	
		try (InputStreamReader reader = new InputStreamReader(resource.getInputStream());
				StringWriter writer = new StringWriter()) {
			
			Splitter splitter = new EdiSplitter();
			SplitterResult result = splitter
				    .split(reader)
				    .selectForm(Form.EDI)
				    .toStrings();
			
			log.info("{}", result.getInterchanges());
			Assert.assertEquals(1, result.getInterchanges().size());				
		}
    }
    
    public void testSplitAndWriteToFile() throws InterruptedException, IOException, SAXException {
    	Resource resource = new ClassPathResource("007914401_CH9OGD2L.850");

		try (InputStreamReader reader = new InputStreamReader(resource.getInputStream());
				StringWriter writer = new StringWriter()) {
			
			Splitter splitter = new EdiSplitter();
			SplitterResult result = splitter
				    .split(reader)
				    .selectForm(Form.EDI)
				    .toStrings();
			
			//log.info("{}", result.getInterchanges());
			int i =1;			
			for (String doc : result.getInterchanges()) {
				saveToFile(doc, "007914401_CHBJYZA1", i);
				i++;
			}						
		}
    }
    
	public void testCreateEDIAck() throws IOException {
		Resource resource = new ClassPathResource("007914401_CH9OGD2L.850");
		String ackResource = new ClassPathResource("ack_997.edi").toString();
		try (Reader reader = new InputStreamReader(resource.getInputStream())) {

			EDIAcknowledgement demo = new EDIAcknowledgement(reader, ackResource);
			demo.create();
		}
	}
    
	@Test
	public void testValidator() throws InterruptedException, IOException, SAXException {
		String ediText = EDIFileUtils.getFileAsString("007914401_CH9OGD2L.850");
		ediValidatorService.validate(ediText);
	}	
    
	private void saveToFile(String doc, String name, int index) {
		try (BufferedWriter fileWriter = new BufferedWriter(
				new FileWriter("testFuseLocation" + name + "/" + name + "_" + index + ".850"))) {			
			try {
				fileWriter.write(doc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
		
	@Test
	public void testProcess() throws InterruptedException, IOException, SAXException {				
		String ediText = EDIFileUtils.getFileAsString("007914401_CH9OGD2L.850");
		ediProcessor.process(ediText, "pepboys");
	}

}