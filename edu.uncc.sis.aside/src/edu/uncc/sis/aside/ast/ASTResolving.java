package edu.uncc.sis.aside.ast;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TryStatement;

/**
 * Utility class to deal with ASTNodes and their relationships
 */
public class ASTResolving {


	public static IBinding getParentMethodOrTypeBinding(ASTNode node) {
		do {
			if (node instanceof MethodDeclaration) {
				return ((MethodDeclaration) node).resolveBinding();
			} else if (node instanceof AbstractTypeDeclaration) {
				return ((AbstractTypeDeclaration) node).resolveBinding();
			} else if (node instanceof AnonymousClassDeclaration) {
				return ((AnonymousClassDeclaration) node).resolveBinding();
			}
			node= node.getParent();
		} while (node != null);

		return null;
	}

	public static BodyDeclaration findParentBodyDeclaration(ASTNode node) {
		while ((node != null) && (!(node instanceof BodyDeclaration))) {
			node= node.getParent();
		}
		return (BodyDeclaration) node;
	}

	public static BodyDeclaration findParentBodyDeclaration(ASTNode node, boolean treatModifiersOutside) {
		StructuralPropertyDescriptor lastLocation= null;

		while (node != null) {
			if (node instanceof BodyDeclaration) {
				BodyDeclaration decl= (BodyDeclaration) node;
				if (!treatModifiersOutside || lastLocation != decl.getModifiersProperty()) {
					return decl;
				}
				treatModifiersOutside= false;
			}
			lastLocation= node.getLocationInParent();
			node= node.getParent();
		}
		return (BodyDeclaration) node;
	}


	public static CompilationUnit findParentCompilationUnit(ASTNode node) {
		return (CompilationUnit) findAncestor(node, ASTNode.COMPILATION_UNIT);
	}

	/**
	 * Finds the parent type of a node.
	 *
	 * @param node the node inside the type to find
	 * @param treatModifiersOutside if set, modifiers are not part of their type, but of the type's parent
	 * @return returns either a AbstractTypeDeclaration or an AnonymousTypeDeclaration
	 */
	public static ASTNode findParentType(ASTNode node, boolean treatModifiersOutside) {
		StructuralPropertyDescriptor lastLocation= null;

		while (node != null) {
			if (node instanceof AbstractTypeDeclaration) {
				AbstractTypeDeclaration decl= (AbstractTypeDeclaration) node;
				if (!treatModifiersOutside || lastLocation != decl.getModifiersProperty()) {
					return decl;
				}
			} else if (node instanceof AnonymousClassDeclaration) {
				return node;
			}
			lastLocation= node.getLocationInParent();
			node= node.getParent();
		}
		return null;
	}

	public static ASTNode findParentType(ASTNode node) {
		return findParentType(node, false);
	}

	/**
	 * Returns the method binding of the node's parent method declaration or <code>null</code> if
	 * the node is not inside a method.
	 * 
	 * @param node the ast node
	 * @return the method binding of the node's parent method declaration or <code>null</code> if
	 *         the node
	 */
	public static MethodDeclaration findParentMethodDeclaration(ASTNode node) {
		while (node != null) {
			if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
				return (MethodDeclaration) node;
			}
			if (node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration) {
				return null;
			}
			node= node.getParent();
		}
		return null;
	}

	public static ASTNode findAncestor(ASTNode node, int nodeType) {
		while ((node != null) && (node.getNodeType() != nodeType)) {
			node= node.getParent();
		}
		return node;
	}

	public static Statement findParentStatement(ASTNode node) {
		while ((node != null) && (!(node instanceof Statement))) {
			node= node.getParent();
			if (node instanceof BodyDeclaration) {
				return null;
			}
		}
		return (Statement) node;
	}

	public static TryStatement findParentTryStatement(ASTNode node) {
		while ((node != null) && (!(node instanceof TryStatement))) {
			node= node.getParent();
			if (node instanceof BodyDeclaration) {
				return null;
			}
		}
		return (TryStatement) node;
	}

	public static boolean isInsideConstructorInvocation(MethodDeclaration methodDeclaration, ASTNode node) {
		if (methodDeclaration.isConstructor()) {
			Statement statement= ASTResolving.findParentStatement(node);
			if (statement instanceof ConstructorInvocation || statement instanceof SuperConstructorInvocation) {
				return true; // argument in a this or super call
			}
		}
		return false;
	}
	
	public static ASTNode findParent(ASTNode node, StructuralPropertyDescriptor[][] pathes) {
		for (int p= 0; p < pathes.length; p++) {
			StructuralPropertyDescriptor[] path= pathes[p];
			ASTNode current= node;
			int d= path.length - 1;
			for (; d >= 0 && current != null; d--) {
				StructuralPropertyDescriptor descriptor= path[d];
				if (!descriptor.equals(current.getLocationInParent()))
					break;
				current= current.getParent();
			}
			if (d < 0)
				return current;
		}
		return null;
	}
	
	public static boolean isParent(ASTNode node, ASTNode parent) {
		Assert.isNotNull(parent);
		do {
			node= node.getParent();
			if (node == parent)
				return true;
		} while (node != null);
		return false;
	}
	
	/**
	 * Returns the receiver's type binding of the given method invocation.
	 *
	 * @param invocation method invocation to resolve type of
	 * @return the type binding of the receiver
	 */
	public static ITypeBinding getReceiverTypeBinding(MethodInvocation invocation) {
		ITypeBinding result= null;
		Expression exp= invocation.getExpression();
		if(exp != null) {
			return exp.resolveTypeBinding();
		}
		else {
			AbstractTypeDeclaration type= (AbstractTypeDeclaration)getParent(invocation, AbstractTypeDeclaration.class);
			if (type != null)
				return type.resolveBinding();
		}
		return result;
	}
	
	public static ASTNode getParent(ASTNode node, Class<AbstractTypeDeclaration> parentClass) {
		do {
			node= node.getParent();
		} while (node != null && !parentClass.isInstance(node));
		return node;
	}

	public static ASTNode getParent(ASTNode node, int nodeType) {
		do {
			node= node.getParent();
		} while (node != null && node.getNodeType() != nodeType);
		return node;
	}
	
	public static ITypeBinding getEnclosingType(ASTNode node) {
		while(node != null) {
			if (node instanceof AbstractTypeDeclaration) {
				return ((AbstractTypeDeclaration)node).resolveBinding();
			} else if (node instanceof AnonymousClassDeclaration) {
				return ((AnonymousClassDeclaration)node).resolveBinding();
			}
			node= node.getParent();
		}
		return null;
	}
	
	public static CompilationUnit createQuickFixAST(ICompilationUnit compilationUnit, IProgressMonitor monitor) {
		ASTParser astParser= ASTParser.newParser(AST.JLS3);
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
		astParser.setStatementsRecovery(true);
		astParser.setBindingsRecovery(true);
		return (CompilationUnit) astParser.createAST(monitor);
	}

}
