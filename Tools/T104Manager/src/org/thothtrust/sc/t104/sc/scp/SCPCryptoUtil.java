package org.thothtrust.sc.t104.sc.scp;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.swing.JOptionPane;

import org.thothtrust.sc.t104.Constants;
import org.thothtrust.sc.t104.Main;
import org.thothtrust.sc.t104.api.ManagementTokenAPI;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.sc.Device;
import org.thothtrust.sc.t104.sc.DeviceHelper;
import org.thothtrust.sc.t104.util.BinUtils;
import org.thothtrust.sc.t104.util.CryptoUtil;
import org.thothtrust.sc.t104.util.MathUtil;

public class SCPCryptoUtil {

	public static SCPSession openThetaPassSecureChannel(Device tempDev)
			throws CardException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException,
			InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidSizeException {
		boolean allowProceed = false;
		ECPrivateKey hostPrivateKey = null;
		ECPublicKey hostPublicKey = null;
		byte[] hostPubKeyBytes = null;
		byte[] cardPubKeyBytes = null;
		ResponseAPDU resp = null;
		byte[] sessKey = null;
		MessageDigest hash = MessageDigest.getInstance("SHA-256");
		byte[] cNonce = null;
		SCPSession sess = null;

		// Get card public key
		cardPubKeyBytes = ManagementTokenAPI.getTokenLongTermPubKey();

		System.out.println("Card Public Key: " + BinUtils.toHexString(cardPubKeyBytes));
		if (cardPubKeyBytes != null) {
			allowProceed = true;
		}

		if (allowProceed) {
			// Generate ephemeral keypair
			KeyPair kp = CryptoUtil.generateECKeyPair("secp256k1");
			hostPublicKey = (ECPublicKey) kp.getPublic();
			hostPrivateKey = (ECPrivateKey) kp.getPrivate();

			// Get public key bytes
			hostPubKeyBytes = CryptoUtil.getPublicKeyBytes(hostPublicKey, 32, true);
			System.out.println("Host Key: " + BinUtils.toHexString(hostPubKeyBytes));
		}

		if (allowProceed) {
			// Convert card public key bytes to ECPublicKey
			ECPublicKey cardECPubKey = (ECPublicKey) CryptoUtil.getPublicKeyWithASN1Header(cardPubKeyBytes,
					"secp256k1");

			// Execute KeyAgreement scheme
			KeyAgreement ka = KeyAgreement.getInstance("ECDH");
			ka.init(hostPrivateKey);
			ka.doPhase(cardECPubKey, true);
			byte[] rawSecret = ka.generateSecret();

			System.out.println("Raw Secret: " + BinUtils.toHexString(rawSecret));

			// Do SHA256 hash on raw ECDH secret resulting from KeyAgreement scheme
			sessKey = hash.digest(rawSecret);
			System.out.println("Host Shared Secret: " + BinUtils.toHexString(sessKey));

			System.out.println("Generating confirmation code ...");
			hash.reset();
			byte[] rawOTPInput = hash.digest(sessKey);
			int offset = (int) (rawOTPInput[(short) 31] & 0xf);

			int binary = ((rawOTPInput[offset] & 0x7f) << 24) | ((rawOTPInput[offset + 1] & 0xff) << 16)
					| ((rawOTPInput[offset + 2] & 0xff) << 8) | (rawOTPInput[offset + 3] & 0xff);

			byte[] ib1 = new byte[4];
			byte[] ib2 = new byte[10];
			byte[] finalOTPAsc = new byte[6];
			MathUtil.intToBytes(binary % 1000000, ib1, (short) 0);
			MathUtil.toDecimalASCII(ib1, (short) 0, (short) 4, ib2, (short) 0);
			System.arraycopy(ib2, 4, finalOTPAsc, 0, 6);
			String otpCodeText = BinUtils.toAsciiString(finalOTPAsc);
			System.out.println("Expected OTP Code: " + otpCodeText);

			hash.reset();
			hash.update(hostPubKeyBytes);
			hash.update(cardPubKeyBytes);
			byte[] digestData = hash.digest();
			cNonce = new byte[12];
			System.arraycopy(digestData, 0, cNonce, 0, 12);
			System.out.println("cNonce: " + BinUtils.toHexString(cNonce));

			// Seperate runnable session for those with screen
			if ((byte) (Main.getConnectedTokenHardwareInfo().getInteractiveCapabilities()
					& Constants.INTERACT_SCREEN) == Constants.INTERACT_SCREEN) {
				Thread thread = new Thread() {
					public void run() {
						JOptionPane.showMessageDialog(null,
								"<html><center>Please verify OTP Code: <br><h1>" + otpCodeText
										+ "</h1></center></html>",
								"Secure Channel Verification", JOptionPane.INFORMATION_MESSAGE);
					}
				};
				thread.start();
			}

			// Begin SC APDU
			resp = tempDev
					.send(new CommandAPDU((byte) 0x88, (byte) 0x88, (byte) 0x00, (byte) 0x00, hostPubKeyBytes, 0));
			System.out.println("Resp: " + BinUtils.toHexString(resp.getBytes()));

			// Create session of 2 bytes (short) counters starting at '0' for device and
			// client and share the sessKey for RMAC, MAC, SENC, SDEC keys.
			if (DeviceHelper.isSuccessfulResponse(resp)) {
				sess = new SCPSession(new byte[2], new byte[2], cNonce, null, null, null, null,
						new SecretKeySpec(sessKey, "AES"), new SecretKeySpec(sessKey, "HmacSHA256"),
						new SecretKeySpec(sessKey, "HmacSHA256"), new SecretKeySpec(sessKey, "AES"));
			}
		}

		return sess;
	}

	public static byte[] handleThetaPassMessage(boolean isWrap, SCPSession sessionData, byte[] apduBuf, int apduBufOff,
			int apduLen) throws InvalidKeyException, InvalidAlgorithmParameterException, InvalidSizeException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] ret = null;
		boolean allowProceed = false;
		MessageDigest hash = MessageDigest.getInstance("SHA-256");
		Cipher cryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
		if (isWrap) {
			System.out.println("Begin ThetaPass message wrap ...");
			if (apduLen > 198) {
				throw new InvalidSizeException("APDU Length is more than 197 bytes");
			}

			// Check apdu where CLA, INS, P1, P2, Lc must match total apduLen
			if ((short) (apduLen - 5) != (short) (apduBuf[apduBufOff + 4] & 0xFF)) {
				throw new InvalidSizeException("APDU Length is invalid");
			}

			// Compute IV
			byte[] ivBase = new byte[16];
			short sessCtr = BinUtils.bytesToShort(sessionData.getSessCtr()[0], sessionData.getSessCtr()[1]);
			short devCtr = BinUtils.bytesToShort(sessionData.getSessDevCtr()[0], sessionData.getSessDevCtr()[1]);

			System.out.println("SCPCryptoUtil.handleThetaPassMessage().decrypt: get sessCtr: " + sessCtr);

			System.arraycopy(sessionData.getSessNonce(), 0, ivBase, 0, 12);
			BinUtils.shortToBytes(sessCtr, ivBase, (short) 12);
			BinUtils.shortToBytes(devCtr, ivBase, (short) 14);
			System.out.println("IVBase: " + BinUtils.toHexString(ivBase));
			hash.reset();
			byte[] hashIV = hash.digest(ivBase);
			byte[] iv = new byte[16];
			System.arraycopy(hashIV, 0, iv, 0, 16);
			System.out.println("IV: " + BinUtils.toHexString(iv));

			// Encrypt data
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cryptor.init(Cipher.ENCRYPT_MODE, sessionData.getSENCKey(), ivSpec);
			byte[] ciphertext = cryptor.doFinal(apduBuf, apduBufOff + 5, apduLen - 5);
			System.out.println("Ciphertext: " + BinUtils.toHexString(ciphertext));

			// Params
			byte[] params = new byte[4];
			System.arraycopy(apduBuf, apduBufOff, params, 0, 4);

			// Hash ciphertext and concatenate in front with IV before MAC-ing with
			// HMACSHA256(SHA256(IV | Ciphertext), MACKey)
			hash.reset();
			hash.update(params);
			hash.update(iv);
			byte[] tbs = hash.digest(ciphertext);
			System.out.println("Hash: " + BinUtils.toHexString(tbs));
			Mac hmac = Mac.getInstance("HmacSHA256");
			hmac.init(sessionData.getSMACKey());
			byte[] mac = hmac.doFinal(tbs);
			System.out.println("MAC: " + BinUtils.toHexString(mac));

			byte[] result = new byte[ciphertext.length + mac.length];
			System.arraycopy(ciphertext, 0, result, 0, ciphertext.length);
			System.arraycopy(mac, 0, result, ciphertext.length, mac.length);
			System.out.println("Ciphered cryptogram: " + BinUtils.toHexString(result));

			// Increment client counter
			System.out.println("Increment ThetaKey Client Ctr ...");
//			System.out.println("SCPCryptoUtil.handleThetaPassMessage().encrypt: sessCtr: " + sessCtr);
			sessCtr++;
//			System.out.println("SCPCryptoUtil.handleThetaPassMessage().encrypt: after inc: " + sessCtr);

			// Set back counter into sessionData
			byte[] sBytes = new byte[2];
			BinUtils.shortToBytes(sessCtr, sBytes, (short) 0);
			sessionData.setSessCtr(sBytes);
//			System.out.println("SCPCryptoUtil.handleThetaPassMessage().encrypt: setting inc: " + BinUtils.toHexString(sBytes));

			// Return proper APDU CLA, INS, P1, P2, Lc, Encrypted-and-MAC-Content
			ret = new byte[5 + result.length];
			System.arraycopy(params, 0, ret, 0, params.length);
			ret[4] = (byte) (result.length & 0xFF);
			System.arraycopy(result, 0, ret, 5, result.length);
		} else {
			System.out.println("Begin ThetaPass message unwrap ...");
			if (apduLen == 2) {
				// Most likely is SW so return as is, increment counter and handle as non-wrapped SW
				// Increment device counter
				System.out.println("[INF] Increment ThetaKey Device Ctr ...");
				short devCtr = BinUtils.bytesToShort(sessionData.getSessDevCtr()[0], sessionData.getSessDevCtr()[1]);
				devCtr++;

				// Set back counter into sessionData
				byte[] sBytes = new byte[2];
				BinUtils.shortToBytes(devCtr, sBytes, (short) 0);
				sessionData.setSessDevCtr(sBytes);
				
				ret = new byte[apduLen];
				System.arraycopy(apduBuf, apduBufOff, ret, 0, apduLen);
				return ret;
			}

			// There is at least an SW and a RMAC involved
//			if (apduLen >= 34) {
				// Compute IV
				byte[] plaintext = null;
				byte[] ivBase = new byte[16];
				System.arraycopy(sessionData.getSessNonce(), 0, ivBase, 0, 12);
				System.arraycopy(sessionData.getSessCtr(), 0, ivBase, 12, 2);
				System.arraycopy(sessionData.getSessDevCtr(), 0, ivBase, 14, 2);
				hash.reset();
				byte[] hashIV = hash.digest(ivBase);
				byte[] iv = new byte[16];
				System.arraycopy(hashIV, 0, iv, 0, 16);
				System.out.println("IVBase: " + BinUtils.toHexString(ivBase));
				System.out.println("IV: " + BinUtils.toHexString(iv));

				// Hash ciphertext and concatenate in front with IV before MAC-ing with
				// HMACSHA256(SHA256(SW | IV | Ciphertext), MACKey)
				hash.reset();
				hash.update(apduBuf, apduBufOff + apduLen - 2, 2); // SW
				hash.update(iv);
				hash.update(apduBuf, apduBufOff, apduLen - 34); // Content without SW and RMAC
				byte[] tbs = hash.digest();
				System.out.println("Hash: " + BinUtils.toHexString(tbs));
				Mac hmac = Mac.getInstance("HmacSHA256");
				hmac.init(sessionData.getSRMACKey());
				byte[] mac = hmac.doFinal(tbs);
				System.out.println("[INF] Computed MAC: " + BinUtils.toHexString(mac));
				System.out.println(
						"[INF] Found MAC: " + BinUtils.toHexString(apduBuf, (apduBufOff + (apduLen - 34)), 32));

				if (BinUtils.binArrayElementsCompare(mac, 0, apduBuf, (apduBufOff + (apduLen - 34)), 32)) {
					// Decrypt data
					IvParameterSpec ivSpec = new IvParameterSpec(iv);
					cryptor.init(Cipher.DECRYPT_MODE, sessionData.getSDEKKey(), ivSpec);
					plaintext = cryptor.doFinal(apduBuf, apduBufOff, apduLen - 34);
					System.out.println("[INF] Plaintext: " + BinUtils.toHexString(plaintext));
				} else {
					System.out.println("[ERR] Decryption MAC does not match !!! ");
				}

				// Increment device counter
				System.out.println("[INF] Increment ThetaKey Device Ctr ...");
				short devCtr = BinUtils.bytesToShort(sessionData.getSessDevCtr()[0], sessionData.getSessDevCtr()[1]);
				devCtr++;

				// Set back counter into sessionData
				byte[] sBytes = new byte[2];
				BinUtils.shortToBytes(devCtr, sBytes, (short) 0);
				sessionData.setSessDevCtr(sBytes);

				// Return proper APDU Decrypted-Content, SW
				ret = new byte[2 + plaintext.length];
				System.arraycopy(plaintext, 0, ret, 0, plaintext.length);
				System.arraycopy(apduBuf, apduBufOff + apduLen - 2, ret, plaintext.length, 2);
			}
//		}
		return ret;
	}
}