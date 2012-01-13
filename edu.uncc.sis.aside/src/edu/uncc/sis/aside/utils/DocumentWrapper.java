package edu.uncc.sis.aside.utils;

import org.w3c.dom.Document;

import edu.uncc.sis.aside.domainmodels.RuleType;

public class DocumentWrapper {

	private Document document;
	private RuleType.Type type;
	
	public DocumentWrapper(Document document, RuleType.Type type){
		this.document = document;
		this.type = type;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public RuleType.Type getType() {
		return type;
	}

	public void setType(RuleType.Type type) {
		this.type = type;
	}
}
