package org.thothtrust.sc.t104.util;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.KeyAgreement;

public class CryptoUtil {
	
	public static KeyPair generateECKeyPair(String algo)
			throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(algo));
		java.security.spec.ECParameterSpec ecParameterSpec = parameters
				.getParameterSpec(java.security.spec.ECParameterSpec.class);
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		keyGen.initialize(ecParameterSpec);
		KeyPair kp = keyGen.generateKeyPair();
		ECPublicKey publicKey = (ECPublicKey) kp.getPublic();
		return kp;
	}

	public static byte[] jceECPublicKey256ToBytes(ECPublicKey pubKey) {
		byte[] tmpECPublicKey = new byte[65];
		int cursor = 0;
		int copy = 0;
		boolean isProblematicKey = false;
		tmpECPublicKey[cursor] = (byte) 0x04;
		cursor++;
		// Handle key material via key length checking
		byte[] X = pubKey.getW().getAffineX().toByteArray();
		if (X.length > 32) {
			copy = 1;
			System.arraycopy(X, copy, tmpECPublicKey, cursor, 32);
		} else if (X.length < 32) {
			copy = 32 - X.length;
			System.arraycopy(X, 0, tmpECPublicKey, cursor + copy, X.length);
		} else {
			System.arraycopy(X, copy, tmpECPublicKey, cursor, 32);
		}
		cursor += 32;
		copy = 0;

		byte[] Y = pubKey.getW().getAffineY().toByteArray();
		if (Y.length > 32) {
			copy = 1;
			System.arraycopy(Y, copy, tmpECPublicKey, cursor, 32);
		} else if (Y.length < 32) {
			copy = 32 - Y.length;
			System.arraycopy(Y, 0, tmpECPublicKey, cursor + copy, Y.length);
		} else {
			System.arraycopy(Y, copy, tmpECPublicKey, cursor, 32);
		}
		cursor = 0;
		copy = 0;

		System.out.println("rX: " + BinUtils.toHexString(X));
		System.out.println("rY: " + BinUtils.toHexString(Y));

		if (!isProblematicKey) {
			return tmpECPublicKey;
		} else {
			return null;
		}
	}
	
	public static byte[] getPublicKeyBytes(KeyPair kp, int keyLength, boolean hasHeader) {
		int bytesToCopy = 1 + (2 * keyLength);
		byte[] outFormattedPubKey = new byte[1 + (2 * keyLength)];
		int cursor = 0;
		int copy = 0;
		outFormattedPubKey[cursor] = (byte) 0x04;

		if (!hasHeader) {
			bytesToCopy = 2 * keyLength;
			outFormattedPubKey = new byte[bytesToCopy];
		} else {
			cursor++;
		}

		// Handle key material via key length checking
		byte[] X = ((ECPublicKey) kp.getPublic()).getW().getAffineX().toByteArray();
		if (X.length > keyLength) {
			copy = 1;
			System.arraycopy(X, copy, outFormattedPubKey, cursor, keyLength);
		} else if (X.length < keyLength) {
			copy = keyLength - X.length;
			System.arraycopy(X, 0, outFormattedPubKey, cursor + copy, X.length);
		} else {
			System.arraycopy(X, copy, outFormattedPubKey, cursor, keyLength);
		}
		System.out.println("[DBG] X: " + BinUtils.toHexString(X));
		cursor += keyLength;
		copy = 0;
		byte[] Y = ((ECPublicKey) kp.getPublic()).getW().getAffineY().toByteArray();
		if (Y.length > keyLength) {
			copy = 1;
			System.arraycopy(Y, copy, outFormattedPubKey, cursor, keyLength);
		} else if (Y.length < keyLength) {
			copy = keyLength - Y.length;
			System.arraycopy(Y, 0, outFormattedPubKey, cursor + copy, Y.length);
		} else {
			System.arraycopy(Y, copy, outFormattedPubKey, cursor, keyLength);
		}
		System.out.println("[DBG] Y: " + BinUtils.toHexString(Y));
		return outFormattedPubKey;
	}

	public static byte[] getPublicKeyBytes(ECPublicKey publicKey, int keyLength, boolean hasHeader) {
		int bytesToCopy = 1 + (2 * keyLength);
		byte[] outFormattedPubKey = new byte[1 + (2 * keyLength)];
		int cursor = 0;
		int copy = 0;
		outFormattedPubKey[cursor] = (byte) 0x04;

		if (!hasHeader) {
			bytesToCopy = 2 * keyLength;
			outFormattedPubKey = new byte[bytesToCopy];
		} else {
			cursor++;
		}

		// Handle key material via key length checking
		byte[] X = publicKey.getW().getAffineX().toByteArray();
		if (X.length > keyLength) {
			copy = 1;
			System.arraycopy(X, copy, outFormattedPubKey, cursor, keyLength);
		} else if (X.length < keyLength) {
			copy = keyLength - X.length;
			System.arraycopy(X, 0, outFormattedPubKey, cursor + copy, X.length);
		} else {
			System.arraycopy(X, copy, outFormattedPubKey, cursor, keyLength);
		}
		System.out.println("[DBG] X: " + BinUtils.toHexString(X));
		cursor += keyLength;
		copy = 0;
		byte[] Y = publicKey.getW().getAffineY().toByteArray();
		if (Y.length > keyLength) {
			copy = 1;
			System.arraycopy(Y, copy, outFormattedPubKey, cursor, keyLength);
		} else if (Y.length < keyLength) {
			copy = keyLength - Y.length;
			System.arraycopy(Y, 0, outFormattedPubKey, cursor + copy, Y.length);
		} else {
			System.arraycopy(Y, copy, outFormattedPubKey, cursor, keyLength);
		}
		System.out.println("[DBG] Y: " + BinUtils.toHexString(Y));
		return outFormattedPubKey;
	}

	public static PublicKey getPublicKey(byte[] xyBytes, String ecAlgo)
			throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
		int keyLength = xyBytes.length / 2;
		byte[] xBytes = new byte[keyLength];
		byte[] yBytes = new byte[keyLength];
		System.arraycopy(xyBytes, 0, xBytes, 0, keyLength);
		System.arraycopy(xyBytes, keyLength, yBytes, 0, keyLength);
		BigInteger x = new BigInteger(BinUtils.toHexString(xBytes), 16);
		BigInteger y = new BigInteger(BinUtils.toHexString(yBytes), 16);
		ECPoint w = new ECPoint(x, y);
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(ecAlgo));
		java.security.spec.ECParameterSpec ecParameterSpec = parameters
				.getParameterSpec(java.security.spec.ECParameterSpec.class);
		ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(w, ecParameterSpec);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		return (ECPublicKey) keyFactory.generatePublic(ecPublicKeySpec);
	}

	public static PublicKey getPublicKey(byte[] xBytes, byte[] yBytes)
			throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {

		BigInteger x = new BigInteger(BinUtils.toHexString(xBytes), 16);
		BigInteger y = new BigInteger(BinUtils.toHexString(yBytes), 16);
		ECPoint w = new ECPoint(x, y);
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec("secp256r1"));
		java.security.spec.ECParameterSpec ecParameterSpec = parameters
				.getParameterSpec(java.security.spec.ECParameterSpec.class);
		ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(w, ecParameterSpec);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		return (ECPublicKey) keyFactory.generatePublic(ecPublicKeySpec);
	}
	
	public static PublicKey getPublicKeyWithASN1Header(byte[] asnXYBytes, String ecAlgo)
			throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
		if (asnXYBytes.length % 2 != 0 && asnXYBytes[0] != (byte) 0x04) {
			return null;
		}
		int keyLength = ((asnXYBytes.length - 1) / 2);
		byte[] xBytes = new byte[keyLength];
		byte[] yBytes = new byte[keyLength];
		System.arraycopy(asnXYBytes, 1, xBytes, 0, keyLength);
		System.arraycopy(asnXYBytes, keyLength + 1, yBytes, 0, keyLength);
		BigInteger x = new BigInteger(BinUtils.toHexString(xBytes), 16);
		BigInteger y = new BigInteger(BinUtils.toHexString(yBytes), 16);
		ECPoint w = new ECPoint(x, y);
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(ecAlgo));
		java.security.spec.ECParameterSpec ecParameterSpec = parameters
				.getParameterSpec(java.security.spec.ECParameterSpec.class);
		ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(w, ecParameterSpec);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		return (ECPublicKey) keyFactory.generatePublic(ecPublicKeySpec);
	}

	public static byte[] deriveECSharedSecret(byte[] pubKey, KeyPair kp) throws NoSuchAlgorithmException,
			InvalidParameterSpecException, InvalidKeySpecException, InvalidKeyException {
		byte[] xBytes = new byte[32];
		byte[] yBytes = new byte[32];
		System.arraycopy(pubKey, 0, xBytes, 0, 32);
		System.arraycopy(pubKey, 32, yBytes, 0, 32);
		ECPublicKey targetPubKey = (ECPublicKey) getPublicKey(xBytes, yBytes);
		KeyAgreement ka = KeyAgreement.getInstance("ECDH");
		ka.init(kp.getPrivate());
		ka.doPhase(targetPubKey, true);
		return ka.generateSecret();
	}

	public static byte[] deriveECSharedSecret(ECPublicKey targetPubKey, KeyPair kp) throws NoSuchAlgorithmException,
			InvalidParameterSpecException, InvalidKeySpecException, InvalidKeyException {
		KeyAgreement ka = KeyAgreement.getInstance("ECDH");
		ka.init(kp.getPrivate());
		ka.doPhase(targetPubKey, true);
		return ka.generateSecret();
	}
}
