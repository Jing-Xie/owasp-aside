package edu.uncc.sis.aside.domainmodels;

public class ReturnedTrustBoundary extends TrustBoundary {

	private String returnClass;

	public ReturnedTrustBoundary(String declarationClass, String methodName, String methodType, String[] attrTypes, String returnClass){
		super(declarationClass, methodName, methodType, attrTypes);
		this.returnClass = returnClass;
	}
	
	public String getReturnClass() {
		return returnClass;
	}

	public void setReturnClass(String returnClass) {
		this.returnClass = returnClass;
	}

	@Override
	public boolean equals(Object obj) {
		
		return super.equals(obj);
	}

	@Override
	public String toString() {
		
		return super.toString();
	}
	
}
