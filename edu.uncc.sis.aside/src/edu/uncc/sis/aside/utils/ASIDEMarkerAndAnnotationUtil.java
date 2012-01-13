package edu.uncc.sis.aside.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import edu.uncc.sis.aside.AsidePlugin;

/**
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class ASIDEMarkerAndAnnotationUtil {

	private static final String ASIDE_MARKER_TYPE = "edu.uncc.sis.aside.AsideMarker";
	private static final String ANNOTATION_TYPE = "edu.uncc.sis.aside.invisibleAnnotation";

	public static IMarker addMarker(CompilationUnit compilationUnit,
			Map<String, Object> markerAttributes) {

		IMarker marker = null;

		try {

			IJavaElement javaElement = compilationUnit.getJavaElement();
			if (javaElement != null) {
				IFile file = (IFile) javaElement.getCorrespondingResource();
				marker = file.createMarker(ASIDE_MARKER_TYPE);
				marker.setAttributes(markerAttributes);
			}

		} catch (CoreException e) {
		}

		return marker;
	}

	public static void clearStaleMarkers(ArrayList<IMarker> markers) {
		try {
			for (IMarker marker : markers) {
				if (marker != null && marker.exists()) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
		}
	}

	public static boolean hasAnnotationAtPosition(ICompilationUnit cu,
			int charStart, int length) {

		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(cu.getPath(),
				LocationKind.IFILE);
		if (buffer == null) {
			return false;
		}

		IAnnotationModel model = buffer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();
		Annotation annotation;
		Position position;
		while (iterator.hasNext()) {
			annotation = iterator.next();
			if (annotation.getType().equals(ANNOTATION_TYPE)) {
				position = model.getPosition(annotation);

				if (position.getOffset() == charStart
						&& position.getLength() == length) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isTainted(MethodInvocation node,
			ArrayList<Expression> taintedMapSources, ASTMatcher astMatcher) {

		for (Expression exp : taintedMapSources) {
			if (exp.getNodeType() == ASTNode.METHOD_INVOCATION) {
				MethodInvocation methodInvocation = (MethodInvocation) exp;
				if (methodInvocation.subtreeMatch(astMatcher, node)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isTainted(SimpleName node,
			ArrayList<Expression> taintedMapSources) {
		IBinding binding = node.resolveBinding();

		for (Expression exp : taintedMapSources) {
			if (exp.getNodeType() == ASTNode.SIMPLE_NAME) {
				SimpleName temp = (SimpleName) exp;
				IBinding tempBinding = temp.resolveBinding();
				if (tempBinding != null && binding != null
						&& tempBinding.isEqualTo(binding)) {
					return true;
				}
			}

		}

		return false;
	}

	public static MethodDeclaration getMatchee(MethodDeclaration examinee,
			Set<MethodDeclaration> candidates) {

		ASTMatcher astMatcher = AsidePlugin.getDefault().getASTMatcher();
		for (MethodDeclaration candidate : candidates) {

			if (astMatcher.match(examinee, candidate)) {
				return candidate;
			}
		}
		return null;
	}

	public static boolean isMainEntrance(ICompilationUnit cu,
			MethodDeclaration node) {
		// get the node's signature
		int start = node.getStartPosition();
		try {
			IJavaElement element = cu.getElementAt(start);

			if (element.getElementType() == IJavaElement.METHOD) {
				IMethod method = (IMethod) element;
				if (method.isMainMethod())
					return true;
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isControlStatementWithBlock(ASTNode node) {
		switch (node.getNodeType()) {
		case ASTNode.IF_STATEMENT:
		case ASTNode.WHILE_STATEMENT:
		case ASTNode.FOR_STATEMENT:
		case ASTNode.DO_STATEMENT:
			return true;
		default:
			return false;
		}
	}

	public static boolean specialCase(MethodInvocation target) {

		if (target.getParent() == null) {
			return false;
		}

		ASTNode parent = target.getParent();
		if (parent.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}

		MethodInvocation parentMethodInvocation = (MethodInvocation) parent;

		Expression expression = parentMethodInvocation.getExpression();

		if (expression == null) {
			return false;
		}

		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName invoker = (SimpleName) expression;

		SimpleName name = parentMethodInvocation.getName();

		if (name.getIdentifier().equals("getValidInput")
				&& invoker.getIdentifier().equals("ESAPI.validator")) {
			return true;
		}

		return false;
	}

	// TODO
	public static boolean isFalsePositive(Expression argumentNode,
			int argCharStart, int argLength, ICompilationUnit cu) {
		
		int nodeType = argumentNode.getNodeType();
		
		switch(nodeType){
		case ASTNode.STRING_LITERAL:
			return true;
		case ASTNode.NUMBER_LITERAL:
			return true;
		case ASTNode.BOOLEAN_LITERAL:
			return true;
		case ASTNode.TYPE_LITERAL:
			return true;
		case ASTNode.CHARACTER_LITERAL:
			return true;
		default:
			ITypeBinding binding = argumentNode.resolveTypeBinding();
			String qualifiedName = binding.getQualifiedName();
			
			if(qualifiedName != null && qualifiedName.equals("java.lang.String"))
				return false;
			return true;
		}
		
		
	}

}
