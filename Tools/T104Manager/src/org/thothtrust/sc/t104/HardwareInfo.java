package org.thothtrust.sc.t104;

import java.util.ArrayList;

import org.thothtrust.sc.t104.util.BinUtils;

public class HardwareInfo {

	private int version;
	private byte[] cryptoCapabities = null;
	private ArrayList<UserCapability> userCapabilities = new ArrayList<>();
	private ArrayList<SCPCapability> scpCapabilities = new ArrayList<>();
	private ArrayList<BackupCapability> backupCapabilities = new ArrayList<>();
	private byte interactiveCapabilities;
	private byte credentialFormat;
	private int memPersist;
	private int memTempReset;
	private int memTempDeselect;
	private byte[] hwIDPubKeyBytes;

	public HardwareInfo(byte[] data) {
		System.out.println("Parsing HW Info: " + BinUtils.toHexString(data));
		int off = 0;

		// Read version
		version = (int) BinUtils.bytesToShort(data[off], data[off + 1]);
		off += 2;

		// Read TLV start from here
		while (off != data.length) {
			byte a = data[off];
			short len = (short) (data[off + 1] & 0xFF);
			off += 2;

			switch (a) {
			case Constants.TLV_TAG_HW_CAP:
				cryptoCapabities = new byte[len];
				System.arraycopy(data, off, cryptoCapabities, 0, len);
				off += len;
				break;
			case Constants.TLV_TAG_HW_USR:
				if (len == 1) {
					userCapabilities.add(new UserCapability(data[off], null));
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_MEM_PERSIST:
				if (len == 4) {
					memPersist = BinUtils.bytesToInt(data, off);
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_MEM_TEMP_RST:
				if (len == 4) {
					memTempReset = BinUtils.bytesToInt(data, off);
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_MEM_TEMP_DST:
				if (len == 4) {
					memTempDeselect = BinUtils.bytesToInt(data, off);
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_ID:
				break;
			case Constants.TLV_TAG_HW_ID_PUB:
				hwIDPubKeyBytes = new byte[len + 1];
				hwIDPubKeyBytes[0] = (byte) 0x04;
				System.arraycopy(data, off, hwIDPubKeyBytes, 1, len);
				off += len;
				break;
			case Constants.TLV_TAG_HW_ID_ATTEST:
				break;
			case Constants.TLV_TAG_HW_SCP:
				if (len == 2) {
					byte[] b = new byte[len];
					System.arraycopy(data, off, b, 0, len);
					scpCapabilities.add(new SCPCapability(BinUtils.bytesToShort(b[0], b[1])));
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_BK:
				if (len == 1) {
					backupCapabilities.add(new BackupCapability(data[off], null));
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_INTERACT:
				if (len == 1) {
					interactiveCapabilities = data[off];
					off += len;
				}
				break;
			case Constants.TLV_TAG_HW_CRED_FORMAT:
				if (len == 1) {
					credentialFormat = data[off];
					off += len;
				}
				break;
			}
		}
	}

	public int getVersion() {
		return version;
	}

	public byte[] getCryptoCapabities() {
		return cryptoCapabities;
	}

	public ArrayList<UserCapability> getUserCapabilities() {
		return userCapabilities;
	}

	public ArrayList<SCPCapability> getScpCapabilities() {
		return scpCapabilities;
	}

	public ArrayList<BackupCapability> getBackupCapabilities() {
		return backupCapabilities;
	}

	public String getUserCapabilitiesStr() {
		StringBuffer sb = new StringBuffer();
		for (UserCapability cap : userCapabilities) {
			switch (cap.getUserType()) {
			case Constants.USER_ADMIN:
				sb.append("Admin");
				break;
			case Constants.USER_KEYMAN:
				sb.append("KeyMan");
				break;
			case Constants.USER_CRYPTUSR:
				sb.append("CryptUsr");
				break;
			}
			if (cap.getUserInfo() != null) {
				sb.append(": " + BinUtils.toHexString(cap.getUserInfo()) + ";");
			} else {
				sb.append(";");
			}
		}
		return sb.toString();
	}

	public String getScpCapabilitiesStr() {
		StringBuffer sb = new StringBuffer();
		for (SCPCapability cap : scpCapabilities) {
			switch (cap.getScpType()) {
			case Constants.SCP_TT_TP_A03:
				sb.append("ThothTrust_ThetaPass_A03; ");
				break;
			}
		}
		return sb.toString();
	}

	public String getBackupCapabilitiesStr() {
		StringBuffer sb = new StringBuffer();
		for (BackupCapability cap : backupCapabilities) {
			switch (cap.getBackupType()) {
			case Constants.BACKUP_CW_TOKEN:
				sb.append("Backup_CodeWav_Token_Format");
				break;
			case Constants.BACKUP_TTUDS_TOKEN:
				sb.append("Backup_USIGN_Token_Format");
				break;
			}
			if (cap.getBackupInfo() != null) {
				sb.append(": " + BinUtils.toHexString(cap.getBackupInfo()) + "; ");
			} else {
				sb.append("; ");
			}
		}
		return sb.toString();
	}

	public String getInteractiveCapabilitiesStr() {
		StringBuffer sb = null;
		if (getInteractiveCapabilities() == (byte) 0x00) {
			return "None";
		} else {
			sb = new StringBuffer();
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_SCREEN) == Constants.INTERACT_SCREEN) {
				sb.append("Embedded Screen; ");
			}
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_KEYPAD) == Constants.INTERACT_KEYPAD) {
				sb.append("Embedded Keypad; ");
			}
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_FPREAD) == Constants.INTERACT_FPREAD) {
				sb.append("Fingerprint Sensor; ");
			}
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_FACIAL) == Constants.INTERACT_FACIAL) {
				sb.append("Facial Recognition; ");
			}
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_PALM) == Constants.INTERACT_PALM) {
				sb.append("Palm Recognition; ");
			}
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_EYE) == Constants.INTERACT_EYE) {
				sb.append("Iris Recognition; ");
			}
			if ((byte) (getInteractiveCapabilities() & Constants.INTERACT_RTC) == Constants.INTERACT_RTC) {
				sb.append("HW RTC Clock; ");
			}
		}
		return sb.toString();
	}

	public String getCredentialFormatStr() {
		StringBuffer sb = null;
		if (getInteractiveCapabilities() == (byte) 0x00) {
			return "None";
		} else {
			sb = new StringBuffer();
			if (getCredentialFormat() == Constants.CRED_AUTHTYPE_PIN) {
				sb.append("PIN; ");
			}
			if (getCredentialFormat() == Constants.CRED_AUTHTYPE_PWD) {
				sb.append("Password; ");
			}
		}
		return sb.toString();
	}

	public byte getInteractiveCapabilities() {
		return interactiveCapabilities;
	}

	public byte getCredentialFormat() {
		return credentialFormat;
	}

	public int getMemPersist() {
		return memPersist;
	}

	public int getMemTempReset() {
		return memTempReset;
	}

	public int getMemTempDeselect() {
		return memTempDeselect;
	}

	public byte[] getHwIDPubKeyBytes() {
		return hwIDPubKeyBytes;
	}

}