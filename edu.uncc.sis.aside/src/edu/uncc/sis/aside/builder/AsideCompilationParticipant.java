package edu.uncc.sis.aside.builder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.visitors.MethodDeclarationVisitor;
import edu.uncc.sis.aside.visitors.MethodInvocationVisitor;

/**
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class AsideCompilationParticipant extends CompilationParticipant {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(AsideCompilationParticipant.class.getName());
	
	private static CompilationUnit compilationUnitASTBeforeReconcile = null;

	private static ASTMatcher astMatcher = null;

	private static Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> projectMarkerMap = null;
	
	private int counter = 0;

	/*
	 * 0-argument constructor as required by extension point
	 */
	public AsideCompilationParticipant() {

		if (astMatcher == null) {
			astMatcher = AsidePlugin.getDefault().getASTMatcher();
		}

	}

	@Override
	public int aboutToBuild(IJavaProject project) {

		return READY_FOR_BUILD;
	}

	@Override
	public boolean isActive(IJavaProject project) {

		/*
		 * attach a hidden aside file and an audit file to the project if the
		 * project does not have one yet
		 */

		try {

			IProject resourceProject = (IProject) project
					.getAdapter(IProject.class);
			if (resourceProject != null) {

				String description = "Project: " + resourceProject.getName() + "\nLocation: "
						+ resourceProject.getLocation().toOSString() + "\n";

				IFile aside_detection = resourceProject.getFile(".aside");

				if (aside_detection == null || !aside_detection.exists()) {
					aside_detection.create(new ByteArrayInputStream(description
							.getBytes()), false, null);
					ResourceAttributes asideInfoAttributes = new ResourceAttributes();
					asideInfoAttributes.setHidden(true);
					asideInfoAttributes.setReadOnly(false);
					aside_detection.setResourceAttributes(asideInfoAttributes);
				}
				
                /*
				IFile asideLog = resourceProject.getFile("aside.audit");

				if (asideLog == null || !asideLog.exists()) {
					asideLog.create(new ByteArrayInputStream(description
							.getBytes()), false, null);
					ResourceAttributes asideLogAttributes = new ResourceAttributes();
					asideLogAttributes.setHidden(false);
					asideLogAttributes.setReadOnly(true);
					aside_detection.setResourceAttributes(asideLogAttributes);
				}
				*/
			}
		} catch (CoreException e) {
		}

		return true;
	}

	@Override
	public void reconcile(ReconcileContext context) {
		
		if(!AsidePlugin.getDefault().getSignal()){// Avoid reconciling
			return;
		}
		
		counter ++;
		logger.info("RECONCILING... " + counter);

		try {
				
			IJavaElementDelta javaElementDelta = context.getDelta();

			if(javaElementDelta == null){
				return;
			}
			IJavaProject project = javaElementDelta.getElement()
					.getJavaProject();

			if(project == null){
				return;
			}
			
			projectMarkerMap = AsidePlugin.getDefault().getMarkerIndex(project);

			if (projectMarkerMap == null) {
				projectMarkerMap = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();
			}

			int kind = javaElementDelta.getKind();

			/* consider changes on one java file only */
			if (kind != IJavaElementDelta.CHANGED) {
				return;
			}

			int flags = javaElementDelta.getFlags();
			if ((flags & IJavaElementDelta.F_CONTENT) != 0
					|| (flags & IJavaElementDelta.F_AST_AFFECTED) != 0) {

				CompilationUnit compilationUnitASTAfterReconcile = context
						.getAST3();
                
				if(compilationUnitASTAfterReconcile == null){
					return;
				}
				
				ICompilationUnit cu = (ICompilationUnit) compilationUnitASTAfterReconcile
				.getJavaElement().getPrimaryElement();
				
				Map<MethodDeclaration, ArrayList<IMarker>> markersMap = projectMarkerMap
						.get(cu);

				if (compilationUnitASTBeforeReconcile == null) {
					// inspect on this compilation unit AST

					MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor(
							compilationUnitASTAfterReconcile, markersMap, cu, null);
					markersMap = methodDeclarationVisitor.process();

					if (markersMap == null) {
						markersMap = new HashMap<MethodDeclaration, ArrayList<IMarker>>();
					}

					projectMarkerMap.put(
							(ICompilationUnit) compilationUnitASTAfterReconcile
									.getJavaElement().getPrimaryElement(),
							markersMap);

					/* store the AST after this reconcile process for next round
					 reconcile */

					compilationUnitASTBeforeReconcile = compilationUnitASTAfterReconcile;

					AsidePlugin.getDefault().setMarkerIndex(project,
							projectMarkerMap);

					return;
				}

				if (astMatcher.match(compilationUnitASTBeforeReconcile,
						compilationUnitASTAfterReconcile)) {
					// no change on the structure of the ICompilationUnit
					return;
				}

				/* Compare ASTs before and after reconciling, locate the
				   method declaration within which the change happened */

				List<TypeDeclaration> types = compilationUnitASTAfterReconcile
						.types();

				if(markersMap == null){
					markersMap = new HashMap<MethodDeclaration, ArrayList<IMarker>>();
				}
				Set<MethodDeclaration> methodsBefore = markersMap.keySet();

				Map<MethodDeclaration, ArrayList<IMarker>> newMap = new HashMap<MethodDeclaration, ArrayList<IMarker>>();

				for (TypeDeclaration type : types) {
					MethodDeclaration[] methodsAfter = type.getMethods();

					for (MethodDeclaration matchee : methodsAfter) {
						MethodDeclaration member = getMatchInSet(matchee,
								methodsBefore);

						if (member != null) {
							newMap.put(member, markersMap.get(member));

							markersMap.remove(member);

						} else if (member == null) {
							MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor(
									matchee, null, cu, null);
							ArrayList<IMarker> markers = methodInvocationVisitor
									.process();

							newMap.put(matchee, markers);
						}

					}

				}

				if (!markersMap.isEmpty()) {
					Collection<ArrayList<IMarker>> values = markersMap.values();

					for (ArrayList<IMarker> value : values) {
						if (value != null && !value.isEmpty()) {
							for (IMarker marker : value) {
								if (marker.exists()) {
									marker.delete();
								}
							}
						}
					}
				}

				projectMarkerMap.put(
						(ICompilationUnit) compilationUnitASTAfterReconcile
								.getJavaElement().getPrimaryElement(), newMap);
				/* store the AST after this reconcile process for next round
				   reconcile */
				compilationUnitASTBeforeReconcile = compilationUnitASTAfterReconcile;
			}

			AsidePlugin.getDefault().setMarkerIndex(project, projectMarkerMap);

		} catch (JavaModelException e) {

		} catch (CoreException e) {

		}
	}

	private MethodDeclaration getMatchInSet(MethodDeclaration matchee,
			Set<MethodDeclaration> pool) {

		for (MethodDeclaration member : pool) {

			if (astMatcher.match(matchee, member)) {
				return member;
			}
		}

		return null;
	}

}
