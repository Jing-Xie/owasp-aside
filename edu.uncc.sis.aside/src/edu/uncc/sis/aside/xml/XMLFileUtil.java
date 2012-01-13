package edu.uncc.sis.aside.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.uncc.sis.aside.constants.PluginConstants;

/**
 * Think about file read/write lock
 * 
 * @author Jing Xie
 * 
 */
public class XMLFileUtil {

	/*
	 * READ XML CONTENT
	 */
	
	public static Document getDefaultRuleSetDocument(int type) {
		Bundle bundle = Platform.getBundle(PluginConstants.PLUGIN_ID);
		Path path = null;
		Document doc = null;
		switch (type) {
		case 1:
			path = new Path(PluginConstants.DEFAULT_VALIDATION_RULES_FILE);
			break;
		case 2:
			path = new Path(PluginConstants.DEFAULT_TRUST_BOUNDARIES_FILE);
			break;
		case 3:
			path = new Path(PluginConstants.DEFAULT_SECURE_PROGRAMMING_KNOWLEDGE_BASE);	
			break;
		default:
			break;
		}
		try {
			URL fileURL = FileLocator.find(bundle, path, null);
			if (fileURL != null) {
				InputStream in;
				in = fileURL.openStream();
				if (in != null) {
					doc = getDocument(in);
				}
			}
		} catch (IOException e) {		
			e.printStackTrace();
		}
		return doc;
	}

	public static Document getDocument(InputStream fileName)
			 {
		
		Document doc = null;
		
		try {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();

		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(fileName);
		} catch (IOException e) {		
			e.printStackTrace();
		} catch (ParserConfigurationException e) {		
			e.printStackTrace();
		} catch (SAXException e) {			
			e.printStackTrace();
		}
		return doc;

	}

	
	public static Document getCustomizedRuleSetDocument(String fileName) {
		Document doc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(new File(fileName));
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return doc;
	}
	
	public static Document getCustomizedRuleSetDocument(File file) {
		Document doc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(file);
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return doc;
	}

	/*
	 * WRITE TO XML
	 */
	
	
}
