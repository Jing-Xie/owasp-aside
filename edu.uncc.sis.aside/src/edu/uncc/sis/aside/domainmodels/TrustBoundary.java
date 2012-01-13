package edu.uncc.sis.aside.domainmodels;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * To model Trust Boundaries defined in XML. It's hard since the entries are
 * heterogeneous. Should I categorize them into subgroups with which all the
 * entries are homogeneous? Yes, using abstract class/interface.
 * 
 * @author Jing Xie
 * 
 */
public abstract class TrustBoundary implements IAdaptable {

	private String methodName;
	private String declarationClass;
	private String methodType;
	private String[] attrTypes;

	public TrustBoundary(String declarationClass, String methodName, String methodType, String[] attrTypes){
		this.declarationClass = declarationClass;
		this.methodName = methodName;
		this.methodType = methodType;
		this.attrTypes = attrTypes;
	}
	
	/*
	 *  Defer the adaption to platform
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getDeclarationClass() {
		return declarationClass;
	}

	public void setDeclarationClass(String declarationClass) {
		this.declarationClass = declarationClass;
	}

	public String getMethodType() {
		return methodType;
	}

	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	public String[] getAttrTypes() {
		return attrTypes;
	}

	public void setAttrTypes(String[] attrTypes) {
		this.attrTypes = attrTypes;
	}
	
	public String toString(){
		return methodName;
	}
}
