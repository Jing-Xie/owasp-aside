package edu.uncc.sis.aside.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader {
	protected String getField(Element e, String key) {
		String field = null;

		if (e != null) {
			NodeList nl = e.getElementsByTagName(key);

			if (nl != null && nl.getLength() > 0) {
				Node n = nl.item(0);

				if (n.getNodeType() == Node.ELEMENT_NODE) {
					e = (Element) n;
					Node text = e.getFirstChild();

					if (text != null) {
						field = text.getNodeValue();
					}

					if (field != null) {
						field = field.trim();
					}
				} else if (n.getNodeType() == Node.TEXT_NODE) {
					field = n.getNodeValue();
				}
			}
		}

		return field;
	}

	protected int getValue(Element e) {
		int value = 0;

		if (e != null) {
			Node n = e.getFirstChild();

			if (n != null) {
				String s = n.getNodeValue();

				if (s != null) {
					value = Integer.parseInt(s.trim());
				}
			}
		}

		return value;
	}
}
