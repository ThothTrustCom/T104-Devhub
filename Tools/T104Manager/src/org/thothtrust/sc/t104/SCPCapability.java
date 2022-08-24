package org.thothtrust.sc.t104;

public class SCPCapability {
	
	private short scpType;
	
	public SCPCapability() {		
	}
	
	public SCPCapability(short scpType) {
		setScpType(scpType);
	}

	public short getScpType() {
		return scpType;
	}

	public void setScpType(short scpType) {
		this.scpType = scpType;
	}
		
}