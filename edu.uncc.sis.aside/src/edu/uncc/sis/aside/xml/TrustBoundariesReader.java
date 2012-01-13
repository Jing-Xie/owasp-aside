package edu.uncc.sis.aside.xml;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uncc.sis.aside.domainmodels.NonReturnedTrustBoundary;
import edu.uncc.sis.aside.domainmodels.ReturnedTrustBoundary;
import edu.uncc.sis.aside.domainmodels.TrustBoundary;
import edu.uncc.sis.aside.utils.Converter;

public final class TrustBoundariesReader extends XMLReader {

	private static TrustBoundariesReader reader;

	private TrustBoundariesReader() {

	}

	public static TrustBoundariesReader getInstance() {
		if (reader == null) {
			reader = new TrustBoundariesReader();
		}

		return reader;
	}

	public LinkedList<TrustBoundary> getTrustBoundaries(Document document) {
		LinkedList<TrustBoundary> boundarySet = new LinkedList<TrustBoundary>();

		if (document != null) {
			Element root = document.getDocumentElement();
			if (root == null) {
				return boundarySet;
			}
			NodeList children = document.getElementsByTagName("TrustBoundary");
			if (children == null) {
				return boundarySet;
			}
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child != null) {
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) child;
						TrustBoundary boundary = getSingleTrustBoundary(element);
						boundarySet.add(boundary);
					}
				}
			}
		}

		return boundarySet;
	}

	private TrustBoundary getSingleTrustBoundary(Element element) {
		TrustBoundary boundary = null;

		String declarationClass = getField(element, "DeclarationClass");
		String methodName = getField(element, "MethodName");

		String methodType = element.getAttribute("type");
		String attrType = element.getAttribute("attr");
		String[] attrTypes = Converter.stringToStringArray(attrType);

		if (getField(element, "ReturnType") != null
				&& getField(element, "ArgumentOrdinal") == null) {
			String returnClass = getField(element, "ReturnType");
			boundary = new ReturnedTrustBoundary(declarationClass, methodName,
					methodType, attrTypes, returnClass);
		} else if (getField(element, "ReturnType") == null
				&& getField(element, "ArgumentOrdinal") != null) {
			String argumentIndicesString = getField(element, "ArgumentOrdinal");
			int[] argumentIndice = Converter
					.stringToIntArray(argumentIndicesString);
			boundary = new NonReturnedTrustBoundary(declarationClass,
					methodName, methodType, attrTypes, argumentIndice);
		} else {
			// something is wrong with the XML file
		}

		return boundary;
	}
}
