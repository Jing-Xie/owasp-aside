package edu.uncc.sis.aside.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import edu.uncc.sis.aside.ast.ASTBuilder;
import edu.uncc.sis.aside.visitors.MethodDeclarationVisitor;

public class AsideBuilder extends IncrementalProjectBuilder {

//	private static final String ASIDE_BUILDER_ID = "edu.uncc.sis.aside.AsideBuilder";
//	private static final String ASIDE_NATURE_ID = "edu.uncc.sis.aside.AsideNature";
	private static final String ASIDE_MARKER_TYPE = "edu.uncc.sis.aside.AsideMarker";

	// vulnerability information for an Aside nature enabled IProject
	private static Map<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> checkpoints;

	/*
	 * 0-argument constructor as required by the extension point, this is a
	 * must.
	 */
	public AsideBuilder() {
		super();
		if (checkpoints == null)
			checkpoints = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();
	}

	class DeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				findAndMarkVulnerabilities(resource);
				break;
			case IResourceDelta.REMOVED:
				// platform takes care of removing markers
				disposeDeletedMappings(resource);
				break;
			case IResourceDelta.CHANGED:
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					int flags = delta.getFlags();
					if ((flags & IResourceDelta.CONTENT) != 0
							&& (flags & IResourceDelta.LOCAL_CHANGED) == 0
							&& file.getName().endsWith(".java")) {
                         ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
                         if(checkpoints != null){
                        	 Map<MethodDeclaration, ArrayList<IMarker>> markersMap = checkpoints.get(cu);
                        	 
                         }
					}

				}

				break;
			}
			// return true to continue visiting children.
			return true;
		}

		private void disposeDeletedMappings(IResource resource) {
			if (checkpoints != null && !checkpoints.isEmpty()) {
				if (resource instanceof IFolder) {
					return;
				} else if (resource instanceof IFile
						&& resource.getName().endsWith(".java")) {
					IFile file = (IFile) resource;
					ICompilationUnit cu = JavaCore
							.createCompilationUnitFrom(file);
					Set<Entry<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>> set = checkpoints
							.entrySet();
					Iterator<Entry<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>> iterator = set
							.iterator();
					while (iterator.hasNext()) {
						Entry<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>> entry = iterator
								.next();
						ICompilationUnit key = entry.getKey();
						if (key.equals(cu)) {
							iterator.remove();
						}
					}
				}
			}
		}

	}

	class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			try {
				resource.getProject().deleteMarkers(ASIDE_MARKER_TYPE, true,
						IFile.DEPTH_INFINITE);
				if (checkpoints == null) {
					checkpoints = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();
				} else if (checkpoints != null && !checkpoints.isEmpty()) {
					checkpoints.clear();
				}

				findAndMarkVulnerabilities(resource);

			} catch (CoreException e) {

			}
			// return true to continue visiting children
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		IProject targetProject = getProject();
		
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else if(kind == AUTO_BUILD || kind == INCREMENTAL_BUILD){
			IResourceDelta delta = getDelta(targetProject);
			if (delta == null) {
				
			} else {
				System.out.println(kind);
				incrementalBuild(delta, monitor);
			}
		}else if(kind == CLEAN_BUILD){
			
		}
		
		IProject[] projects = new IProject[1];
		projects[0] = targetProject; 
		
		return projects;
	}

	private void findAndMarkVulnerabilities(IResource resource)
			throws JavaModelException {

		if (resource instanceof IProject) {
			return;
		} else if (resource instanceof IFolder) {
			return;
		} else if (resource instanceof IFile
				&& resource.getName().endsWith(".java")) {
			IFile file = (IFile) resource;
            
			ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
			
			if (cu.isStructureKnown()) {
				ICompilationUnit workingCopy = cu
						.getWorkingCopy(new NullProgressMonitor());
				CompilationUnit astRoot = ASTBuilder.getASTBuilder().parse(
						workingCopy);
				MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor(
						astRoot, null, cu, null);
				Map<MethodDeclaration, ArrayList<IMarker>> markersMap = methodDeclarationVisitor
						.process();
				checkpoints.put(cu, markersMap);
			}

		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
//		try {
//			getProject().accept(new ResourceVisitor());
//		} catch (CoreException e) {
//		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
//		delta.accept(new DeltaVisitor());
	}

	public Map<MethodDeclaration, ArrayList<IMarker>> getCheckpoints(
			ICompilationUnit cu) {
		if (checkpoints != null) {
			Map<MethodDeclaration, ArrayList<IMarker>> markers = checkpoints
					.get(cu);
			if (markers != null) {
				return markers;
			}
		}
		return null;
	}

	public void setCheckpoints(ICompilationUnit cu,
			Map.Entry<MethodDeclaration, ArrayList<IMarker>> markerEntries) {
		if (checkpoints == null) {
			checkpoints = new HashMap<ICompilationUnit, Map<MethodDeclaration, ArrayList<IMarker>>>();
		}
		Map<MethodDeclaration, ArrayList<IMarker>> markers = checkpoints
				.get(cu);
		if (markers == null) {
			markers = new HashMap<MethodDeclaration, ArrayList<IMarker>>();
		}
		ArrayList<IMarker> asideMarkers = markers.get(markerEntries.getKey());
		if (asideMarkers == null || asideMarkers.isEmpty()) {
			asideMarkers = markerEntries.getValue();
		} else {
			asideMarkers = mergeMarkerLists(asideMarkers, markerEntries
					.getValue());
		}
	}

	private ArrayList<IMarker> mergeMarkerLists(ArrayList<IMarker> seniors,
			ArrayList<IMarker> newcomers) {

		// TODO
		for (IMarker newcomer : newcomers) {
			for (IMarker senior : seniors) {
				if (newcomer != null && senior != null
						&& newcomer.equals(senior)) {

				}
			}

		}

		return seniors;
	}

}
