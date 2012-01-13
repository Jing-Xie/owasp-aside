package edu.uncc.sis.aside.domainmodels;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

public class ValidationRule implements IAdaptable {

	private String ruleKey, ruleValue, defaultValue, sourceFile;
	private RuleType.Type type;
	
	@Override
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public String getRuleKey() {
		return ruleKey;
	}

	public void setRuleKey(String ruleKey) {
		this.ruleKey = ruleKey;
	}

	public String getRuleValue() {
		return ruleValue;
	}

	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setType(RuleType.Type type) {
		this.type = type;
	}

	public RuleType.Type getType() {
		return type;
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof ValidationRule) {
			ValidationRule rule = (ValidationRule) object;
            if(rule.getType() == this.getType()){
            	if (rule.getRuleKey() != null && rule.getRuleValue() != null) {
    				if (rule.toString().equals(toString())) {
    					return true;
    				}
    			}
            }
		}

		return false;
	}

	@Override
	public String toString() {
		if (sourceFile == null) {
			return getRuleKey() + "=" + getRuleValue();
		}
		return sourceFile + ": " + getRuleKey()
				+ "=" + getRuleValue();
	}

}
