package org.thothtrust.sc.t104;

public class AOCContainerObject {

	private byte[] credName;
	private byte[] auxData;
	
	public AOCContainerObject() {		
	}
	
	public AOCContainerObject(byte[] credName, byte[] auxData) {
		setCredName(credName);
		setAuxData(auxData);
	}
	
	public byte[] getCredName() {
		return credName;
	}
	public void setCredName(byte[] credName) {
		this.credName = credName;
	}
	public byte[] getAuxData() {
		return auxData;
	}
	public void setAuxData(byte[] auxData) {
		this.auxData = auxData;
	}

}