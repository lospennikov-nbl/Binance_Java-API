package com.binance.api.generator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MessageXMLParseErrorHandler implements ErrorHandler {

  MessageXMLParseErrorHandler() {
  }

  private String getParseExceptionInfo(SAXParseException spe) {
    String systemId = spe.getSystemId();
    if (systemId == null) {
      systemId = "null";
    }

    String info = "URI=" + systemId + " Line=" + spe.getLineNumber() +
        ": " + spe.getMessage();
    return info;
  }

  @Override
  public void warning(SAXParseException spe) throws SAXException {
    System.out.println("Warning: " + getParseExceptionInfo(spe));
  }

  @Override
  public void error(SAXParseException spe) throws SAXException {
    String message = "Error: " + getParseExceptionInfo(spe);
    throw new SAXException(message);
  }

  @Override
  public void fatalError(SAXParseException spe) throws SAXException {
    String message = "Fatal Error: " + getParseExceptionInfo(spe);
    throw new SAXException(message);
  }
}
