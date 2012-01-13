package edu.uncc.sis.aside.utils;

import java.util.ArrayList;
import java.util.Arrays;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.domainmodels.VulnerabilityKnowledge;
import edu.uncc.sis.aside.xml.XMLFileUtil;
import edu.uncc.sis.aside.xml.XMLReader;

public class SecureProgrammingKnowledgeBase extends XMLReader {

	private static volatile SecureProgrammingKnowledgeBase instance = null;

	private static ArrayList<VulnerabilityKnowledge> inputValidationList = null;
	private static ArrayList<VulnerabilityKnowledge> outputEncodingList = null;

	private SecureProgrammingKnowledgeBase() {
		super();
		if (inputValidationList == null)
			inputValidationList = new ArrayList<VulnerabilityKnowledge>();

		if (outputEncodingList == null)
			outputEncodingList = new ArrayList<VulnerabilityKnowledge>();

		populateListFromXML();
	}

	public static SecureProgrammingKnowledgeBase getInstance() {
		if (instance == null)
			synchronized (SecureProgrammingKnowledgeBase.class) {
				if (instance == null)
					instance = new SecureProgrammingKnowledgeBase();
			}

		return instance;
	}

	private void populateListFromXML() {
		Document document = XMLFileUtil
				.getDefaultRuleSetDocument(PluginConstants.VK);
		if (document == null)
			return;

		Element root = document.getDocumentElement();
		if (root == null)
			return;

		NodeList children = root.getElementsByTagName("Knowledge");
		if (children == null)
			return;

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child != null) {

				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) child;
					getSingleVulnerabilityKnowledge(element);
				}
			}
		}
	}

	private VulnerabilityKnowledge getSingleVulnerabilityKnowledge(
			Element element) {
		VulnerabilityKnowledge vulnerability = null;

		String explanation = getField(element, "Explanation");
		String remedition = getField(element, "Remedition");
		String auxilliaryLinks = getField(element, "Links");
		String type = element.getAttribute("type");

		String[] linksList = Converter.stringToStringArray(auxilliaryLinks);
		ArrayList<String> links = null;
		if (linksList == null) {
			links = new ArrayList<String>();
		} else {
			links = (ArrayList<String>) Arrays.asList(linksList);
		}

		vulnerability = new VulnerabilityKnowledge(type, explanation,
				remedition, links);

		if (type.equalsIgnoreCase("inputvalidation")) {
			inputValidationList.add(vulnerability);
		} else if (type.equalsIgnoreCase("outputencoding")) {
			outputEncodingList.add(vulnerability);
		}

		return vulnerability;
	}

	public int getEntrySize(int type) {

		switch (type) {

		case PluginConstants.VK_INPUTVALIDATION:
			return inputValidationList.size();
		case PluginConstants.VK_OUTPUTENCODING:
			return outputEncodingList.size();
		default:
			return 0;
		}
	}

	public VulnerabilityKnowledge getEntry(int type, int index) {
		switch (type) {

		case PluginConstants.VK_INPUTVALIDATION:
			return inputValidationList.get(index);
		case PluginConstants.VK_OUTPUTENCODING:
			return outputEncodingList.get(index);
		default:
			return null;
		}
	}

}
