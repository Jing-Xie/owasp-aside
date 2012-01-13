package edu.uncc.sis.aside.markers;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
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

public class EncodingResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			EncodingResolution.class.getName());

	private ICompilationUnit fCompilationUnit;
	private CompilationUnit astRoot;
	private String fStrategyType;
	private String esapiEncoderMethodName;

	public EncodingResolution(ICompilationUnit cu, String strategyType) {
		super();
		fCompilationUnit = cu;
		fStrategyType = strategyType;
		esapiEncoderMethodName = "encodeFor" + fStrategyType;
	}

	@Override
	public String getDescription() {
		return fStrategyType + " encoding is the practice of translating unprintable characters or characters with special meaning within URLs to a representation that is unambiguous and universally accepted by web browsers and servers.";
	}

	@Override
	public Image getImage() {
		return AsidePlugin.getImageDescriptor("icons/saturn.png").createImage();
	}

	@Override
	public String getLabel() {
		return "Apply " + fStrategyType + " Encoding Strategy to Selected Input";
	}

	@Override
	public void run(IMarker marker) {

		logger.info("User clicks on URL OUTPUT ENCODING Rule in an attempt to get output URL encoded at "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in "
				+ fCompilationUnit.getElementName());
		try {
			astRoot = ASTResolving.createQuickFixAST(fCompilationUnit, null);
			ImportRewrite fImportRewrite = ImportRewrite.create(astRoot, true);

			int offset = (int) marker.getAttribute(IMarker.CHAR_START, -1);
			int length = (int) marker.getAttribute(IMarker.CHAR_END, -1)
					- offset;

			ASTNode node = NodeFinder.perform(astRoot, offset, length);

			if (!(node instanceof Expression)) {
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
			
			CodeGenerator.getInstance().generateEncodingRoutine(document, fImportRewrite, ast, declaration, (Expression)node, esapiEncoderMethodName);

		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

}
