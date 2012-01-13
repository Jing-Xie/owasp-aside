package edu.uncc.sis.aside.decorators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import edu.uncc.sis.aside.AsidePlugin;

public class AsideLightweightDecorator extends LabelProvider implements
		ILightweightLabelDecorator {

	/*
	 * The ILightweightLabelDecorator is a decorator that decorates using a
	 * prefix, suffix and overlay image rather than doing all of the image and
	 * text management itself like an ILabelDecorator.
	 */

	private static final String ASIDE_MARKER_TYPE = "edu.uncc.sis.aside.AsideMarker";

	/**
	 * public 0-argument constructor required by the extension point
	 */
	public AsideLightweightDecorator() {
		super();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang
	 *      .Object, org.eclipse.jface.viewers.IDecoration) Calculates
	 *      decorations based on element status and type or other relative
	 *      properties if there's any
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {

		IResource resource = getResource(element);

		if (resource == null)
			return;

		if (!resource.exists())
			return;

		if (!resource.isAccessible())
			return;

		try {

			if (resource.getType() == IResource.PROJECT) {

				IProject project = (IProject) resource;
				IJavaElement javaElement = JavaCore.create(project);
				if (javaElement == null)
					return;
				if (javaElement.getElementType() != IJavaElement.JAVA_PROJECT)
					return;

				IMarker[] markers = project.findMarkers(ASIDE_MARKER_TYPE,
						false, IResource.DEPTH_INFINITE);
				if (markers.length != 0) {
					decoration.addOverlay(
							AsidePlugin.getImageDescriptor("icons/reddot.jpg"),
							IDecoration.TOP_LEFT);
					decoration.addSuffix(" [ASIDED]");
				}

			} else if (resource.getType() == IResource.FOLDER) {

				IFolder folder = (IFolder) resource;
				IJavaElement javaElement = JavaCore.create(folder);
				if (javaElement == null)
					return;
				if (javaElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT
						&& javaElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT)
					return;

				IMarker[] markers = folder.findMarkers(ASIDE_MARKER_TYPE,
						false, IResource.DEPTH_INFINITE);
				if (markers.length != 0) {
					decoration.addSuffix(" [ASIDED]");
					decoration.addOverlay(
							AsidePlugin.getImageDescriptor("icons/reddot.jpg"),
							IDecoration.TOP_LEFT);
				}

			} else

			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;

				IJavaElement javaElement = JavaCore.create(file);
				if (javaElement == null)
					return;
				if (javaElement.getElementType() != IJavaElement.COMPILATION_UNIT)
					return;

				IMarker[] markers = file.findMarkers(ASIDE_MARKER_TYPE, false,
						IResource.DEPTH_INFINITE);
				// markers should not be null according to spec
				if (markers.length != 0) {

					decoration.addSuffix(" [ASIDED]");
					decoration.addOverlay(
							AsidePlugin.getImageDescriptor("icons/reddot.jpg"),
							IDecoration.TOP_LEFT);
				}
			} else {
				return;
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource) object;
		}

		if (object instanceof IAdaptable) {
			return (IResource) (((IAdaptable) object)
					.getAdapter(IResource.class));
		}

		return null;
	}

}
