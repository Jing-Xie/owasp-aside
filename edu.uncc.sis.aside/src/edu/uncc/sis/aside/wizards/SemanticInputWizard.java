package edu.uncc.sis.aside.wizards;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import edu.uncc.sis.aside.ast.ASTResolving;

public class SemanticInputWizard extends Wizard implements INewWizard {
	private SemanticInputWizardPage page;
	private String ruleKey;
	private String rulePattern;

	private IDocument document;
	private ImportRewrite fImportRewrite;
	private AST ast;
	private ASTNode coveredNode, coveringNode;

	/**
	 * Constructor for SemanticInputWizard. Get more info from marker resolution
	 */
	public SemanticInputWizard(IDocument document,
			ImportRewrite fImportRewrite, AST ast,
			 ASTNode coveredNode,
			ASTNode coveringNode, String ruleKey, String rulePattern) {
		super();
		this.setWindowTitle("Semantic Validation Configuration");
		setNeedsProgressMonitor(false);

		this.document = document;
		this.ast = ast;
		this.fImportRewrite = fImportRewrite;
		this.coveredNode = coveredNode;
		this.coveringNode = coveringNode;
		this.ruleKey = ruleKey;
		this.rulePattern = rulePattern;
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new SemanticInputWizardPage(ruleKey);
		addPage(page);
	}

	public boolean performFinish() {

		String validationType = page.syntacticOrSemantic();

		if (validationType.equals("semantic") && ruleKey.equalsIgnoreCase("URL")) {
			String baseDirectory = page.getBaseDirectory();

		}else if(validationType.equals("semantic") && ruleKey.equalsIgnoreCase("EMAIL")){
			
		}

		if (validationType.equals("syntactic")) {
			/*
			 * Syntactic checking against regular expressions
			 */
			try {
				ASTRewrite fASTRewrite = ASTRewrite.create(ast);
				TextEdit importEdits, textEdits;

				Statement statement = ASTResolving
						.findParentStatement(coveringNode);
				if (statement == null) {
					return true;
				}

				StructuralPropertyDescriptor location = statement
						.getLocationInParent();

				if (location == null || location.isChildProperty()
						|| !location.isChildListProperty()) {
					return true;
				}

				if (statement instanceof ExpressionStatement) {

					ExpressionStatement expressionStatement = (ExpressionStatement) statement;
					Expression expression = expressionStatement.getExpression();
					ASTNode parent = coveringNode.getParent();

					/* String s; ... s = a.b(); ... */
					if (parent == expression && parent instanceof Assignment) {
						Assignment assignment = (Assignment) parent;
						fASTRewrite = refactoringCode(fASTRewrite, ast,
								rulePattern, statement, assignment, location);
						textEdits = fASTRewrite.rewriteAST();
						importEdits = fImportRewrite.rewriteImports(null);
						textEdits.apply(document, TextEdit.CREATE_UNDO
								| TextEdit.UPDATE_REGIONS);
						importEdits.apply(document, TextEdit.CREATE_UNDO
								| TextEdit.UPDATE_REGIONS);
						return true;
					}

					/* ... c.d(a.b(), ...); ... */
					if (parent == expression
							&& parent instanceof MethodInvocation) {
						// Apparently, this is hard

						return true;
					}

					/* Object s; ... s = c.d(a.b(), ...); ... */
					ASTNode grandNode = parent.getParent();
					if (grandNode != null && grandNode == expression
							&& parent instanceof MethodInvocation) {

						return true;
					}
				}

				if (statement instanceof VariableDeclarationStatement) {

					VariableDeclarationFragment fragment = (VariableDeclarationFragment) coveredNode
							.getParent();

					if (fragment == null) {
						return true;
					}

					/* ... String string = a.b(); ... */
					fASTRewrite = refactoringCode(fASTRewrite, ast,
							rulePattern, statement, fragment, location);
					fImportRewrite.addImport("java.util.regex.Pattern");
					textEdits = fASTRewrite.rewriteAST();
					importEdits = fImportRewrite.rewriteImports(null);
					textEdits.apply(document, TextEdit.CREATE_UNDO
							| TextEdit.UPDATE_REGIONS);
					importEdits.apply(document, TextEdit.CREATE_UNDO
							| TextEdit.UPDATE_REGIONS);
					return true;
				}
			} catch (CoreException e) {

			} catch (MalformedTreeException e) {

			} catch (BadLocationException e) {

			}
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	private ASTRewrite refactoringCode(ASTRewrite fASTRewrite, AST ast,
			String pattern, Statement statement, ASTNode node,
			StructuralPropertyDescriptor location) {

		IfStatement ifStatement = ast.newIfStatement();

		MethodInvocation ifExpression = ast.newMethodInvocation();
		ifExpression.setExpression(ast.newSimpleName("Pattern"));
		ifExpression.setName(ast.newSimpleName("matches"));
		List<ASTNode> arguments = ifExpression.arguments();
		StringLiteral patternNode = ast.newStringLiteral();
		patternNode.setLiteralValue(pattern);
		arguments.add(0, patternNode);
		if (node instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
			arguments.add(1, fASTRewrite.createCopyTarget(fragment.getName()));
		} else if (node instanceof Assignment) {
			Assignment assignment = (Assignment) node;
			arguments.add(1, fASTRewrite.createCopyTarget(assignment
					.getLeftHandSide()));
		}

		Block ifBlock = ast.newBlock();
		List<ASTNode> childrenList = (List) statement.getParent()
				.getStructuralProperty(location);
		for (int i = childrenList.indexOf(statement) + 1; i < childrenList
				.size(); i++) {
			ifBlock.statements().add(
					fASTRewrite.createMoveTarget(childrenList.get(i)));
		}

		ifStatement.setExpression(ifExpression);
		ifStatement.setThenStatement(ifBlock);

		LineComment notice = (LineComment) fASTRewrite.createStringPlaceholder(
				"// NOTE: Input Validation code generated by ASIDE",
				ASTNode.LINE_COMMENT);

		ListRewrite fListRewrite = fASTRewrite
				.getListRewrite(ASTResolving
						.getParent(statement, ASTNode.BLOCK),
						Block.STATEMENTS_PROPERTY);

		fListRewrite.insertAfter(notice, statement, null);
		fListRewrite.insertAfter(ifStatement, notice, null);

		return fASTRewrite;
	}
}