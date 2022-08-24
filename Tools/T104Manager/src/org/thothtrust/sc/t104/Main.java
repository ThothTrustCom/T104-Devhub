package org.thothtrust.sc.t104;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.smartcardio.CardException;
import javax.swing.JOptionPane;

import org.thothtrust.sc.t104.api.ManagementTokenAPI;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.ui.MainFrame;
import org.thothtrust.sc.t104.util.BinUtils;
import org.thothtrust.sc.t104.util.StringUtils;

public class Main {

	public static MainFrame gui = null;
	private static volatile HardwareInfo hwInfo = null;
	public static final String[] tokenOptions = { "Rediscover Token", "Quit Token Manager" };

	public static void main(String[] args) {
		try {
			int retryCtr = 0;
			int maxRetry = 5;
			while (retryCtr != maxRetry) {
				try {
					hwInfo = ManagementTokenAPI.getHardwareInfo();
					break;
				} catch (CardException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| ShortBufferException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException | InvalidParameterSpecException | InvalidKeySpecException
						| InvalidSizeException ce) {
					ce.printStackTrace();
					System.out.println("Retrying to connect to hardware [" + retryCtr + "/" + maxRetry + "] times ...");
					retryCtr++;
				}
			}
			while (hwInfo == null) {
				int i = JOptionPane.showOptionDialog(null,
						"<HTML><BODY><H2>No suitable tokens found !</H2>Please insert token to proceed.</BODY></HTML>",
						"No Tokens Found", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, tokenOptions,
						tokenOptions[0]);
				if (i == 0) {
					ManagementTokenAPI.rediscoverTokens();
					hwInfo = ManagementTokenAPI.getHardwareInfo();
				} else {
					System.out.println("Exiting ...");
					System.exit(1);
				}
			}
			if (isAcceptableVersion(hwInfo.getVersion())) {
				System.out.println("Token Versions");
				System.out.println("==============");
				System.out.println("Version         : " + StringUtils.versionToString(hwInfo.getVersion()));
				System.out.println("Interact Flags  : "
						+ BinUtils.toHexString(new byte[] { hwInfo.getInteractiveCapabilities() }));
				System.out.println("Crypto Flags    : " + BinUtils.toHexString(hwInfo.getCryptoCapabities()));
				System.out.println("User Types Flags: " + hwInfo.getUserCapabilitiesStr());
				System.out.println("SecureChnl Flags: " + hwInfo.getScpCapabilitiesStr());
				System.out.println("Backup Fmt Flags: " + hwInfo.getBackupCapabilitiesStr());
				System.out.println("Mem Persistent  : " + hwInfo.getMemPersist() + " bytes free");
				System.out.println("Mem Resettable  : " + hwInfo.getMemTempReset() + " bytes free");
				System.out.println("Mem Deselectable: " + hwInfo.getMemTempDeselect() + " bytes free");
				if (!ManagementTokenAPI.openSecureChannel()) {
					JOptionPane.showMessageDialog(null,
							"Failed to establish secure communication with token !\r\nExiting ...",
							"Failed Secure Communication To Token", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Unacceptable software or token version !\r\nExiting ...",
						"Incorrect Software or Hardware Version", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			gui = new MainFrame(ManagementTokenAPI.getSelectedToken());
			gui.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isAcceptableVersion(int version) {
		if ((version >= Constants.MIN_VERSION) && (Constants.MIN_VERSION <= Constants.VERSION)) {
			return true;
		}

		return false;
	}

	public static int getConnectedTokenVersion() {
		if (hwInfo != null) {
			return hwInfo.getVersion();
		}
		return -1;
	}

	public static HardwareInfo getConnectedTokenHardwareInfo() {
		return hwInfo;
	}

	public static void performDisconnect() {
		JOptionPane.showMessageDialog(null,
				"<HTML><BODY><H2>Token has been DISCONNECTED !</H2>Please insert token and relaunch program.</BODY></HTML>",
				"Token Disconnected", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}
