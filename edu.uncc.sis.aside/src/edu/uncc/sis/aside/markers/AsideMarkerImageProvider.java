package edu.uncc.sis.aside.markers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import edu.uncc.sis.aside.AsidePlugin;

@SuppressWarnings("restriction")
public class AsideMarkerImageProvider implements IAnnotationImageProvider {

	
	/*
	 * 0-argument constructor as required by extension point
	 */
	public AsideMarkerImageProvider() {

	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		
		return AsidePlugin.getImageDescriptor("icons/devil.png");
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		
		
		
		return null;
	}

	@Override
	public Image getManagedImage(Annotation annotation) {
		
		return null;
	}

}
