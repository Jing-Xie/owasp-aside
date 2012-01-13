package edu.uncc.sis.aside.preferences;

public class PreferencesSet {

	public PreferencesSet( boolean aside_check,
			boolean project_check, boolean external_check, String[] path_items) {
		
		this.aside_check = aside_check;
		this.project_check = project_check;
		this.external_check = external_check;
		this.path_items = path_items;
	}

	public boolean isAside_check() {
		return aside_check;
	}

	public void setAside_check(boolean aside_check) {
		this.aside_check = aside_check;
	}

	public boolean isProject_check() {
		return project_check;
	}

	public void setProject_check(boolean project_check) {
		this.project_check = project_check;
	}

	public boolean isExternal_check() {
		return external_check;
	}

	public void setExternal_check(boolean external_check) {
		this.external_check = external_check;
	}

	public String[] getPath_items() {
		return path_items;
	}

	public void setPath_items(String[] path_items) {
		this.path_items = path_items;
	}

	private boolean aside_check, project_check, external_check;
	private String[] path_items;

}
