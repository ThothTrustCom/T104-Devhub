package org.thothtrust.sc.t104;

public class BackupCapability {

	private byte backupType;
	private byte[] backupInfo;
	
	public BackupCapability() {}
	
	public BackupCapability(byte backupType, byte[] backupInfo) {
		setBackupType(backupType);
		setBackupInfo(backupInfo);
	}
	
	public byte getBackupType() {
		return backupType;
	}
	public void setBackupType(byte backupType) {
		this.backupType = backupType;
	}
	public byte[] getBackupInfo() {
		return backupInfo;
	}
	public void setBackupInfo(byte[] backupInfo) {
		this.backupInfo = backupInfo;
	}
		
}