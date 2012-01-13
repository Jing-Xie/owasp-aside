package edu.uncc.sis.aside.domainmodels;

public class NonReturnedTrustBoundary extends TrustBoundary {

	private int[] argumentIndice;
	
	public NonReturnedTrustBoundary(String declarationClass, String methodName, String methodType, String[] attrTypes, int[] argumentIndice){
		super(declarationClass, methodName, methodType, attrTypes);
		this.argumentIndice = argumentIndice;
	}
	
	public int[] getArgumentIndice() {
		return argumentIndice;
	}

	public void setArgumentIndice(int[] argumentIndice) {
		this.argumentIndice = argumentIndice;
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
