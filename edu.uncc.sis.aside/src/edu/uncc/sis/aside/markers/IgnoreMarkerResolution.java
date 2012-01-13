package edu.uncc.sis.aside.markers;

import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

import edu.uncc.sis.aside.AsidePlugin;

public class IgnoreMarkerResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager()
	.getLogger(IgnoreMarkerResolution.class.getName());
	
	private static final String ANNOTATION_TYPE = "edu.uncc.sis.aside.invisibleAnnotation";
	
	private ICompilationUnit fCompilationUnit;
	
	
	public IgnoreMarkerResolution(ICompilationUnit fCompilationUnit){
		super();
		this.fCompilationUnit = fCompilationUnit;
	}
	
	@Override
	public String getDescription() {
		String description = "This is not your concern, and you would like ASIDE to treat it as trusted.";
		return description;
	}

	@Override
	public Image getImage() {
		
		return AsidePlugin.getImageDescriptor("icons/broom.png")
		.createImage();
	}

	@Override
	public String getLabel() {
		String label = "Ignore this";
		return label;
	}

	@Override
	public void run(IMarker marker) {
		
		logger.info("User chooses to ignore this warning at line " + marker.getAttribute(IMarker.LINE_NUMBER, -1) + " in "
				+ fCompilationUnit.getElementName());
		
		int offset = marker.getAttribute(IMarker.CHAR_START, -1);
		int length = marker.getAttribute(IMarker.CHAR_END, -1) -offset;
		
		// Add an annotation to the code
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = manager.getTextFileBuffer(fCompilationUnit.getPath(),
				LocationKind.IFILE);
		if (buffer == null) {
			return;
		}

		IAnnotationModel model = buffer.getAnnotationModel();   
		
		Annotation annotation = createTargetAnnotation();	
		model.addAnnotation(annotation, new Position(offset, length));
		
		deletMarker(marker);
	}
	
	// TODO: find a better way to delete marker
	private void deletMarker(IMarker marker) {
		
		try {
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private Annotation createTargetAnnotation() {	
		return new Annotation(ANNOTATION_TYPE, false, null);
	}

}
