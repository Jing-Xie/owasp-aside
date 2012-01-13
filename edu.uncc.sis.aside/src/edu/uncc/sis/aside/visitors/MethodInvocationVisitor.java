package edu.uncc.sis.aside.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.domainmodels.TrustBoundaryRepository;
import edu.uncc.sis.aside.preferences.PreferencesSet;
import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;
import edu.uncc.sis.aside.utils.Converter;

/**
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class MethodInvocationVisitor extends ASTVisitor {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			MethodInvocationVisitor.class.getName());

	private static final List<String> JAVA_MAP_TYPES = Arrays.asList(
			"java.util.Map", "java.util.HashMap", "java.util.AbstractMap",
			"java.util.Attributes", "java.util.Hashtable",
			"java.util.IdentityHashMap", "java.util.RenderingHints",
			"java.util.TreeMap", "java.util.WeakHashMap");

	private static final List<String> JAVA_LIST_TYPES = Arrays.asList(
			"java.util.List", "java.util.AbstractList", "java.util.ArrayList",
			"java.util.LinkedList", "java.util.Vector", "java.util.Stack");

	private ASTMatcher astMatcher;

	private MethodDeclaration acceptor;

	private ArrayList<IMarker> asideMarkers;

	private IProject project;

	private ICompilationUnit cu;

	private CompilationUnit astRoot;

	private PreferencesSet prefSet;

	// Expression: SimpleName && MethodInvocation
	private ArrayList<Expression> taintedListSources = new ArrayList<Expression>();

	// Expression: ArrayAccess && MethodInvocation
	private ArrayList<Expression> taintedMapSources = new ArrayList<Expression>();

	/**
	 * Constructor
	 * 
	 * @param prefSet
	 */
	public MethodInvocationVisitor(MethodDeclaration node,
			ArrayList<IMarker> existingAsideMarkers, ICompilationUnit cu,
			PreferencesSet prefSet) {
		super();
		acceptor = node;
		this.cu = cu;

		this.prefSet = prefSet;

		if (existingAsideMarkers != null && !existingAsideMarkers.isEmpty()) {
			ASIDEMarkerAndAnnotationUtil
					.clearStaleMarkers(existingAsideMarkers);
		}

		if (asideMarkers == null) {
			asideMarkers = new ArrayList<IMarker>();
		}

		astRoot = ASTResolving.findParentCompilationUnit(node);

		IJavaProject javaProject = cu.getJavaProject();
		if (javaProject != null) {
			project = (IProject) javaProject.getAdapter(IProject.class);

		}

		astMatcher = AsidePlugin.getDefault().getASTMatcher();
	}

	public ArrayList<IMarker> process() {
		if (acceptor != null)
			acceptor.accept(this);
		return asideMarkers;
	}

	@Override
	public boolean visit(MethodInvocation node) {

		IMethodBinding methodBinding = node.resolveMethodBinding();

		if (methodBinding == null) {
			return false;
		}

		ITypeBinding returnTypeBinding = methodBinding.getReturnType();

		if (returnTypeBinding == null) {
			return false;
		}

		String qualifiedName = returnTypeBinding.getQualifiedName();
		boolean pre_tainted = TrustBoundaryRepository.getHandler(project,
				prefSet).isExist(methodBinding, true, qualifiedName);
		String[] attrTypes = TrustBoundaryRepository.getHandler(project,
				prefSet).getAttrTypes();

		Statement parentStatement = ASTResolving.findParentStatement(node);

		/* the marker to be created and attached to the corresponding node */
		IMarker marker = null;

		Map<String, Object> markerAttributes = new HashMap<String, Object>();

		int lineNumber = astRoot.getLineNumber(node.getStartPosition());

		/*
		 * aside marker attributes which will be used for the corresponding
		 * marker resolutions
		 */
		markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
		markerAttributes.put("edu.uncc.sis.aside.marker.flow",
				Converter.arrayToString(attrTypes));

		if (!pre_tainted) {

			if (taintedMapSources.isEmpty()) {
				return true;
			}

			if (!ASIDEMarkerAndAnnotationUtil
					.isControlStatementWithBlock(parentStatement)
					&& !ASIDEMarkerAndAnnotationUtil
							.isControlStatementWithBlock(parentStatement
									.getParent())
					&& !ASIDEMarkerAndAnnotationUtil.specialCase(node)) {
				int start = node.getStartPosition();
				int length = node.getLength();
				int end = start + length;

				if (!ASIDEMarkerAndAnnotationUtil.hasAnnotationAtPosition(cu,
						start, length)) {
					if (qualifiedName.equals("java.lang.String")
							|| qualifiedName.equals("java.lang.Object")) {
						Expression methodExpression = node.getExpression();
						SimpleName methodName = node.getName();
						/* Object value = Map.get("key"); */
						if (methodExpression != null
								&& methodName.getIdentifier() != null
								&& methodName.getIdentifier().equals("get")) {
							int invokeExpressionType = methodExpression
									.getNodeType();
							switch (invokeExpressionType) {
							case ASTNode.METHOD_INVOCATION:

								MethodInvocation target = (MethodInvocation) methodExpression;

								if (ASIDEMarkerAndAnnotationUtil.isTainted(
										target, taintedMapSources, astMatcher)) {

									if (!ASIDEMarkerAndAnnotationUtil
											.specialCase(target)) {

										markerAttributes.put(
												IMarker.CHAR_START, start);
										markerAttributes.put(IMarker.CHAR_END,
												end);
										// TODO: make message more concrete and
										// meaningful
										markerAttributes
												.put(IMarker.MESSAGE,
														"The return value of "
																+ methodBinding
																		.getName()
																+ "() at line "
																+ lineNumber
																+ " is vulnerable to be manipulated by malicious users");

										markerAttributes
												.put("edu.uncc.sis.aside.marker.validationType",
														"String");
										marker = ASIDEMarkerAndAnnotationUtil
												.addMarker(astRoot,
														markerAttributes);

										asideMarkers.add(marker);
										logger.info("Found vulnerability at line "
												+ lineNumber
												+ " in Java File <<"
												+ astRoot.getJavaElement()
														.getElementName()
												+ ">> in Project =="
												+ project.getName() + "==");
									}

								}
								break;
							case ASTNode.SIMPLE_NAME:
								if (ASIDEMarkerAndAnnotationUtil.isTainted(
										(SimpleName) methodExpression,
										taintedMapSources)) {

									markerAttributes.put(IMarker.CHAR_START,
											start);
									markerAttributes.put(IMarker.CHAR_END, end);
									markerAttributes
											.put(IMarker.MESSAGE,
													"The return value of "
															+ methodBinding
																	.getName()
															+ "() at line "
															+ lineNumber
															+ " is vulnerable to be manipulated by malicious users");

									markerAttributes
											.put("edu.uncc.sis.aside.marker.validationType",
													"String");
									marker = ASIDEMarkerAndAnnotationUtil
											.addMarker(astRoot,
													markerAttributes);

									asideMarkers.add(marker);
									logger.info("Found vulnerability at line "
											+ lineNumber
											+ " in Java File <<"
											+ astRoot.getJavaElement()
													.getElementName()
											+ ">> in Project =="
											+ project.getName() + "==");

								}
								break;
							default:
								break;
							}
						}

					}

					if (qualifiedName.equals("int")) {
						// TODO
					}
				}

			}

			return true;
		}

		if (JAVA_MAP_TYPES.contains(qualifiedName)) {
			taintedMapSources.add(node);
			StructuralPropertyDescriptor location = node.getLocationInParent();
			if (location.isChildProperty()) {
				ASTNode parent = node.getParent();
				if (parent.getNodeType() == ASTNode.ASSIGNMENT) {
					if (node.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
						Assignment assignmentParent = (Assignment) parent;
						Expression assignmentExpression = assignmentParent
								.getLeftHandSide();
						taintedMapSources.add(assignmentExpression);
					}
				}

				if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
					if (node.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
						VariableDeclarationFragment variableDeclarationParent = (VariableDeclarationFragment) parent;
						SimpleName variable = variableDeclarationParent
								.getName();
						taintedMapSources.add(variable);
					}
				}

			}
		}

		// See ArrayAccessVisitor.java
		if (JAVA_LIST_TYPES.contains(qualifiedName)) {
			taintedListSources.add(node);
			StructuralPropertyDescriptor location = node.getLocationInParent();
			if (location.isChildProperty()) {
				ASTNode parent = node.getParent();
				if (parent.getNodeType() == ASTNode.ASSIGNMENT) {
					if (node.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
						Assignment assignmentParent = (Assignment) parent;
						Expression assignmentExpression = assignmentParent
								.getLeftHandSide();
						taintedListSources.add(assignmentExpression);
					}
				}

				if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
					if (node.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
						VariableDeclarationFragment variableDeclarationParent = (VariableDeclarationFragment) parent;
						SimpleName variable = variableDeclarationParent
								.getName();
						taintedListSources.add(variable);
					}
				}

			}
		}

		/*
		 * the method invocation under examination matches one of the trust
		 * boundary, next, check the location of this method invocation
		 */
		// Statement parentStatement = ASTResolving.findParentStatement(node);

		if (!ASIDEMarkerAndAnnotationUtil
				.isControlStatementWithBlock(parentStatement)
				&& !ASIDEMarkerAndAnnotationUtil
						.isControlStatementWithBlock(parentStatement
								.getParent())
				&& !ASIDEMarkerAndAnnotationUtil.specialCase(node)) {

			if (qualifiedName.equals("java.lang.String")) {

				int start = node.getStartPosition();
				int length = node.getLength();
				int end = start + length;

				if (!ASIDEMarkerAndAnnotationUtil.hasAnnotationAtPosition(cu,
						start, length)) {

					markerAttributes.put(IMarker.CHAR_START, start);
					markerAttributes.put(IMarker.CHAR_END, end);
					markerAttributes
							.put(IMarker.MESSAGE,
									"The return value of "
											+ methodBinding.getName()
											+ "() at line "
											+ lineNumber
											+ " is vulnerable to be manipulated by malicious users");

					markerAttributes.put(
							"edu.uncc.sis.aside.marker.validationType",
							"String");
					marker = ASIDEMarkerAndAnnotationUtil.addMarker(astRoot,
							markerAttributes);

					asideMarkers.add(marker);
					logger.info("Found vulnerability at line " + lineNumber
							+ " in Java File <<"
							+ astRoot.getJavaElement().getElementName()
							+ ">> in Project ==" + project.getName() + "==");
				}
			} else if (qualifiedName.equals("int")) {

				int start = node.getStartPosition();
				int length = node.getLength();
				int end = start + length;

				if (!ASIDEMarkerAndAnnotationUtil.hasAnnotationAtPosition(cu,
						start, length)) {
					markerAttributes.put(IMarker.CHAR_START, start);
					markerAttributes.put(IMarker.CHAR_END, end);
					markerAttributes
							.put(IMarker.MESSAGE,
									"The return value of "
											+ methodBinding.getName()
											+ " at line "
											+ lineNumber
											+ " is vulnerable to be manipulated by malicious users");

					markerAttributes.put(
							"edu.uncc.sis.aside.marker.validationType", "int");
					marker = ASIDEMarkerAndAnnotationUtil.addMarker(astRoot,
							markerAttributes);
					asideMarkers.add(marker);
					logger.info("Found vulnerability at line " + lineNumber
							+ " in Java File <<"
							+ astRoot.getJavaElement().getElementName()
							+ ">> in Project ==" + project.getName() + "==");
				}

			} else if (qualifiedName.equals("void")) {
				/*
				 * the specified argument node should be java.lang.String type
				 */

				int[] arguments = TrustBoundaryRepository.getHandler(project,
						prefSet).getArguments();
				ArrayList<IMarker> argumentMarkerList = new ArrayList<IMarker>();
				List<Expression> argumentNodeList = node.arguments();

				for (int index = 0; index < arguments.length; index++) {
					Expression argumentNode = argumentNodeList.get(index);

					int argCharStart = argumentNode.getStartPosition();

					int argLength = argumentNode.getLength();

					if (!ASIDEMarkerAndAnnotationUtil.hasAnnotationAtPosition(
							cu, argCharStart, argLength)) {

						/*
						 * TODO: Check the validity of each argument node to eliminate
						 * false positives
						 */

						if(ASIDEMarkerAndAnnotationUtil.isFalsePositive(argumentNode, argCharStart, argLength, cu)){
							continue;
						}
						
						markerAttributes.put(IMarker.CHAR_START,
								argumentNode.getStartPosition());
						markerAttributes.put(
								IMarker.CHAR_END,
								argumentNode.getStartPosition()
										+ argumentNode.getLength());
						markerAttributes
								.put(IMarker.MESSAGE,
										"The argument at "
												+ index
												+ " of "
												+ methodBinding.getName()
												+ "() at line "
												+ lineNumber
												+ " is vulnerable to be manipulated by malicious users");

						markerAttributes.put(
								"edu.uncc.sis.aside.marker.targetPosition",
								index);
						marker = ASIDEMarkerAndAnnotationUtil.addMarker(
								astRoot, markerAttributes);
						argumentMarkerList.add(marker);
					}
				}
				if (!argumentMarkerList.isEmpty()) {
					asideMarkers.addAll(argumentMarkerList);

					logger.info("Found vulnerability at line " + lineNumber
							+ " in Java File <<"
							+ astRoot.getJavaElement().getElementName()
							+ ">> in Project ==" + project.getName() + "==");
				}
			}

		}

		// record the location of this method invocation in its ancestor
		return false;
	}

	@Override
	public void endVisit(MethodInvocation node) {

		super.endVisit(node);
	}

	public ArrayList<Expression> getTaintedListSources() {
		return taintedListSources;
	}

}
