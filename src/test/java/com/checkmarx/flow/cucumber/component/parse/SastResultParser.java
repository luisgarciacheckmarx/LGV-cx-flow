package com.checkmarx.flow.cucumber.component.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SastResultParser {
    private static final Logger log = LoggerFactory.getLogger(SastResultParser.class);

    public Map<String, Element> getPathMapByFilename(String filename) {
        Document sastResult = parse(filename);
        NodeList resultElements = sastResult.getElementsByTagName("Result");

        return IntStream.range(0, resultElements.getLength())
                .mapToObj(index -> (Element) resultElements.item(index))
                .collect(Collectors.toMap(
                        resultElement -> resultElement.getAttribute("FileName"),
                        resultElement -> (Element) resultElement.getElementsByTagName("Path").item(0)));
    }

    private Document parse(String filename) {
        Document result = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Path srcResourcePath = Paths.get(TestContext.CUCUMBER_DATA_DIR,
                    TestContext.SAMPLE_SAST_RESULTS_DIR,
                    filename);
            try (InputStream srcStream = classLoader.getResourceAsStream(srcResourcePath.toString())) {
                if (srcStream != null) {
                    result = documentBuilder.parse(srcStream);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Error parsing SAST results.", e);
        }
        return result;
    }
}
