package org.personal.interfaces;

import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface XmlFileWriter {
    String createXmlFile(String name) throws IOException;
    void writeXmlFile(String path, String content) throws IOException ;

    void prettifyXmlFile(String path) throws SAXException, IOException, TransformerException;
}
