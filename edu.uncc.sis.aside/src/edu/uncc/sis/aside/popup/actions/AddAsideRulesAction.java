package edu.uncc.sis.aside.popup.actions;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.uncc.sis.aside.AsidePlugin;

public class AddAsideRulesAction implements IObjectActionDelegate {

	private static final Logger logger = AsidePlugin.getLogManager()
	.getLogger(AddAsideRulesAction.class.getName());
	
	private static final String ASIDE_NATURE_ID = "edu.uncc.sis.aside.AsideNature";

	private IWorkbenchPart workbenchPart;
	private IAction targetAction;

	public AddAsideRulesAction() {
		super();
	}

	@Override
	public void run(IAction action) {

		logger.info("USER issues ADD RULES TO PROJECT command from Project Explore context menu");
		
		if (targetAction.getStyle() != IAction.AS_PUSH_BUTTON) {
			return;
		}

		ISelectionProvider selectionProvider = workbenchPart.getSite()
				.getSelectionProvider();

		if (selectionProvider == null) {
			return;
		}

		ISelection selection = selectionProvider.getSelection();

		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			Object firstElement = structuredSelection.getFirstElement();

			if (firstElement != null && firstElement instanceof IResource) {
				IJavaElement javaElement = (IJavaElement) ((IResource) firstElement)
						.getAdapter(IJavaElement.class);

				generateAsideRules(javaElement);
			} else if (firstElement != null
					&& firstElement instanceof IJavaElement) {
				generateAsideRules((IJavaElement) firstElement);
			}

		}

	}

	private void generateAsideRules(IJavaElement javaElement) {

		try {
			if (javaElement == null) {
				return;
			}

			IJavaProject javaProject = javaElement.getJavaProject();

			IProject project = (IProject) javaProject
					.getAdapter(IProject.class);

			if (project == null || !project.exists()) {
				return;
			}

			IFolder asideRulesFolder = project.getFolder("aside-rules");

			IFile trustBoundariesFile, validationRulesFile;

			String templateString1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TrustBoundaries>\n\n</TrustBoundaries>";
			String templateString2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ValidationPatterns>\n\n</ValidationPatterns>";

			if (asideRulesFolder.exists()) {
				trustBoundariesFile = asideRulesFolder
						.getFile("trust-boundaries.xml");
				if (!trustBoundariesFile.exists()) {
					trustBoundariesFile.create(new ByteArrayInputStream(
							templateString1.getBytes()), false, null);
				}

				validationRulesFile = asideRulesFolder
						.getFile("validation-rules.xml");
				if (!validationRulesFile.exists()) {
					validationRulesFile.create(new ByteArrayInputStream(
							templateString2.getBytes()), false, null);
				}
			}

			asideRulesFolder.create(false, false, null);
			trustBoundariesFile = asideRulesFolder
					.getFile("trust-boundaries.xml");

			trustBoundariesFile.create(new ByteArrayInputStream(templateString1
					.getBytes()), false, null);

			validationRulesFile = asideRulesFolder
					.getFile("validation-rules.xml");

			validationRulesFile.create(new ByteArrayInputStream(templateString2
					.getBytes()), false, null);

		} catch (CoreException e) {
		}

	}

	private void attachOrDetachAsideNatureToJavaProject(IJavaElement javaElement) {

		if (javaElement == null) {
			return;
		}
		try {
			IJavaProject javaProject = javaElement.getJavaProject();

			IProject project = (IProject) javaProject
					.getAdapter(IProject.class);

			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			if (project.hasNature(ASIDE_NATURE_ID)) {

				String[] new_natures = new String[natures.length - 1];

				// Remove the nature
				for (int i = 0; i < natures.length; ++i) {
					if (natures[i].equals(ASIDE_NATURE_ID)) {
						System.arraycopy(natures, 0, new_natures, 0, i);
						System.arraycopy(natures, i + 1, new_natures, i,
								natures.length - i - 1);
						description.setNatureIds(new_natures);
						project.setDescription(description, null);
						return;
					}

				}

			} else if (!project.hasNature(ASIDE_NATURE_ID)) {

				for (int j = 0; j < natures.length; ++j) {
					if (natures[j].equals(ASIDE_NATURE_ID)) {
						return;
					}
				}

				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = ASIDE_NATURE_ID;
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
			}

		} catch (CoreException e) {

		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetAction = action;
		this.workbenchPart = targetPart;
	}

}
