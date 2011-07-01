package org.auscope.portal.server.domain.xml;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is for extracting all attributes of a specific element type from an XML Stream
 * 
 * It exists to offer a high performance low memory method of parsing an XML stream in a simplistic manner
 *
 */
public class XMLStreamAttributeExtractor implements Iterator<String> {
	
	private Scanner scanner;
	private Pattern compiledPattern;
	private Pattern compiledAttributeToFind;
	private String nextValue;
	
	/**
	 * @param elementToMatch The FQ XML element name you will be searching for
	 * @param attributeToFind The FQ attribute within elementToMatch that will be extracted
	 * @param xmlStream The input XML stream
	 */
	public XMLStreamAttributeExtractor(String elementToMatch, String attributeToFind, InputStream xmlStream) {
		this(elementToMatch, attributeToFind, xmlStream,  java.nio.charset.Charset.defaultCharset().toString());
	}
	
	/**
	 * @param elementToMatch The FQ XML element name you will be searching for
	 * @param attributeToFind The FQ attribute within elementToMatch that will be extracted
	 * @param xmlStream The input XML stream
	 * @param xmlStreamCharset The optional charset for the input stream.
	 */
	public XMLStreamAttributeExtractor(String elementToMatch, String attributeToFind, InputStream xmlStream, String xmlStreamCharset) {
		this.scanner = new Scanner(xmlStream, xmlStreamCharset);
		this.compiledAttributeToFind = Pattern.compile(String.format(".*%1$s=\"([^\"]*)\".*", attributeToFind));
		this.compiledPattern = Pattern.compile(String.format("<%1$s[^\\\\>]*>", elementToMatch));
		
	}

	/**
	 * Returns true if there is another attribute value to extract from this extractor 
	 * @return
	 */
	@Override
	public boolean hasNext() {
		if (nextValue == null) {
			do {
				nextValue = scanner.findWithinHorizon(this.compiledPattern, 0);
				if (nextValue == null) {
					return false;
				}
			} while(!this.compiledAttributeToFind.matcher(nextValue).matches());
		}
		
		return true;
	}

	/**
	 * Extracts the next attribute value from this attribute extractor
	 * @return
	 */
	@Override
	public String next() {
		if (nextValue == null) {
			nextValue = scanner.findWithinHorizon(this.compiledPattern, 0);
			if (nextValue == null) {
				return null;
			}
		}
		
		try {
			Matcher attrMatcher = this.compiledAttributeToFind.matcher(nextValue);
			if (attrMatcher.matches()) {
				return attrMatcher.group(1);
			} else {
				throw new IllegalStateException();
			}
		} finally {
			nextValue = null;
		}
	}

	/**
	 * Throws an UnsupportedOperationException
	 */
	@Override
	public void remove() {
		scanner.remove();
		
	}
	
	
}
