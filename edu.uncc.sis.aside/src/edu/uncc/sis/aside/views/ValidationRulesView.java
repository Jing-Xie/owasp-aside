package edu.uncc.sis.aside.views;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.domainmodels.RuleType;
import edu.uncc.sis.aside.domainmodels.ValidationRule;
import edu.uncc.sis.aside.domainmodels.ValidationRulesProvider;

/**
 * @author Jing Xie
 * @since 06/06/2010
 */

public class ValidationRulesView extends ViewPart implements
		IResourceChangeListener {

	private static final Logger logger = AsidePlugin.getLogManager()
	.getLogger(ValidationRulesView.class.getName());
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String VIEW_ID = "edu.uncc.sis.aside.views.ValidationRulesView";

	private TableViewer viewer;
	private Action addRow;
	private Action deleteSelectedRow;
	private Action doubleClickAction;

	private ISelection selection;

	/**
	 * The constructor.
	 */
	public ValidationRulesView() {
		super();
		IViewSite viewSite = getViewSite();
		
		if(viewSite != null){
			IWorkbenchPage viewPage = viewSite.getPage();
			selection = viewPage.getSelection();
		}

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		logger.info("User opens Validation Rules View");
		
		IProject project = getSelectedProject();

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		createColumns(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new RuleSorter());
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider

		viewer.setInput(ValidationRulesProvider.getInstance(project)
				.getRuleset());

		getSite().setSelectionProvider(viewer);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				"edu.uncc.sis.aside.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	public TableViewer getViewer() {
		return viewer;
	}

	/*
	 * Creates columns for the table
	 */
	private void createColumns(TableViewer viewer) {

		String[] titles = { "Rule ID", "Rule Pattern", "Default Value",
				"Source File", "Type of Rule" };
		int[] bounds = { 200, 400, 200, 200, 100 };

		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);

			// enable editing support
			column
					.setEditingSupport(new ValidationRuleEditingSupport(viewer,
							i));
		}

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ValidationRulesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(addRow);
		manager.add(new Separator());
		manager.add(deleteSelectedRow);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addRow);
		manager.add(deleteSelectedRow);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(addRow);
		manager.add(deleteSelectedRow);
	}

	private void makeActions() {
		addRow = new Action() {
			public void run() {
				// TODO more complicated operations are needed
				showMessage("Action 1 executed");
			}
		};
		addRow.setText("Add a Row Below");
		addRow.setToolTipText("To add another validaiton rule");
		addRow.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		deleteSelectedRow = new Action() {
			public void run() {
				// TODO more complicated operations are needed
				showMessage("Action 2 executed");
			}
		};
		deleteSelectedRow.setText("Delete Selected Row");
		deleteSelectedRow.setToolTipText("To delete a validation rule");
		deleteSelectedRow.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Validation Rules View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//			viewer.setInput(ValidationRulesProvider.getInstance(project)
//					.getRuleset());
		}

		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				initialize();
			}
			LinkedList<ValidationRule> ruleset = (LinkedList<ValidationRule>) parent;
			return ruleset.toArray();
		}
	}

	/*
	 * TODO Find the rule pack file in the current project and read rules from
	 * it.
	 */
	private void initialize() {
		System.out.println("nothing should happen here");
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		// ASIDE specific icons to be used in this view
		private final Image KEY = AsidePlugin.getImageDescriptor(
				"icons/uranus.png").createImage();

		public String getColumnText(Object obj, int columnIndex) {
			ValidationRule rule = (ValidationRule) obj;
			switch (columnIndex) {
			case 0:
				return rule.getRuleKey();
			case 1:
				return rule.getRuleValue();
			case 2:
				return rule.getDefaultValue();
			case 3:
				return rule.getSourceFile();
			case 4:
				return rule.getType().toString();
			default:
				throw new RuntimeException("Should never happen");
			}
		}

		public Image getColumnImage(Object obj, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return KEY;
			case 1:
			case 2:
			case 3:
			case 4:
			default:
				return null;
			}
		}

		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					imageKey);
		}
	}

	class ValidationRuleEditingSupport extends EditingSupport {

		private CellEditor editor = null;
		private int columnIndex;

		public ValidationRuleEditingSupport(ColumnViewer viewer, int columnIndex) {
			super(viewer);

			if (columnIndex == 0 || columnIndex == 1 || columnIndex == 2) {
				editor = new TextCellEditor(((TableViewer) viewer).getTable());
			}
			this.columnIndex = columnIndex;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof ValidationRule) {
				ValidationRule rule = (ValidationRule) element;
				if ((rule.getType()).equals(RuleType.Type.DEFAULT)) {
					return false;
				} else {
					switch (columnIndex) {
					case 0:
					case 1:
					case 2:
						return true;
					case 3:
					case 4:
					default:
						return false;
					}
				}

			}
			return false;

		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			ValidationRule rule = (ValidationRule) element;
			switch (this.columnIndex) {
			case 0:
				return rule.getRuleKey();
			case 1:
				return rule.getRuleValue();
			case 2:
				return rule.getDefaultValue();
			case 3:
				return rule.getSourceFile();
			case 4:
			default:
				break;
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			ValidationRule rule = (ValidationRule) element;
			// necessary validation of the supplied value is needed to ensure
			// the rules file does not get polluted

			switch (this.columnIndex) {
			case 0:
				rule.setRuleKey(String.valueOf(value));
				break;
			case 1:
				rule.setRuleValue(String.valueOf(value));
				break;
			case 2:
				rule.setDefaultValue(String.valueOf(value));
				break;
			case 3:
			case 4:
			default:
				break;
			}
			// TODO this update does only affect the viewer, not the model
			getViewer().update(element, null);
		}

	}

	class RuleSorter extends ViewerSorter {

		private int propertyIndex, direction;
		private static final int DESCENDING = 1;

		// Constructor
		public RuleSorter() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int columnIndex) {
			if (columnIndex == this.propertyIndex) {
				// toggle the direction if current column has just been sorted
				direction = 1 - direction;
			} else {
				this.propertyIndex = columnIndex;
				// initialized direction
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			ValidationRule item1 = (ValidationRule) e1;
			ValidationRule item2 = (ValidationRule) e2;
			int compareResult = 0;
			switch (propertyIndex) {
			case 0:
				compareResult = item1.getRuleKey()
						.compareTo(item2.getRuleKey());
				break;
			case 1:
				compareResult = item1.getRuleValue().compareTo(
						item2.getRuleValue());
				break;
			case 2:
			case 3:
			case 4:
			default:
				break;
			}
			if (direction == DESCENDING) {
				compareResult = -compareResult;
			}
			return compareResult;
		}

	}

	private IProject getSelectedProject() {

		IProject project = null;

		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof IResource) {
				IResource resource = (IResource) firstElement;
				IProject currentProject = resource.getProject();

				try {
					if (currentProject != null
							&& currentProject
									.hasNature("org.eclipse.jdt.core.javanature")) {
						project = currentProject;
					}
				} catch (CoreException e) {

				}
			}
		}
		return project;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO response to the changes on the source file of displayed rules
		// write back to model
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			System.out.println("Change");
		}
	}
}