package edu.uncc.sis.aside.markers;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import edu.uncc.sis.aside.AsidePlugin;
import edu.uncc.sis.aside.utils.Converter;

public class AsideMarkerOutputResolutionGenerator implements
		IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			AsideMarkerOutputResolutionGenerator.class.getName());

	// It is a batch model, one click will lead to multi-fixes

	private static final String ASIDE_MARKER_TYPE = "edu.uncc.sis.aside.AsideMarker";
	private static final IMarkerResolution[] NO_RESOLUTION = new IMarkerResolution[0];

	// currently there are 13 encoding strategies documented in ESAPI, adding
	// ignore solution
	private static final int SIZE = 14;

	private String[] types = new String[] { "Base64", "CSS", "DN", "HTML",
			"HTMLAttribute", "JavaScript", "LDAP", "OS", "SQL", "VBScript",
			"XML", "XMLAttribute", "XPath" };

	/*
	 * public 0-argument constructor as required by the extension point
	 */
	public AsideMarkerOutputResolutionGenerator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.
	 * core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return internalGetResolutions(marker);
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return internalHasResolution(marker);
	}

	private boolean internalHasResolution(IMarker marker) {

		try {
			String markerType = marker.getType();

			if (markerType == null || !markerType.equals(ASIDE_MARKER_TYPE)) {
				return false;
			}

			String flow = (String) marker
					.getAttribute("edu.uncc.sis.aside.marker.flow");

			if (flow == null) {
				return false;
			}

			String[] result = Converter.stringToStringArray(flow);
			for (int i = 0; i < result.length; i++) {
				if (result[i].equals("output")) {
					return true;
				}
			}

			return false;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

	}

	private IMarkerResolution[] internalGetResolutions(IMarker marker) {

		ICompilationUnit unit = getCompilationUnit(marker);

		logger.info("User hovers over the marker or clicks on the marker icon at Line "
				+ marker.getAttribute(IMarker.LINE_NUMBER, -1)
				+ " in File <<"
				+ unit.getElementName() + ">> in Project ==TBD==");

		if (!internalHasResolution(marker)) {
			return NO_RESOLUTION;
		}

		LinkedList<IMarkerResolution> resolutions = new LinkedList<IMarkerResolution>();

		String type = null;
		for (int i = 0; i < SIZE - 1; i++) {
			type = types[i];
			IMarkerResolution encodingResolution = new EncodingResolution(unit,
					type);
			resolutions.add(encodingResolution);
		}

		IMarkerResolution ignoreResolution = new IgnoreMarkerResolution(unit);
		resolutions.add(ignoreResolution);

	
		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);

	}

	private static ICompilationUnit getCompilationUnit(IMarker marker) {
		IResource res = marker.getResource();
		if (res instanceof IFile && res.isAccessible()) {
			IJavaElement element = JavaCore.create((IFile) res);
			if (element instanceof ICompilationUnit)
				return (ICompilationUnit) element;
		}
		return null;
	}

}
