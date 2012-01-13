package edu.uncc.sis.aside.constants;

public class PluginConstants {

	public static final String PLUGIN_ID = "edu.uncc.sis.aside";
	public static final String TRUST_BOUNDARIES_FILE = "aside-trust-boundaries.xml";
	public static final String VALIDATION_RULES_FILE = "aside-valiation-rules.xml";

	public static final String DEFAULT_TRUST_BOUNDARIES_FILE = "default_rule_pack"
			+ System.getProperty("file.separator")
			+ "aside-default-trust-boundaries.xml";
	public static final String DEFAULT_VALIDATION_RULES_FILE = "default_rule_pack"
			+ System.getProperty("file.separator")
			+ "aside-default-validation-rules.xml";
	public static final String DEFAULT_SECURE_PROGRAMMING_KNOWLEDGE_BASE = "default_rule_pack"
			+ System.getProperty("file.separator")
			+ "aside-secure-programming-knowledge-base.xml";


	public static final int VR = 1;
	public static final int TB = 2;
	public static final int VK = 3;
	
	public static final int VK_INPUTVALIDATION = 1;
	public static final int VK_OUTPUTENCODING = 2;
}
