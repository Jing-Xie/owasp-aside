package edu.uncc.sis.aside.auxiliary.popup.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.uncc.sis.aside.ast.ASTResolving;
import edu.uncc.sis.aside.auxiliary.core.CodeGenerator;

public class DynamicCommanderHandler extends AbstractHandler {

	private IEditorPart editorPart = null;

	private String selectedInputType = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		editorPart = HandlerUtil.getActiveEditor(event);

		Object trigger = event.getTrigger();

		if (trigger == null) {
			
			selectedInputType = "";
			
		} else if (trigger instanceof Event) {
			
			Event currentEvent = (Event) trigger;
			Widget receiver = currentEvent.widget;

			if (receiver instanceof MenuItem) {
				MenuItem item = (MenuItem) receiver;
				String text = item.getText();

				if (text.lastIndexOf(":") != -1) {
					selectedInputType = text.substring(text.lastIndexOf(":") + 1).trim();
				} else {
					selectedInputType = text.trim();
				}

			}

		}else {
			selectedInputType = "";
		}
		System.out.println(selectedInputType);
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection == null)
			return selection;

		if (selection instanceof ITextSelection) {
			ITextSelection tSelection = (ITextSelection) selection;

			processSelection(tSelection, selectedInputType);
		}

		return selection;
	}

	private void processSelection(ITextSelection tSelection,
			String selectedInputType) {

		IEditorInput editorInput = editorPart.getEditorInput();
		IJavaElement javaElement = JavaUI
				.getEditorInputJavaElement(editorInput);

		int type = javaElement.getElementType();

		if (type != IJavaElement.COMPILATION_UNIT) {
			// for debugging
			System.out
					.println("editor input does not correspond to the ICompilationUnit");
		}

		ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
		CompilationUnit astRoot = parse(compilationUnit);

		// only when the selected text is a string type should it be further
		// processed
		verifyTextSelection(tSelection, astRoot, selectedInputType);
	}

	private void verifyTextSelection(ITextSelection tSelection,
			CompilationUnit root, String key) {
		int start = tSelection.getOffset();
		int length = tSelection.getLength();

		ASTNode node = NodeFinder.perform(root, start, length);
		if (node == null) {
			return;
		}

		int type = node.getNodeType();
		if (type != ASTNode.SIMPLE_NAME) {
			return;
		}

		ImportRewrite fImportRewrite = ImportRewrite.create(root, true);

		MethodDeclaration declaration = ASTResolving
				.findParentMethodDeclaration(node);

		if (declaration == null) {
			return;
		}

		Block body = declaration.getBody();

		AST ast = body.getAST();

		IDocument document = JavaUI.getDocumentProvider().getDocument(
				editorPart.getEditorInput());
		
		//TODO check for the validity of the selection

		CodeGenerator.getInstance().generateValidationRoutine(document, root,
				fImportRewrite, ast, node, key);
	}

	private CompilationUnit parse(ICompilationUnit cu) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(cu);
		return (CompilationUnit) parser.createAST(null);
	}

}
