package edu.uncc.sis.aside.wizards;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SemanticInputWizardPage extends WizardPage {
	
	private String ruleKey;
	
	private Button syntacticCheck, semanticCheck;

	private Composite semanticComposite;

	private Label rootDirectoryLabel, domainNameLabel;

	private Text rootDirectory, domainName;

	private Button browseRootDirectory;

	/**
	 * Constructor for SemanticInputWizardPage.
	 * 
	 * @param pageName
	 */
	public SemanticInputWizardPage(String key) {
		super("Semantic Configuration Page Of " + key);
		this.ruleKey = key;
		setTitle("Configure " + key);
		setDescription("Specify particular information that needs to be added.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		final Shell shell = parent.getShell();

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayoutData(gd);

		semanticCheck = new Button(container, SWT.CHECK);
		semanticCheck.setText("&Semantic Validation");
		semanticCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSemanticChecked(ruleKey);
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		semanticCheck.setLayoutData(gd);

		semanticComposite = new Composite(container, SWT.BORDER);
		layout = new GridLayout();
		layout.numColumns = 3;
		semanticComposite.setLayout(layout);
		
		if(ruleKey.equalsIgnoreCase("URL")){
			rootDirectoryLabel = new Label(semanticComposite, SWT.NONE);
			rootDirectoryLabel.setText("Root Directory: ");

			rootDirectory = new Text(semanticComposite, SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			rootDirectory.setLayoutData(gd);

			browseRootDirectory = new Button(semanticComposite, SWT.PUSH);
			browseRootDirectory.setText("Browse...");
			browseRootDirectory.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					popupDialogUponPush(shell);
				}
			});
		}else if(ruleKey.equalsIgnoreCase("EMAIL")){
			domainNameLabel = new Label(semanticComposite, SWT.NONE);
			domainNameLabel.setText("&Email Address Domain Name: ");
			
			domainName = new Text(semanticComposite, SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);;
			gd.horizontalSpan = 2;
			domainName.setLayoutData(gd);
		}

		gd = new GridData(GridData.FILL_HORIZONTAL);
		semanticComposite.setLayoutData(gd);

		syntacticCheck = new Button(container, SWT.CHECK);
		syntacticCheck.setText("S&yntactic Validation");
		syntacticCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSyntacticChecked(ruleKey);
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		syntacticCheck.setLayoutData(gd);

		initialize();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {

		// default settings
		semanticCheck.setSelection(false);
		
		if(ruleKey.equalsIgnoreCase("URL")){
			rootDirectory.setEnabled(false);
			browseRootDirectory.setEnabled(false);
		}else if(ruleKey.equalsIgnoreCase("EMAIL")){
			domainName.setEnabled(false);
		}

		syntacticCheck.setSelection(true);

	}

	private void handleSyntacticChecked(String ruleKey) {

		boolean checked = syntacticCheck.getSelection();

		semanticCheck.setSelection(!checked);
		
		if(ruleKey.equalsIgnoreCase("URL")){
			rootDirectoryLabel.setEnabled(!checked);
			rootDirectory.setEnabled(!checked);
			browseRootDirectory.setEnabled(!checked);
		}else if(ruleKey.equalsIgnoreCase("EMAIL")){
			domainNameLabel.setEnabled(!checked);
			domainName.setEnabled(!checked);
		}	
	
	}

	private void handleSemanticChecked(String ruleKey) {
		boolean checked = semanticCheck.getSelection();

		syntacticCheck.setSelection(!checked);
		
		if(ruleKey.equalsIgnoreCase("URL")){
			rootDirectoryLabel.setEnabled(checked);
			rootDirectory.setEnabled(checked);
			browseRootDirectory.setEnabled(checked);
		}else if(ruleKey.equalsIgnoreCase("EMAIL")){
			domainNameLabel.setEnabled(checked);
			domainName.setEnabled(checked);
		}
		
	}

	private void popupDialogUponPush(Shell shell) {
		DirectoryDialog dirDialog = new DirectoryDialog(shell);

		// User's home directory
		String base = System.getProperty("user.home");
		dirDialog.setFilterPath(base);
		dirDialog.setText("ASIDE Directory Dialog");
		dirDialog.setMessage("Select a base directory");
		String dirPath = dirDialog.open();
		if (dirPath == null)
			return;
		rootDirectory.setText(dirPath);
	}

	public String syntacticOrSemantic() {
		String validationType = "syntactic";
		if (syntacticCheck.getSelection() && !semanticCheck.getSelection()) {
			return validationType;
		}
		if (!syntacticCheck.getSelection() && semanticCheck.getSelection()) {
			return validationType = "semantic";
		}

		return validationType;

	}
	
	public String getBaseDirectory(){
		return rootDirectory.getText();
	}
	
	public String getServerName(){
		return domainName.getText();
	}
}