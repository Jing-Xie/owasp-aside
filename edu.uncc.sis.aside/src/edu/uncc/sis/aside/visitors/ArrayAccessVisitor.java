package edu.uncc.sis.aside.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class ArrayAccessVisitor extends ASTVisitor {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			ArrayAccessVisitor.class.getName());

	private MethodDeclaration parentMethodDeclaration;
	private ArrayList<Expression> taintedListSources;
	private ArrayList<IMarker> arrayAccessMarkers;

	private ICompilationUnit cu;
	private CompilationUnit astRoot;

	private String paramSearch;

	public ArrayAccessVisitor(MethodDeclaration parent,
			ArrayList<Expression> taintedListSources,
			ArrayList<IMarker> existingArrayAccessMarkers) {
		super();
		this.parentMethodDeclaration = parent;
		this.taintedListSources = taintedListSources;

		if (existingArrayAccessMarkers != null && !existingArrayAccessMarkers.isEmpty()) {
			ASIDEMarkerAndAnnotationUtil
					.clearStaleMarkers(existingArrayAccessMarkers);
		}

		if (arrayAccessMarkers == null) {
			arrayAccessMarkers = new ArrayList<IMarker>();
		}

		astRoot = ASTResolving.findParentCompilationUnit(parent);
		cu = (ICompilationUnit) astRoot.getJavaElement();
		/*
		 * TODO: for current implementation which targets only the main entrance
		 * method, we only examine the first argument of the method since main
		 * method has only one argument
		 */
		List<SingleVariableDeclaration> parameters = parent.parameters();
		if (!parameters.isEmpty()) {
			SingleVariableDeclaration target = parameters.get(0);
			paramSearch = target.getName().getIdentifier();
		}

	}

	@Override
	public boolean visit(ArrayAccess node) {

		/*
		 * if (taintedListSources.isEmpty()) { return false; }
		 */

		IMarker marker;

		Map<String, Object> markerAttributes = new HashMap<String, Object>();

		int lineNumber = astRoot.getLineNumber(node.getStartPosition());

		markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);

		Expression arrayExpression = node.getArray();
		if (arrayExpression instanceof SimpleName) {
			SimpleName expressionName = (SimpleName) arrayExpression;
			String name = expressionName.getIdentifier();
			int charStart = node.getStartPosition();
			int length = node.getLength();
			int charEnd = charStart + length;

			if (name.equals(paramSearch)
					&& !ASIDEMarkerAndAnnotationUtil.hasAnnotationAtPosition(
							cu, charStart, length)) {

				markerAttributes.put(IMarker.CHAR_START, charStart);
				markerAttributes.put(IMarker.CHAR_END, charEnd);
				markerAttributes
						.put(IMarker.MESSAGE,
								"The value represented by this array accessing code is vulnerable to be manipulated by malicious users.");
				markerAttributes.put(
						"edu.uncc.sis.aside.marker.validationType", "String");
				// This is the attribute that differentiates ArrayAccess markers with other node markers
				markerAttributes.put(IMarker.TEXT, "ArrayAccess");

				marker = ASIDEMarkerAndAnnotationUtil.addMarker(astRoot,
						markerAttributes);

				arrayAccessMarkers.add(marker);
			}
		}

		return false;
	}

	@Override
	public void endVisit(ArrayAccess node) {
		super.endVisit(node);
	}

	public ArrayList<IMarker> process() {
		if (parentMethodDeclaration != null)
			parentMethodDeclaration.accept(this);

		return arrayAccessMarkers;
	}

}
