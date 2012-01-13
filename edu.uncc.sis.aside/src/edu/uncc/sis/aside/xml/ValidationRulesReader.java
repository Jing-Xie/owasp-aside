package edu.uncc.sis.aside.xml;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uncc.sis.aside.domainmodels.RuleType;
import edu.uncc.sis.aside.domainmodels.ValidationRule;
import edu.uncc.sis.aside.utils.DocumentWrapper;

public final class ValidationRulesReader extends XMLReader {

	private static ValidationRulesReader reader;

	private ValidationRulesReader() {
	}

	public static synchronized ValidationRulesReader getInstance() {
		if (reader == null) {
			reader = new ValidationRulesReader();
		}
		return reader;
	}

	public LinkedList<ValidationRule> getValidationRules(
			DocumentWrapper documentWrapper) {

		LinkedList<ValidationRule> rules = new LinkedList<ValidationRule>();
		Document document = documentWrapper.getDocument();
		RuleType.Type type = documentWrapper.getType();
		String uri = document.getDocumentURI();

		if (document != null) {
			Element root = document.getDocumentElement();
			if (root == null) {
				return rules;
			}
			NodeList fNodeList = root.getElementsByTagName("ValidationPattern");
			if (fNodeList == null) {
				return rules;
			}
			for (int i = 0; i < fNodeList.getLength(); i++) {
				Node node = fNodeList.item(i);
				if(node != null){
					if (node.getNodeType() == Node.ELEMENT_NODE) {
					    Element element = (Element)node;	
						ValidationRule rule = getSingleValidationRule(element, uri,
								type);
						rules.add(rule);
					}
				}

			}
		}
		return rules;
	}

	private ValidationRule getSingleValidationRule(Element element, String uri,
			RuleType.Type type) {
		ValidationRule rule = new ValidationRule();

		String ruleKey = element.getAttribute("label");
		String ruleValue = getField(element, "Pattern");
		String defaultValue = getField(element, "Default");

		rule.setRuleKey(ruleKey);
		rule.setRuleValue(ruleValue);
		rule.setDefaultValue(defaultValue);
		rule.setSourceFile(uri);
		rule.setType(type);

		return rule;
	}
}
