package org.thothtrust.sc.t104;

public class UserCapability {

	private byte userType;
	private byte[] userInfo;
	
	public UserCapability() {		
	}
	
	public UserCapability(byte userType, byte[] userInfo) {
		setUserType(userType);
		setUserInfo(userInfo);
	}
	
	public byte getUserType() {
		return userType;
	}
	public void setUserType(byte userType) {
		this.userType = userType;
	}
	public byte[] getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(byte[] userInfo) {
		this.userInfo = userInfo;
	}
	
}