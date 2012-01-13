package edu.uncc.sis.aside.preferences;

public interface IPreferenceConstants {

	// The identifiers for the preferences
	public static final String ASIDE_VR_PREFERENCE = "asideValidationRules";
	public static final String PROJECT_VR_PREFERENCE = "projectValidationRules";
	public static final String EXTERNAL_VR_PREFERENCE = "externalValidationRules";
	public static final String EXTERNAL_VR_PATH_PREFERENCE = "externalPaths";

	public static final String ASIDE_TB_PREFERENCE = "asideTrustBoundaries";
	public static final String PROJECT_TB_PREFERENCE = "projectTrustBoundaries";
	public static final String EXTERNAL_TB_PREFERENCE = "externalTrustBoundaries";
	public static final String EXTERNAL_TB_PATH_PREFERENCE = "externalPaths";

	// The default values for the preferences
	public static final String EXTERNAL_VR_PATH_PREFERENCE_DEFAULT = "";
	public static final String EXTERNAL_TB_PATH_PREFERENCE_DEFAULT = "";

	// The path delimiter
	public static final String PATH_DELIMITER = ";";

	// List Control size
	public static final int VERTICAL_DIALOG_UNITS_PER_CHAR = 8;
	public static final int LIST_HEIGHT_IN_CHARS = 5;
	public static final int LIST_WIDTH_IN_CHARS = 25;
	public static final int LIST_HEIGHT_IN_DLUS = LIST_HEIGHT_IN_CHARS
			* VERTICAL_DIALOG_UNITS_PER_CHAR;
	public static final int LIST_WIDTH_IN_DLUS = LIST_WIDTH_IN_CHARS
			* VERTICAL_DIALOG_UNITS_PER_CHAR;
}
