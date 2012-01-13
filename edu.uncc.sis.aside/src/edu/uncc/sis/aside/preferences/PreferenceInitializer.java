package edu.uncc.sis.aside.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.uncc.sis.aside.AsidePlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = AsidePlugin.getDefault().getPreferenceStore();

		store.setDefault(IPreferenceConstants.ASIDE_TB_PREFERENCE, true);
		store.setDefault(IPreferenceConstants.ASIDE_VR_PREFERENCE, true);
		store.setDefault(IPreferenceConstants.PROJECT_TB_PREFERENCE, false);
		store.setDefault(IPreferenceConstants.PROJECT_VR_PREFERENCE, false);
		store.setDefault(IPreferenceConstants.EXTERNAL_TB_PREFERENCE, false);
		store.setDefault(IPreferenceConstants.EXTERNAL_VR_PREFERENCE, false);
		store.setDefault(IPreferenceConstants.EXTERNAL_TB_PATH_PREFERENCE,
				IPreferenceConstants.EXTERNAL_TB_PATH_PREFERENCE_DEFAULT);
		store.setDefault(IPreferenceConstants.EXTERNAL_VR_PATH_PREFERENCE,
				IPreferenceConstants.EXTERNAL_VR_PATH_PREFERENCE_DEFAULT);

	}

}
