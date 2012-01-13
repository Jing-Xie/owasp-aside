package edu.uncc.sis.aside.popup.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.visitors.MethodDeclarationVisitor;

/**
 * Application Security IDE Plugin (ASIDE)
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 */
public class ManuallyLaunchAsideOnTargetAction implements IObjectActionDelegate {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			ManuallyLaunchAsideOnTargetAction.class.getName());

	private static final String ASIDE_MARKER_TYPE = "edu.uncc.sis.aside.AsideMarker";

	private IAction targetAction;
	private IWorkbenchPart targetWorkbench;

	private static Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> projectMarkerMap = null;

	public ManuallyLaunchAsideOnTargetAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		logger.info("\n\n\nUser clicks Run ASIDE from the context menu\nLaunching ASIDE");

		// scan the selected target's project presentation
		if (targetWorkbench == null) {
			return;
		}

		ISelectionProvider selectionProvider = targetWorkbench.getSite()
				.getSelectionProvider();

		if (selectionProvider == null) {
			return;
		}

		ISelection selection = selectionProvider.getSelection();

		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			Object firstElement = structuredSelection.getFirstElement();

			try {
				if (firstElement != null && firstElement instanceof IResource) {
					IResource resource = (IResource) firstElement;
					IProject project = resource.getProject();
					IJavaProject javaProject = (IJavaProject) project
							.getNature(JavaCore.NATURE_ID);

					inspectOnProject(javaProject);
				} else if (firstElement != null
						&& firstElement instanceof IJavaElement) {
					IResource resource = (IResource) ((IJavaElement) firstElement)
							.getAdapter(IResource.class);
					if (resource != null) {
						IProject project = resource.getProject();

						IJavaProject javaProject = (IJavaProject) project
								.getNature(JavaCore.NATURE_ID);

						inspectOnProject(javaProject);
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

		// At last, set the CompliationParticipant active
		if (!AsidePlugin.getDefault().getSignal()) {
			AsidePlugin.getDefault().setSignal(true);
		}
	}

	private void inspectOnProject(IJavaProject project)
			throws JavaModelException, CoreException {

		if (project == null) {
			return;
		}

		logger.info("inspecting project: " + project.getElementName());

		project.getCorrespondingResource().deleteMarkers(ASIDE_MARKER_TYPE,
				false, IResource.DEPTH_INFINITE);

		projectMarkerMap = AsidePlugin.getDefault().getMarkerIndex(project);

		if (projectMarkerMap == null) {
			projectMarkerMap = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();
		}

		IPackageFragment[] packageFragmentsInProject = project
				.getPackageFragments();
		for (IPackageFragment fragment : packageFragmentsInProject) {
			ICompilationUnit[] units = fragment.getCompilationUnits();
			for (ICompilationUnit unit : units) {

				Map<MethodDeclaration, ArrayList<IMarker>> fileMap = inspectOnJavaFile(unit);
				projectMarkerMap.put(unit, fileMap);
			}
		}

		// At last, put it back to Aside Plugin
		AsidePlugin.getDefault().setMarkerIndex(project, projectMarkerMap);
	}

	private Map<MethodDeclaration, ArrayList<IMarker>> inspectOnJavaFile(
			ICompilationUnit unit) {
		logger.info("inspecting java file: " + unit.getElementName());

		CompilationUnit astRoot = parse(unit);
		// PreferencesSet set = new PreferencesSet(true, true, false, new
		// String[0]);
		MethodDeclarationVisitor declarationVisitor = new MethodDeclarationVisitor(
				astRoot, null, unit, null);
		return declarationVisitor.process();

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart workbench) {
		this.targetAction = action;
		this.targetWorkbench = workbench;

	}

	private CompilationUnit parse(ICompilationUnit unit) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(unit);

		return (CompilationUnit) parser.createAST(null);

	}

}
