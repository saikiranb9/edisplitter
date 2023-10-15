package com.atd.microservices.core.edisplitter.service;

import com.berryworks.edireader.EDIReader;
import com.berryworks.edireader.EDIReaderFactory;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;

@Slf4j
public class EDIAcknowledgement {
    InputSource inputSource;
    OutputStream ackOutput;
    ContentHandler handler;
    EDIReader parser;
    final String outputFileName;

    public EDIAcknowledgement(Reader ediInput, String output) {
        outputFileName = output;

        // Establish output file
        if (outputFileName == null) {
            ackOutput = System.out;
        } else {
            try {
                ackOutput = new BufferedOutputStream(new FileOutputStream(outputFileName));
            } catch (IOException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }

        // Establish inputSource, a SAX InputSource
        try {
            inputSource = new InputSource(ediInput);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * Main processing method for the EDIAcknowledgement object
     */
    public void create() {

        handler = new DefaultHandler();
        char[] leftOver = null;
        Writer ackWriter = new PrintWriter(ackOutput);

        try {
            while (true) {
                // The following line creates an EDIReader explicitly
                // as an alternative to the JAXP-based technique.
                parser = EDIReaderFactory.createEDIReader(inputSource, leftOver);
                if (parser == null) {
                    // end of input
                    break;
                }
                parser.setContentHandler(handler);
                parser.setAcknowledgment(ackWriter);
                parser.parse(inputSource);
                leftOver = parser.getTokenizer().getBuffered();
            }

        } catch (IOException e) {
        	log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (SAXException e) {
        	log.error("\nEDI input not well-formed:\n" + e.toString());
            throw new RuntimeException(e.getMessage());
        }
    }

}