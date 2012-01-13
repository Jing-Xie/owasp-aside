package edu.uncc.sis.aside.domainmodels;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.constants.PluginConstants;
import edu.uncc.sis.aside.preferences.IPreferenceConstants;
import edu.uncc.sis.aside.utils.DocumentWrapper;
import edu.uncc.sis.aside.xml.ValidationRulesReader;
import edu.uncc.sis.aside.xml.XMLFileUtil;

/**
 * This class is defined as a singleton. it reads the rules from rule definition
 * file(s) and
 * 
 * @author Jing Xie
 * 
 */
public final class ValidationRulesProvider {

	private static ValidationRulesProvider ruleProvider;
	private LinkedList<ValidationRule> ruleset;

	private IProject fProject;

	private LinkedList<DocumentWrapper> ruleFileDocWrappers = new LinkedList<DocumentWrapper>();

	private ValidationRulesProvider(IProject project) {

		fProject = project;

		ruleset = new LinkedList<ValidationRule>();
		checkValidationRulesPreferences();

		Iterator<DocumentWrapper> iterator = ruleFileDocWrappers.iterator();
		while (iterator.hasNext()) {
			DocumentWrapper ruleFileDocWrapper = iterator.next();
			LinkedList<ValidationRule> rules = ValidationRulesReader
					.getInstance().getValidationRules(ruleFileDocWrapper);
			ruleset.addAll(rules);
		}

	}

	public static synchronized ValidationRulesProvider getInstance(
			IProject project) {

		if (ruleProvider == null) {
			ruleProvider = new ValidationRulesProvider(project);
		}
		return ruleProvider;
	}

	public LinkedList<ValidationRule> getRuleset() {
		return ruleset;
	}

	private void checkValidationRulesPreferences() {

		boolean asideRulesChecked = AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.ASIDE_VR_PREFERENCE);
		if (asideRulesChecked) {
			// add default ruleset into file path array
			Document defaultRulesDocument = XMLFileUtil
					.getDefaultRuleSetDocument(PluginConstants.VR);
			if (defaultRulesDocument != null) {
				DocumentWrapper defaultDocumentWrapper = new DocumentWrapper(
						defaultRulesDocument, RuleType.Type.DEFAULT);
				ruleFileDocWrappers.add(defaultDocumentWrapper);
			}
		}

		try {
			boolean projectRulesChecked = AsidePlugin.getDefault()
					.getPreferenceStore()
					.getBoolean(IPreferenceConstants.PROJECT_VR_PREFERENCE);
			if (projectRulesChecked) {

				if (fProject != null) {
					IPath fullPath = fProject.getFullPath();
					IPath newPath = fullPath
							.append("/aside-rules/validation-rules.xml");
					IFile rulesFile = fProject.getFile(newPath);
					if (rulesFile != null && rulesFile.exists()) {
						InputStream contents = rulesFile.getContents();
						Document projectRulesDocument = XMLFileUtil
								.getDocument(contents);
						if (projectRulesDocument != null) {
							DocumentWrapper projectDocumentWrapper = new DocumentWrapper(
									projectRulesDocument, RuleType.Type.PROJECT);
							ruleFileDocWrappers.add(projectDocumentWrapper);
						}
					}
				}

			}
		} catch (CoreException e) {
		}

		boolean externalRulesChecked = AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.EXTERNAL_VR_PREFERENCE);
		if (externalRulesChecked) {
			String[] elements = AsidePlugin.getDefault().getVRPathsPreference();

			for (String element : elements) {
				Document doc = XMLFileUtil
						.getCustomizedRuleSetDocument(element);
				DocumentWrapper externalDocumentWrapper = new DocumentWrapper(
						doc, RuleType.Type.EXTERNAL);
				ruleFileDocWrappers.add(externalDocumentWrapper);
			}
		}

	}

	private URL getProjectRulesURL() {
		URI projectLocation = fProject.getLocationURI();
		URL url = null;
		if (projectLocation != null) {
			try {
				URL projectURL = projectLocation.toURL();
				String urlString = projectURL.toString();
				String ruleString = urlString
						+ System.getProperty("file.separator")
						+ PluginConstants.DEFAULT_VALIDATION_RULES_FILE;
				url = new URL(ruleString);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return url;
	}
}
