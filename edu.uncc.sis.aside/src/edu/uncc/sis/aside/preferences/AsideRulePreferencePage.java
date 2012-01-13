package edu.uncc.sis.aside.preferences;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.jobs.TrustBoundariesResetJob;
import edu.uncc.sis.aside.jobs.ValidationRulesResetJob;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog.
 * 
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class AsideRulePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	// controls that will show up in the preference window
	private Composite composite, _externalComposite, externalComposite,
			_pathComposite, pathComposite, trustBoundariesTabComposite,
			validationRulesTabComposite;
	private Button asideRulesCheck, _asideRulesCheck, _projectRulesCheck,
			projectRulesCheck, _externalRulesCheck, externalRulesCheck,
			_addPathButton, addPathButton, _removePathButton, removePathButton;
	private List _pathList, pathList;
	private CTabFolder tabFolder;
	private CTabItem trustBoundariesTab, validationRulesTab;

	private boolean pre_asideRulesCheck, pre_projectRulesCheck,
			pre_externalRulesCheck, preasideRulesCheck, preprojectRulesCheck, preexternalRulesCheck;
	private String pre_pathList, prepathList;

	public AsideRulePreferencePage() {
		super();

		// Set the preference store for this preference page
		setPreferenceStore(AsidePlugin.getDefault().getPreferenceStore());
		setDescription("General Settings for ASIDE Rules");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.LEFT);
		composite.setLayout(new GridLayout());

		GridData compositeData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL);
		composite.setLayoutData(compositeData);

		Label space = new Label(composite, SWT.NONE);
		space.setVisible(false);

		tabFolder = new CTabFolder(composite, SWT.TOP | SWT.BORDER);
		tabFolder.setSimple(false);

		/*
		 * Tab for configuring various Trust Boundaries
		 */
		trustBoundariesTab = new CTabItem(tabFolder, SWT.NONE);
		trustBoundariesTab.setText("Configure Trust Boundaries");
		trustBoundariesTabComposite = new Composite(tabFolder, SWT.NONE);
		trustBoundariesTab.setControl(trustBoundariesTabComposite);

		trustBoundariesTabComposite.setLayout(new GridLayout());
		trustBoundariesTabComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		_asideRulesCheck = new Button(trustBoundariesTabComposite, SWT.CHECK);
		GridData _asideRulesCheckData = new GridData(GridData.FILL_HORIZONTAL);
		_asideRulesCheckData.horizontalIndent = 5;
		_asideRulesCheck.setLayoutData(_asideRulesCheckData);
		_asideRulesCheck.setText("Use default trust boundaries from ASIDE");
		pre_asideRulesCheck = getPreferenceStore().getBoolean(
				IPreferenceConstants.ASIDE_TB_PREFERENCE);
		_asideRulesCheck.setSelection(pre_asideRulesCheck);

		_projectRulesCheck = new Button(trustBoundariesTabComposite, SWT.CHECK);
		GridData _projectRulesCheckData = new GridData(GridData.FILL_HORIZONTAL);
		_projectRulesCheckData.horizontalIndent = 5;
		_projectRulesCheck.setLayoutData(_projectRulesCheckData);
		_projectRulesCheck
				.setText("Use trust boundaries from the project under detection");
		pre_projectRulesCheck = getPreferenceStore().getBoolean(
				IPreferenceConstants.PROJECT_TB_PREFERENCE);
		_projectRulesCheck.setSelection(pre_projectRulesCheck);

		_externalComposite = new Composite(trustBoundariesTabComposite,
				SWT.BEGINNING);
		_externalComposite.setLayout(new GridLayout());
		GridData _externalCompositeData = new GridData(GridData.FILL_HORIZONTAL);
		_externalCompositeData.horizontalAlignment = GridData.BEGINNING;
		_externalComposite.setLayoutData(_externalCompositeData);

		_externalRulesCheck = new Button(_externalComposite, SWT.BEGINNING
				| SWT.CHECK);
		_externalRulesCheck
				.setText("Use trust boundaries from external resource");
		_externalRulesCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				boolean checked = _externalRulesCheck.getSelection();

				if (checked) {
					_pathList.setEnabled(true);
					_pathList.setItems(convert(getPreferenceStore().getString(
							IPreferenceConstants.EXTERNAL_TB_PATH_PREFERENCE)));
					_addPathButton.setEnabled(true);
					_removePathButton.setEnabled(_pathList.getSelectionIndex() >= 0);

				} else {
					_pathList.setEnabled(false);
					_addPathButton.setEnabled(false);
					_removePathButton.setEnabled(false);
				}
			}

		});
		pre_externalRulesCheck = getPreferenceStore().getBoolean(
				IPreferenceConstants.EXTERNAL_TB_PREFERENCE);
		_externalRulesCheck.setSelection(pre_externalRulesCheck);

		_pathComposite = new Composite(_externalComposite, SWT.SHADOW_NONE);
		GridLayout _layout = new GridLayout();
		_layout.numColumns = 2;
		_pathComposite.setLayout(_layout);
		_pathComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		_pathList = new List(_pathComposite, SWT.LEFT | SWT.MULTI
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);

		GridData _layoutData = new GridData();
		_layoutData.horizontalSpan = 2;
		_layoutData.heightHint = convertVerticalDLUsToPixels(IPreferenceConstants.LIST_HEIGHT_IN_DLUS);
		_layoutData.widthHint = convertHorizontalDLUsToPixels(IPreferenceConstants.LIST_WIDTH_IN_DLUS);
		_pathList.setLayoutData(_layoutData);
		_pathList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (_pathList.getItemCount() > 0) {

					int selectionIndex = _pathList.getSelectionIndex();

					if (selectionIndex != -1) {
						_removePathButton.setEnabled(true);
					}

				} else {
					// do nothing
				}
			}

		});

		_addPathButton = new Button(_pathComposite, SWT.PUSH | SWT.LEFT);
		_addPathButton.setText("Browse..");

		_addPathButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// pop up a file selection dialog
				FileDialog fileDialog = new FileDialog(composite.getShell(),
						SWT.OPEN);
				fileDialog.setFilterPath(System.getProperty("user.dir"));
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				fileDialog.setText("Choose validation rules file");
				String newItem = fileDialog.open();
				// check to see whether this item is in the list
				if (newItem != null) {

					if (newItem.length() > 0) {
						if (newItem.endsWith(".xml")) {
							String[] items = _pathList.getItems();
							if (items.length == 0) {
								_pathList.add(newItem);
							} else {
								for (String item : items) {
									if (newItem.equals(item)) {
										MessageBox message = new MessageBox(
												composite.getShell(),
												SWT.ICON_INFORMATION);
										message.setMessage("Selected path is in the path list already!");
										message.open();
										break;
									} else {
										_pathList.add(newItem);
									}

								}
							}
						} else {
							MessageBox message = new MessageBox(composite
									.getShell(), SWT.ICON_INFORMATION);
							message.setMessage("Selected file is not in XML format!");
							message.open();
						}
					}
				}
			}
		});
		_removePathButton = new Button(_pathComposite, SWT.PUSH | SWT.RIGHT);
		_removePathButton.setText("Remove");
		GridData _removePathButtonData = new GridData();
		_removePathButtonData.horizontalAlignment = GridData.END;
		_removePathButton.setLayoutData(_removePathButtonData);
		_removePathButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// delete the selected item in the list and deactivate itself

				int selectionIndex = _pathList.getSelectionIndex();
				if (selectionIndex != -1) {
					_pathList.remove(selectionIndex);
					_selectionChanged();
				}

			}

		});

		boolean _externalSwitchON = _externalRulesCheck.getSelection();
		if (_externalSwitchON) {
			_pathList.setEnabled(true);
			_addPathButton.setEnabled(true);

			_removePathButton.setEnabled(_pathList.getSelectionIndex() >= 0);

		} else {
			_pathList.setEnabled(false);
			_addPathButton.setEnabled(false);
			_removePathButton.setEnabled(false);
		}

		/*
		 * Tab for Configuring Validation Rules
		 */
		validationRulesTab = new CTabItem(tabFolder, SWT.NONE);
		validationRulesTab.setText("Configure Validation Rules");
		validationRulesTabComposite = new Composite(tabFolder, SWT.NONE);
		validationRulesTab.setControl(validationRulesTabComposite);

		validationRulesTabComposite.setLayout(new GridLayout());
		validationRulesTabComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		asideRulesCheck = new Button(validationRulesTabComposite, SWT.CHECK);
		GridData asideRulesCheckData = new GridData(GridData.FILL_HORIZONTAL);
		asideRulesCheckData.horizontalIndent = 5;
		asideRulesCheck.setLayoutData(asideRulesCheckData);
		asideRulesCheck.setText("Use default validation rules from ASIDE");
		preasideRulesCheck = getPreferenceStore().getBoolean(
				IPreferenceConstants.ASIDE_VR_PREFERENCE);
		asideRulesCheck.setSelection(preasideRulesCheck);

		projectRulesCheck = new Button(validationRulesTabComposite, SWT.CHECK);
		GridData projectRulesCheckData = new GridData(GridData.FILL_HORIZONTAL);
		projectRulesCheckData.horizontalIndent = 5;
		projectRulesCheck.setLayoutData(projectRulesCheckData);
		projectRulesCheck
				.setText("Use validation rules from the project under detection");
		preprojectRulesCheck = getPreferenceStore().getBoolean(
				IPreferenceConstants.PROJECT_VR_PREFERENCE);
		projectRulesCheck.setSelection(preprojectRulesCheck);

		externalComposite = new Composite(validationRulesTabComposite, SWT.NONE);

		externalComposite.setLayout(new GridLayout());
		externalComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL));

		externalRulesCheck = new Button(externalComposite, SWT.CHECK);
		externalRulesCheck
				.setText("Use validation rules from external resource");

		externalRulesCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean checked = externalRulesCheck.getSelection();
				if (checked) {
					pathList.setEnabled(true);
					prepathList = getPreferenceStore().getString(IPreferenceConstants.EXTERNAL_VR_PATH_PREFERENCE);
					pathList.setItems(convert(prepathList));
					addPathButton.setEnabled(true);

					removePathButton.setEnabled(pathList.getSelectionIndex() >= 0);
					
				} else {
					pathList.setEnabled(false);
					addPathButton.setEnabled(false);
					removePathButton.setEnabled(false);
				}
			}

		});
		preexternalRulesCheck = getPreferenceStore().getBoolean(
				IPreferenceConstants.EXTERNAL_VR_PREFERENCE);
		externalRulesCheck.setSelection(preexternalRulesCheck);

		pathComposite = new Composite(externalComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		pathComposite.setLayout(layout);

		pathList = new List(pathComposite, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		pathList.setSize(150, 115);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 2;
		layoutData.heightHint = convertVerticalDLUsToPixels(IPreferenceConstants.LIST_HEIGHT_IN_DLUS);
		layoutData.widthHint = convertHorizontalDLUsToPixels(IPreferenceConstants.LIST_WIDTH_IN_DLUS);
		pathList.setLayoutData(layoutData);

		pathList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (pathList.getItemCount() > 0) {
					int selectionIndex = pathList.getSelectionIndex();

					if (selectionIndex != -1) {
						removePathButton.setEnabled(true);
					}
				} else {
					// do nothing
				}

			}

		});
		addPathButton = new Button(pathComposite, SWT.PUSH | SWT.LEFT);
		addPathButton.setText("Browse..");
		addPathButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// pop up a file selection dialog
				FileDialog fileDialog = new FileDialog(composite.getShell(),
						SWT.OPEN);
				fileDialog.setFilterPath(System.getProperty("user.dir"));
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				fileDialog.setText("Choose trust boundaries file");
				String newItem = fileDialog.open();
				// check to see whether this item is in the list
				if (newItem != null) {
					if (newItem.length() > 0) {
						if (newItem.endsWith(".xml")) {
							String[] items = pathList.getItems();
							if (items.length == 0) {
								pathList.add(newItem);
							} else {
								for (String item : items) {
									if (newItem.equals(item)) {
										MessageBox message = new MessageBox(
												composite.getShell(),
												SWT.ICON_INFORMATION);
										message.setMessage("Selected path is in the path list already!");
										message.open();
										break;
									}

								}
								pathList.add(newItem);
							}
						} else {
							MessageBox message = new MessageBox(composite
									.getShell(), SWT.ICON_INFORMATION);
							message.setMessage("Selected file is not in XML format!");
							message.open();
						}
					}

				}

			}

		});
		removePathButton = new Button(pathComposite, SWT.PUSH | SWT.RIGHT);
		GridData removePathButtonData = new GridData(GridData.FILL_HORIZONTAL);
		removePathButtonData.horizontalAlignment = GridData.END;
		removePathButton.setLayoutData(removePathButtonData);
		removePathButton.setText("Remove");
		removePathButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				int selectionIndex = pathList.getSelectionIndex();

				if (selectionIndex != -1) {
					pathList.remove(pathList.getSelectionIndex());
					selectionChanged();
				}
			}

		});

		boolean externalSwitchON = externalRulesCheck.getSelection();
		if (externalSwitchON) {
			pathList.setEnabled(true);
			addPathButton.setEnabled(true);

			removePathButton.setEnabled(pathList.getSelectionIndex() >= 0);

		} else {
			pathList.setEnabled(false);
			addPathButton.setEnabled(false);
			removePathButton.setEnabled(false);
		}

		tabFolder.setSelection(trustBoundariesTab);
		return composite;
	}

	@Override
	protected void performDefaults() {

		PreferenceInitializer initializer = new PreferenceInitializer();
		initializer.initializeDefaultPreferences();
		boolean externalSwitchON = false;

		// Apply these default settings to controls
		_asideRulesCheck.setSelection(AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.ASIDE_TB_PREFERENCE));
		_projectRulesCheck.setSelection(AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.PROJECT_TB_PREFERENCE));
		_externalRulesCheck.setSelection(AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.EXTERNAL_TB_PREFERENCE));

		externalSwitchON = _externalRulesCheck.getSelection();
		if (externalSwitchON) {
			_pathList.setEnabled(true);
			_addPathButton.setEnabled(true);

			_removePathButton.setEnabled(_pathList.getSelectionIndex() >= 0);

		} else {
			_pathList.setEnabled(false);
			_addPathButton.setEnabled(false);
			_removePathButton.setEnabled(false);
		}

		asideRulesCheck.setSelection(AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.ASIDE_VR_PREFERENCE));
		projectRulesCheck.setSelection(AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.PROJECT_VR_PREFERENCE));
		externalRulesCheck.setSelection(AsidePlugin.getDefault()
				.getPreferenceStore()
				.getBoolean(IPreferenceConstants.EXTERNAL_VR_PREFERENCE));

		externalSwitchON = externalRulesCheck.getSelection();
		if (externalSwitchON) {
			pathList.setEnabled(true);
			addPathButton.setEnabled(true);

			removePathButton.setEnabled(pathList.getSelectionIndex() >= 0);

		} else {
			pathList.setEnabled(false);
			addPathButton.setEnabled(false);
			removePathButton.setEnabled(false);
		}

		/*
		 * TODO use eclipse job to start another thread for rescanning, but it will
		 * have to wait until I have time to finish this
		 */

		super.performDefaults();

	}

	@Override
	public boolean performOk() {

		// Trust boundaries setting
		if (_asideRulesCheck.getSelection() != pre_asideRulesCheck) {
			AsidePlugin.getDefault().setAsideTBCheckPreference(
					_asideRulesCheck.getSelection());
		}

		if (_projectRulesCheck.getSelection() != pre_projectRulesCheck) {
			AsidePlugin.getDefault().setProjectTBCheckPreference(
					_projectRulesCheck.getSelection());
		}

		if (_externalRulesCheck.getSelection() && pre_externalRulesCheck) {

			String[] pre_items = convert(pre_pathList);
			String[] current_items = _pathList.getItems();

			if (differentFrom(current_items, pre_items)) {
				AsidePlugin.getDefault().setTBPathPreference(
						_pathList.getItems());
			}

		} else if (!_externalRulesCheck.getSelection()
				&& !pre_externalRulesCheck) {

		} else if (!_externalRulesCheck.getSelection()
				&& pre_externalRulesCheck) {
			AsidePlugin.getDefault().setTBPathPreference(null);
			AsidePlugin.getDefault().setExternalTBCheckPreference(false);
		} else if (_externalRulesCheck.getSelection()
				&& !pre_externalRulesCheck) {
			AsidePlugin.getDefault().setExternalTBCheckPreference(true);
			AsidePlugin.getDefault().setTBPathPreference(_pathList.getItems());
		}

		// Validation rules setting
		AsidePlugin.getDefault().setAsideVRCheckPreference(
				asideRulesCheck.getSelection());

		AsidePlugin.getDefault().setProjectVRCheckPreference(
				projectRulesCheck.getSelection());

		if (externalRulesCheck.getSelection()) {
			AsidePlugin.getDefault().setVRPathPreference(pathList.getItems());
			AsidePlugin.getDefault().setExternalVRCheckPreference(true);
		} else {
			AsidePlugin.getDefault().setVRPathPreference(null);
			AsidePlugin.getDefault().setExternalVRCheckPreference(false);
		}

		PreferencesSet prefSet = new PreferencesSet(_asideRulesCheck.getSelection(), _projectRulesCheck.getSelection(), _externalRulesCheck.getSelection(), _pathList.getItems());
		
		// possible solution: pass control results to Jobs
		TrustBoundariesResetJob tb_job = new TrustBoundariesResetJob("Rescanning projects...", prefSet);
		tb_job.scheduleInteractive();
//		ValidationRulesResetJob vr_job = new ValidationRulesResetJob("Retrieving rules...");
//		vr_job.scheduleNonInteractive();
		
		return super.performOk();

	}

	/*
	 * Sets the enablement of the remove path button depending on the selection
	 * in the list.
	 */
	private void _selectionChanged() {
		int index = _pathList.getSelectionIndex();
		_removePathButton.setEnabled(index >= 0);
	}

	private void selectionChanged() {
		int index = pathList.getSelectionIndex();
		removePathButton.setEnabled(index >= 0);
	}

	private String[] convert(String preferenceValue) {
		StringTokenizer tokenizer = new StringTokenizer(preferenceValue,
				IPreferenceConstants.PATH_DELIMITER);
		int tokenCount = tokenizer.countTokens();
		String[] elements = new String[tokenCount];

		for (int i = 0; i < tokenCount; i++) {
			elements[i] = tokenizer.nextToken();
		}

		return elements;
	}

	private boolean differentFrom(String[] preset, String[] postset) {

		return false;
	}
}