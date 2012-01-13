package edu.uncc.sis.aside.markers;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

import edu.uncc.sis.aside.AsidePlugin;

public class SemanticValidationResolution implements IMarkerResolution, IMarkerResolution2 {

	private static final Logger logger = AsidePlugin.getLogManager()
	.getLogger(SemanticValidationResolution.class.getName());
	// This is only for those who return integers
	
	private ICompilationUnit fCompilationUnit;
	
	public SemanticValidationResolution(ICompilationUnit cu){
		fCompilationUnit = cu;
	}
	
	

	@Override
	public String getDescription() {
		
		return null;
	}

	@Override
	public Image getImage() {
		
		return null;
	}

	@Override
	public String getLabel() {
		
		return null;
	}

	@Override
	public void run(IMarker marker) {
		logger.info("User clicks on Semantic Validation resolution in an attempt to get input validated at "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in "
				+ fCompilationUnit.getElementName());
		
	}


}
