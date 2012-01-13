package edu.uncc.sis.aside.markers;


import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PartInitException;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.auxiliary.core.CodeGenerator;

public class SyntacticValidationResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager()
			.getLogger(SyntacticValidationResolution.class.getName());

	
	private ICompilationUnit fCompilationUnit;
	private String fInputType;
	private IMarker fMarker;
	
	
	/**
	 * Constructor for SyntacticValidationResolution
	 * 
	 * @param cu
	 * @param validationRule
	 */
	public SyntacticValidationResolution(ICompilationUnit cu, IMarker marker,
			String inputType) {
		super();
		fCompilationUnit = cu;
		fInputType = inputType;
		fMarker = marker;
	}

	@Override
	public String getDescription() {

		StringBuffer buf = new StringBuffer();
		buf.append("<p>");

		String description = fInputType.toString();

		if (description == null) {
			description = "Description about this rule is not available";
		}

		buf.append(description);
		buf.append("<p>");
		return buf.toString();
	}

	@Override
	public Image getImage() {

		if (fInputType.equalsIgnoreCase("URL") || fInputType.equalsIgnoreCase("email")) {
			return AsidePlugin.getImageDescriptor("icons/globecompass.png")
					.createImage();
		}

		return AsidePlugin.getImageDescriptor("icons/pyramid.png")
				.createImage();
	}

	@Override
	public String getLabel() {
		return fInputType;
	}

	@Override
	public void run(IMarker marker) {
		logger.info("User clicks on SYNTACTIC VALIDATION Rule << " + fInputType + ">> in an attempt to get input validated at "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in "
				+ fCompilationUnit.getElementName());

		/*
		 * depends on the type (semantic|syntactic) of rule, marker resolution
		 * should respond with different annotations. But now, we consider only
		 * URL and email need advanced checking
		 */
		try {
			CompilationUnit astRoot = ASTResolving.createQuickFixAST(
					fCompilationUnit, null);
			ImportRewrite fImportRewrite = ImportRewrite.create(astRoot, true);

			int offset = (int) fMarker.getAttribute(IMarker.CHAR_START, -1);
			int length = (int) fMarker.getAttribute(IMarker.CHAR_END, -1)
					- offset;

			ASTNode node = NodeFinder.perform(astRoot, offset, length);
			if (node == null) {
				return;
			}

			MethodDeclaration declaration = ASTResolving
					.findParentMethodDeclaration(node);

			if (declaration == null) {
				return;
			}

			Block body = declaration.getBody();

			AST ast = body.getAST();

			IEditorPart part = JavaUI
					.openInEditor(fCompilationUnit, true, true);

			if (part == null) {
				return;
			}

			IDocument document = JavaUI.getDocumentProvider().getDocument(
					part.getEditorInput());

			CodeGenerator.getInstance().generateValidationRoutine(document, astRoot,
					fImportRewrite, ast, node, fInputType);
			
		} catch (JavaModelException e) {
		} catch (PartInitException e) {
		} catch (IllegalArgumentException e) {
		} catch (MalformedTreeException e) {
		}

	}

}
