package org.thothtrust.sc.t104.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.thothtrust.sc.t104.AOCContainerObject;
import org.thothtrust.sc.t104.Constants;
import org.thothtrust.sc.t104.HardwareInfo;
import org.thothtrust.sc.t104.Main;
import org.thothtrust.sc.t104.SCPCapability;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.sc.APDUResult;
import org.thothtrust.sc.t104.sc.Device;
import org.thothtrust.sc.t104.sc.DeviceManager;
import org.thothtrust.sc.t104.sc.scp.SCPCryptoUtil;
import org.thothtrust.sc.t104.sc.scp.SCPSession;
import org.thothtrust.sc.t104.util.BinUtils;

public class ManagementTokenAPI {

	private static volatile Device managementTokenDev = null;
	private static SCPSession sessionData = null;
	private static HardwareInfo hwInfo = null;
	private static ArrayList<SCPCapability> scpList = null;

	public static void init() {
		try {
			if (managementTokenDev != null) {
				try {
					// Catch possible exception that arises when card is removed or disconnected but
					// instance exists
					managementTokenDev.send(new CommandAPDU((byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x00));
				} catch (CardException e) {
					System.out.println("Token prob here ...");
					// Remove instances
					managementTokenDev = null;
				}
			}

			// No tag instance detected
			if (managementTokenDev == null) {
				// Scan for tag
				System.out.println("Scanning for management tokens ...");
				DeviceManager devMan = DeviceManager.getInstance();
				devMan.refreshDevices(false);
				ArrayList<Device> devices = devMan.getDevices();
				if (devices.size() > 0) {
					System.out.println("Found a management token ...");
					managementTokenDev = devices.get(0);
					System.out.println("Token terminal: " + managementTokenDev.getTerminalName());
				}
			}

			// Final check if tag instance exists
			if (managementTokenDev == null) {
				System.out.println("No management token device found !");
			}
		} catch (CardException e) {
			e.printStackTrace();
		}
	}

	public static boolean isTokenReady() {
		if (managementTokenDev != null) {
			if (managementTokenDev.getChannel() != null) {
				return true;
			}
		}

		return false;
	}

	public static Device getSelectedToken() {
		return managementTokenDev;
	}

	public static void rediscoverTokens() {
		init();
	}

	private static APDUResult rawTokenEnquiry(Device tag)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		int lc = 255;
		if (tag != null) {
			if (!tag.isRequireLc()) {
				lc = 0;
			}
			System.out.println("ManagementTokenAPI :: rawTokenEnquiry() ...");
			return new APDUResult(
					tag.send(new CommandAPDU((byte) 0x88, (byte) 0xB2, (byte) 0x00, (byte) 0x00, null, lc)));
		}
		return null;
	}

	private static APDUResult rawListAOCContainerSlots(Device tag)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0xB2, (byte) 0x01, (byte) 0x00, null, 255);
		}
		return null;
	}

	private static APDUResult rawAOCContainerBySlotNumber(Device tag, byte slotNumber)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0xB2, (byte) 0x02, (byte) 0x00, new byte[] { slotNumber }, 255);
		}
		return null;
	}

	private static APDUResult rawClearAOCContainers(Device tag)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0x0E, (byte) 0x00, (byte) 0x00, null, 255);
		}
		return null;
	}

	private static APDUResult rawGetUserTriesRemaining(Device tag, byte userID)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0xB2, (byte) 0x04, userID, null, 255);
		}
		return null;
	}

	private static APDUResult rawEditAOCContainer(Device tag, byte[] containerData)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0xD2, (byte) 0x00, (byte) 0x00, containerData, 255);
		}
		return null;
	}

	private static APDUResult rawUpdatePin(Device tag, byte userID, byte[] newPin)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0xD2, (byte) 0x01, userID, newPin, 255);
		}
		return null;
	}

	private static APDUResult rawAuthenticate(Device tag, byte userID, byte[] userPin)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0x82, userID, (byte) 0x00, userPin, 255);
		}
		return null;
	}

	private static APDUResult rawWhoAmI(Device tag)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0x82, (byte) 0xFF, (byte) 0x00, null, 255);
		}
		return null;
	}

	private static APDUResult rawLogout(Device tag)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		if (tag != null) {
			return sendMessage((byte) 0x88, (byte) 0xFE, (byte) 0x00, (byte) 0x00, null, 0);
		}
		return null;
	}

	public static byte[] getTokenLongTermPubKey()
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidParameterSpecException, InvalidKeySpecException, CardException, InvalidSizeException {
		return getHardwareInfo().getHwIDPubKeyBytes();
	}

	public static HardwareInfo getHardwareInfo()
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidParameterSpecException, InvalidKeySpecException, CardException, InvalidSizeException {
		if (hwInfo == null) {
			// Load HW info from token
			init();

			// Attempts to enquire tag status
			APDUResult res = rawTokenEnquiry(managementTokenDev);
			if (res != null) {
				if (res.isSuccess()) {
					byte[] tagStatuses = res.getResult();
					if (tagStatuses.length < 2) {
						System.out.println("Invalid statuses length returned: " + tagStatuses.length);
					} else {
						hwInfo = new HardwareInfo(tagStatuses);
					}
				} else {
					System.out.println("Failed to enquire token staus: " + BinUtils.toHexString(res.getSw()));
				}
			}
		}
		return hwInfo;
	}

	public static boolean openSecureChannel()
			throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException,
			CardException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidSizeException {
		APDUResult res = null;

		if (managementTokenDev != null) {
			// Open secure channel according to protocol
			if (scpList == null) {
				scpList = Main.getConnectedTokenHardwareInfo().getScpCapabilities();
				if (scpList == null) {
					return false;
				}
				if (scpList.size() == 0) {
					return false;
				}
			}

			// Iterate and try all the SCP list in the SCP Capabilities list
			for (SCPCapability cap : scpList) {
				switch (cap.getScpType()) {
				case Constants.SCP_TT_TP_A03:
					sessionData = SCPCryptoUtil.openThetaPassSecureChannel(managementTokenDev);
					if (sessionData == null) {
						return false;
					}
					sessionData.setScpType(cap.getScpType());
					return true;
				}
			}
		}
		return false;
	}

	private static APDUResult sendMessage(byte CLA, byte INS, byte P1, byte P2, byte[] data, int lc)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		APDUResult res = null;
		APDUResult outRes = null;
		int dataLen = 0;
		int send = 1;

		if (managementTokenDev != null) {
			if (sessionData != null) {
				System.out.println("Sending secure message ...");

				// Check if sessionData scpType is set
				if (sessionData.getScpType() == 0) {
					return null;
				}

				// Re-negotiate SCP if Counter is max (0xFFFFFFFF)
				if (requireRenegotiation()) {
					// If fail to open secure channel return null object as warning
					if (!openSecureChannel()) {
						System.out.println(
								"[ERR] Failed to open secure channel to token during required renegotiations ...");
						return null;
					}
				}

				// Allow a second and third try resend
				while (true) {
					try {
						// Third try, attempt SCP renegotiations first
						if (send > 1) {
							System.out
									.println("[WRN] Previous sendMessage() failed... retrying #" + send + " time ...");
						}
						if (send == 3) {
							// Failed SCP renegotiation, stop and declare fail
							if (!openSecureChannel()) {
								System.out.println(
										"[ERR] Failed to open secure channel to token during required renegotiations ...");
								return null;
							}
						} else if (send >= 4) {
							// Anything above third try, stop and declare fail
							System.out.println(
									"[ERR] Failed to open secure channel to token with too many failed mesage sending retries ...");
							break;
						}

						// Proceed if no SCP re-negotiations needed or new SCP opened successfully
						if (data != null) {
							dataLen = data.length;
						}
						byte[] apduEchoPlain = new byte[5 + dataLen];
						if (CLA != Constants.APDU_CLA_SECURE) {
							apduEchoPlain[0] = Constants.APDU_CLA_SECURE;
						} else {
							apduEchoPlain[0] = CLA;
						}
						apduEchoPlain[1] = INS;
						apduEchoPlain[2] = P1;
						apduEchoPlain[3] = P2;
						apduEchoPlain[4] = (byte) (dataLen & 0xFF);
						if (data != null) {
							System.arraycopy(data, 0, apduEchoPlain, 5, dataLen);
						}
						System.out.println("Plain APDU before wrap: " + BinUtils.toHexString(apduEchoPlain));
						byte[] apduWrapped = null;
						if (sessionData.getScpType() == Constants.SCP_TT_TP_A03) {
							apduWrapped = SCPCryptoUtil.handleThetaPassMessage(true, sessionData, apduEchoPlain, 0,
									apduEchoPlain.length);
						}
						if (apduWrapped == null) {
							System.out.println("[ERR] Wrapping message failed !!!");
							send++;
//							continue;
//							return null;
						} else {
							System.out.println("Wrapped APDU: " + BinUtils.toHexString(apduWrapped));
							byte[] wrappedPayload = new byte[(int) (apduWrapped[4] & 0xFF)];
							System.arraycopy(apduWrapped, 5, wrappedPayload, 0, wrappedPayload.length);
							System.out.println("Wrapped Payload: " + BinUtils.toHexString(wrappedPayload));
							ResponseAPDU respApdu = managementTokenDev.send(new CommandAPDU(apduWrapped[0],
									apduWrapped[1], apduWrapped[2], apduWrapped[3], wrappedPayload, lc));
							res = new APDUResult(respApdu);

							// Receive and decrypt response apdu
							byte[] wrappedResponse = respApdu.getBytes();
							System.out.println("Wrapped Response: " + BinUtils.toHexString(wrappedResponse));
							byte[] apduUnwrapped = null;
							if (sessionData.getScpType() == Constants.SCP_TT_TP_A03) {
								apduUnwrapped = SCPCryptoUtil.handleThetaPassMessage(false, sessionData,
										wrappedResponse, 0, wrappedResponse.length);
							}
							if (apduUnwrapped == null) {
								System.out.println("[ERR] Unwrapping message failed !!!");
//								return null;
								send++;
//								continue;
							} else {
								System.out.println("Unwrapped Response: " + BinUtils.toHexString(apduUnwrapped));
								outRes = new APDUResult(new ResponseAPDU(apduUnwrapped));

								byte[] decryptedPlaintext = null;

								if (outRes.isSuccess()) {
									// Extract decrypted plaintext without RMAC
									if (sessionData.getScpType() == Constants.SCP_TT_TP_A03) {
										decryptedPlaintext = new byte[outRes.getResult().length];
									}
									System.arraycopy(outRes.getResult(), 0, decryptedPlaintext, 0,
											decryptedPlaintext.length);
								} else {
									System.out.println("[ERR] Sending wrapped message failed !!!");
									System.out.println("SW: " + BinUtils.toHexString(res.getSw()));
									if (Constants.SW_ERROR_PROTOCOL_CHANNEL[0] == res.getSw()[0]
											&& Constants.SW_ERROR_PROTOCOL_CHANNEL[1] == res.getSw()[1]) {
										System.out.println("[ERR] Found SCP error ...");
										send++;
									} else {
										return null;
									}
								}

								outRes.setResult(decryptedPlaintext);
								break;
							}
						}
					} catch (IllegalStateException ex) {
						Main.performDisconnect();
					}
				}
			} else {
				outRes = new APDUResult(managementTokenDev.send(new CommandAPDU(CLA, INS, P1, P2, data, lc)));
			}
		}
		return outRes;
	}

	private static boolean requireRenegotiation() {
		if (sessionData != null) {
			if (sessionData.getScpType() == Constants.SCP_TT_TP_A03) {
				byte[] b = new byte[] { (byte) 0xFF, (byte) 0xFF };
				return BinUtils.binArrayElementsCompare(sessionData.getSessCtr(), 0, b, 0, 2);
			}

		}
		return false;
	}

	public static boolean authenticateUser(byte userID, char[] password)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		byte[] pin = new byte[password.length];
		for (int i = 0; i < password.length; i++) {
			pin[i] = (byte) password[i];
		}
		APDUResult res = rawAuthenticate(managementTokenDev, userID, pin);
		if (res != null) {
			return res.isSuccess();
		}
		return false;
	}

	public static boolean isUserAuthenticated(byte userID)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		APDUResult res = rawWhoAmI(managementTokenDev);
		if (res != null) {
			if (res.isSuccess()) {
				System.out.println("AuthUsr: " + BinUtils.toHexString(res.getResult()));
				if (res.getResult()[0] == userID) {
					return true;
				}
			}
		}
		return false;
	}

	public static int getTriesRemaining(byte userID)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		APDUResult res = rawGetUserTriesRemaining(managementTokenDev, userID);
		if (res.isSuccess()) {
			return (int) (res.getResult()[0] & 0xFF);
		}
		return -1;
	}

	public static boolean updatePin(byte userID, char[] password)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		byte[] pin = null;

		if (password != null) {
			pin = new byte[password.length];
			for (int i = 0; i < password.length; i++) {
				pin[i] = (byte) password[i];
			}
		} else {
			pin = new byte[] {};
		}

		APDUResult res = rawUpdatePin(managementTokenDev, userID, pin);
		if (res != null) {
			return res.isSuccess();
		}
		return false;
	}

	public static AOCContainerObject[] getAOCContainerSlotList()
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		APDUResult res = rawListAOCContainerSlots(managementTokenDev);
		if (res != null) {
			if (res.isSuccess()) {
				byte[] aocListBytes = res.getResult();
				System.out.println("AOCSlotList :: " + BinUtils.toHexString(aocListBytes));
				AOCContainerObject[] aocObjs = new AOCContainerObject[aocListBytes.length];
				for (int i = 0; i < aocListBytes.length; i++) {
					if (aocListBytes[i] != (byte) 0x00) {
						aocObjs[i] = getAOCContainerBySlotNumber(i);
					}
				}
				return aocObjs;
			}
		}
		return null;
	}

	public static boolean clearAOCContainers()
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		APDUResult res = rawClearAOCContainers(managementTokenDev);
		if (res != null) {
			return res.isSuccess();
		}
		return false;
	}

	public static boolean editAOCCOntainerSetting(byte[] settingsData)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidParameterSpecException, InvalidKeySpecException, CardException, InvalidSizeException {
		init();
		APDUResult res = rawEditAOCContainer(managementTokenDev, settingsData);
		if (res != null) {
			return res.isSuccess();
		}
		return false;
	}

	public static void logout()
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		rawLogout(managementTokenDev);
	}

	public static AOCContainerObject getAOCContainerBySlotNumber(int slotNumber)
			throws CardException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidSizeException, InvalidParameterSpecException, InvalidKeySpecException {
		init();
		APDUResult res = rawAOCContainerBySlotNumber(managementTokenDev, (byte) (slotNumber & 0xFF));
		AOCContainerObject aocObj = null;
		if (res != null) {
			if (res.isSuccess() && res.getResult() != null) {
				// Decode TLV
				byte[] resData = res.getResult();
				byte[] credID = null;
				byte[] auxData = null;

				for (int i = 0; i < resData.length;) {
					// Read tag
					if (resData[i] == Constants.TLV_TAG_AOC_CRED_ID) {
						// CredID tag
						i++;

						// Read length
						int len = (int) (resData[i] & 0xFF);
						i++;

						// Read value
						credID = new byte[len];
						System.arraycopy(resData, i, credID, 0, len);
						i += len;
					} else if (resData[i] == Constants.TLV_TAG_AOC_AUXDATA) {
						// AuxData tag
						i++;

						// Read length
						int len = (int) (resData[i] & 0xFF);
						i++;

						// Read value
						auxData = new byte[len];
						System.arraycopy(resData, i, auxData, 0, len);
						i += len;
					} else {
						// Return null due to incorrect tag
						break;
					}
				}

				if (credID != null && auxData != null) {
					aocObj = new AOCContainerObject(credID, auxData);
				}
			}
		}
		return aocObj;
	}

	public static String getUserRole(int i) {
		if (i == 1) {
			return "Token Admin";
		} else if (i == 2) {
			return "Key Manager";
		} else if (i == 3) {
			return "Key User";
		}
		return null;
	}
}