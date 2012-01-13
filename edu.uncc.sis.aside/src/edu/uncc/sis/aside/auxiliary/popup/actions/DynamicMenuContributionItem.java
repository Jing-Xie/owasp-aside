package edu.uncc.sis.aside.auxiliary.popup.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import edu.uncc.sis.aside.auxiliary.properties.ESAPIPropertiesReader;
import edu.uncc.sis.aside.views.ExplanationView;

public class DynamicMenuContributionItem extends CompoundContributionItem {

	private static final String CONTRIBUTION_ITEM_ID = "edu.uncc.sis.aside.auxiliary.dynamicMenuContributionItem";
	private static final String CONTRIBUTION_COMMAND_ID = "edu.uncc.sis.aside.auxiliary.dynamicCommand";

	private static final String EXPLANATION_VIEW_ID = "edu.uncc.sis.aside.views.complimentaryExplanationView";
	
	public DynamicMenuContributionItem() {
		super();
	}

	public DynamicMenuContributionItem(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {

		IEditorSite currentEditorSite = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
				.getEditorSite();
		
		ArrayList<String> definedInputTypeList = ESAPIPropertiesReader
				.getInstance().retrieveESAPIDefinedInputTypes();
		String[] inputTypes = definedInputTypeList
				.toArray(new String[definedInputTypeList.size()]);
		IContributionItem[] items = new IContributionItem[inputTypes.length];
		
		if (inputTypes.length == 0) {
			presentComplimentaryExplanationView();
		    return items;
		}
		
		final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(
				currentEditorSite, CONTRIBUTION_ITEM_ID,
				CONTRIBUTION_COMMAND_ID, CommandContributionItem.STYLE_PUSH);

		for (int i = 0; i < inputTypes.length; i++) {
			contributionParameter.label = "Validate input using type: "
					+ inputTypes[i];
			items[i] = new CommandContributionItem(contributionParameter);
		}
		
		return items;
	}

	private void presentComplimentaryExplanationView() {
		
		try {
			IViewPart view = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getActivePage()
					.showView(EXPLANATION_VIEW_ID);
			if (view != null && view instanceof ExplanationView) {
				ExplanationView guidanceView = (ExplanationView) view;
				StyledText text = guidanceView.getWidget();
				String guidance = "ASIDE failed to locate ESAPI configuration file. Please follow OWASP ESAPI installation instructions and place ESAPI.properties file under HOME_DIRECTORY/.esapi";
				text.setText(guidance);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
