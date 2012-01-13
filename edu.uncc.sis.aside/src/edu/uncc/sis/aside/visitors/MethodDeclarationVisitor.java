package edu.uncc.sis.aside.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.uncc.sis.aside.preferences.PreferencesSet;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

/**
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class MethodDeclarationVisitor extends ASTVisitor {

	private CompilationUnit acceptor;
	private ICompilationUnit cu;

	private Map<MethodDeclaration, ArrayList<IMarker>> markersMap;
	private ArrayList<IMarker> asideMarkers;

	private Map<MethodDeclaration, ArrayList<IMarker>> existingMarkersMapForICompilationUnit;

	private PreferencesSet prefSet;

	/**
	 * Constructor
	 * 
	 * @param compilationUnit
	 * @param _existingMarkersMapForICompilationUnit
	 * @param cu
	 * @param prefSet
	 */
	public MethodDeclarationVisitor(
			CompilationUnit compilationUnit,
			Map<MethodDeclaration, ArrayList<IMarker>> _existingMarkersMapForICompilationUnit,
			ICompilationUnit cu, PreferencesSet prefSet) {
		super();
		acceptor = compilationUnit;
		this.cu = cu;
		existingMarkersMapForICompilationUnit = _existingMarkersMapForICompilationUnit;

		if (markersMap == null) {
			markersMap = new HashMap<MethodDeclaration, ArrayList<IMarker>>();
		}
		asideMarkers = new ArrayList<IMarker>();
		this.prefSet = prefSet;
	}

	@Override
	public void endVisit(MethodDeclaration node) {

		markersMap.put(node, asideMarkers);
		super.endVisit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {

		/*
		 * First, test whether this method declaration is THE main entrance
		 * method for Java applications
		 */

		boolean main = ASIDEMarkerAndAnnotationUtil.isMainEntrance(cu, node);

		MethodInvocationVisitor methodInvocationVisitor = null;
		ArrayAccessVisitor arrayAccessVisitor = null;

		if (existingMarkersMapForICompilationUnit == null
				|| existingMarkersMapForICompilationUnit.isEmpty()) {

			methodInvocationVisitor = new MethodInvocationVisitor(node, null,
					cu, prefSet);

			asideMarkers = methodInvocationVisitor.process();

		/*	if (main) {
				arrayAccessVisitor = new ArrayAccessVisitor(node, null, null);
				asideMarkers.addAll(arrayAccessVisitor.process());
			}
*/
		} else {

			MethodDeclaration matchee = ASIDEMarkerAndAnnotationUtil
					.getMatchee(node,
							existingMarkersMapForICompilationUnit.keySet());

			if (matchee != null) {

				ArrayList<IMarker> existingMarkers = existingMarkersMapForICompilationUnit
						.get(matchee);

				methodInvocationVisitor = new MethodInvocationVisitor(node,
						existingMarkers, cu, prefSet);
				asideMarkers = methodInvocationVisitor.process();

				/*if (main) {
					ArrayList<IMarker> existingArrayAccessMarkers = extractArrayAccessMarkers(existingMarkers);
					arrayAccessVisitor = new ArrayAccessVisitor(node, null,
							existingArrayAccessMarkers);
					asideMarkers.addAll(arrayAccessVisitor.process());
				}
*/
				existingMarkersMapForICompilationUnit.remove(matchee);

			} else if (matchee == null) {

				methodInvocationVisitor = new MethodInvocationVisitor(node,
						null, cu, prefSet);

				asideMarkers = methodInvocationVisitor.process();

			/*	if (main) {
					arrayAccessVisitor = new ArrayAccessVisitor(node, null,
							null);
					asideMarkers.addAll(arrayAccessVisitor.process());
				}*/
			}

			try {
				if (!existingMarkersMapForICompilationUnit.isEmpty()) {

					Collection<ArrayList<IMarker>> values = existingMarkersMapForICompilationUnit
							.values();

					for (ArrayList<IMarker> value : values) {
						if (value != null && !value.isEmpty()) {
							for (IMarker marker : value) {
								if (marker != null && marker.exists()) {
									marker.delete();
								}
							}
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

		return false;
	}

	private ArrayList<IMarker> extractArrayAccessMarkers(
			ArrayList<IMarker> existingMarkers) {
		ArrayList<IMarker> temp = new ArrayList<IMarker>();
		try {
			for (IMarker marker : existingMarkers) {

				if (marker != null && marker.exists()) {
					String differentiator = (String) marker
							.getAttribute(IMarker.TEXT);

					if (differentiator != null
							&& differentiator.equals("ArrayAccess")) {
						temp.add(marker);
					}
				}

			}
		} catch (CoreException e) {

			e.printStackTrace();
		}

		return temp;
	}

	public Map<MethodDeclaration, ArrayList<IMarker>> process() {
		if (acceptor != null)
			acceptor.accept(this);
		return markersMap;
	}

}
