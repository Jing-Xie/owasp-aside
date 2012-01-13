package edu.uncc.sis.aside.ast;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public final class ASTBuilder {

	// ASTBuilder is a singleton builds up AST based on the type of input
	private static ASTBuilder astBuilder;

	private ASTBuilder() {

	}

	public static ASTBuilder getASTBuilder() {
		if (astBuilder == null) {
			astBuilder = new ASTBuilder();
		}
		return astBuilder;
	}

	/**
	 * Get AST node for source file
	 * 
	 * @param unit
	 *            Lightweight handle from Java Model which represents a Java
	 *            source file
	 * @return CompilatinUnit An AST node which represents the Java class file
	 */
	public CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);

		return (CompilationUnit) parser.createAST(null);
	}

}
