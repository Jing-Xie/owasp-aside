package edu.uncc.sis.aside.visitors;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

public class ASTNodeAnnotationVisitor extends ASTVisitor {

	private static final String ANNOTATION_TYPE = "edu.uncc.sis.aside.invisibleAnnotation";
	
	private ArrayList<ASTNode> annotatedNodes;
	
	private ASTNode target;
	private ICompilationUnit fCompilationUnit;
	
	public ASTNodeAnnotationVisitor(ASTNode target, ICompilationUnit cu){
		this.target = target;
		this.fCompilationUnit = cu;
		annotatedNodes = new ArrayList<ASTNode>();
	}


	@Override
	public boolean preVisit2(ASTNode node) {
		
		Annotation annotation = getAttachedAnnoation(node);
		
		if(annotation != null){
			annotatedNodes.add(node);
		}
		
		return true;
	}
	
	@Override
	public void postVisit(ASTNode node) {
		
		super.postVisit(node);
	}
	
	public void process(){
		target.accept(this);
	}
	
    public ArrayList<ASTNode> getAnnotatedNodes(){
    	return annotatedNodes;
    }	
	
	private Annotation getAttachedAnnoation(ASTNode target) {

		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(fCompilationUnit.getPath(), LocationKind.IFILE);
		if (buffer == null) {
			return null;
		}
		
		IAnnotationModel model = buffer.getAnnotationModel();

		Iterator<Annotation> iterator = model.getAnnotationIterator();
		Annotation annotation;
		Position position;
		while (iterator.hasNext()) {
			annotation = iterator.next();
			if(annotation.getType().equals(ANNOTATION_TYPE)){
				position = model.getPosition(annotation);
				if (position.getOffset() == target.getStartPosition()
						&& position.getLength() == target.getLength()) {
					return annotation;
				}
			}
		}

		return null;
	}
}
