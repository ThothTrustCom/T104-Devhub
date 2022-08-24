package org.thothtrust.sc.t104.sc.scp;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.thothtrust.sc.t104.util.BinUtils;

public class SCPSession {

	private short scpType= 0;
	private byte[] sessCtr = null;
	private byte[] sessDevCtr = null;
	private byte[] sessNonce = null;
	private byte[] sessMacChain = null;
	private byte[] ocePublicKeyData = null;
	private byte[] cardEphemeralPublicKeyData = null;
	private Key receiptKey = null;
	private Key SENCKey = null;
	private Key SMACKey = null;
	private Key SRMACKey = null;
	private Key SDEKKey = null;

	public SCPSession() {
	}

	public SCPSession(byte[] keystream, int keyStreamOff, byte[] ocePublicKeyData, byte[] cardEphemeralPublicKeyData) {
		sessCtr = new byte[16];
		sessMacChain = new byte[16];
		sessCtr[15] = (byte) 0x01;
		receiptKey = new SecretKeySpec(keystream, keyStreamOff, 32, "AES");
		System.out.println("SCPSession :: Receipt Key: " + BinUtils.toHexString(receiptKey.getEncoded()));
		SENCKey = new SecretKeySpec(keystream, keyStreamOff + 32, 32, "AES");
		System.out.println("SCPSession :: SENC Key: " + BinUtils.toHexString(SENCKey.getEncoded()));
		SMACKey = new SecretKeySpec(keystream, keyStreamOff + 64, 32, "AES");
		System.out.println("SCPSession :: SMAC Key: " + BinUtils.toHexString(SMACKey.getEncoded()));
		SRMACKey = new SecretKeySpec(keystream, keyStreamOff + 96, 32, "AES");
		System.out.println("SCPSession :: SRMAC Key: " + BinUtils.toHexString(SRMACKey.getEncoded()));
		SDEKKey = new SecretKeySpec(keystream, keyStreamOff + 128, 32, "AES");
		System.out.println("SCPSession :: SDEK Key: " + BinUtils.toHexString(SDEKKey.getEncoded()));
		setOcePublicKeyData(ocePublicKeyData);
		setCardEphemeralPublicKeyData(cardEphemeralPublicKeyData);
	}

	public SCPSession(byte[] sessCtr, byte[] sessDevCtr, byte[] sessNonce, byte[] sessMacChain, byte[] ocePublicKeyData,
			byte[] cardEphemeralPublicKeyData, Key receiptKey, Key SENCKey, Key SMACKey, Key SRMACKey, Key SDEKKey) {
		setSessCtr(sessCtr);
		setSessDevCtr(sessDevCtr);
		setSessNonce(sessNonce);
		setSessMacChain(sessMacChain);
		setOcePublicKeyData(ocePublicKeyData);
		setCardEphemeralPublicKeyData(cardEphemeralPublicKeyData);
		setReceiptKey(receiptKey);
		setSENCKey(SENCKey);
		setSMACKey(SMACKey);
		setSRMACKey(SRMACKey);
		setSDEKKey(SDEKKey);
	}
	
	public short getScpType() {
		return scpType;
	}

	public void setScpType(short scpType) {
		this.scpType = scpType;
	}

	public byte[] getSessCtr() {
		return sessCtr;
	}

	public void setSessCtr(byte[] sessCtr) {
		this.sessCtr = sessCtr;
	}

	public byte[] getSessDevCtr() {
		return sessDevCtr;
	}

	public void setSessDevCtr(byte[] sessDevCtr) {
		this.sessDevCtr = sessDevCtr;
	}

	public byte[] getSessNonce() {
		return sessNonce;
	}

	public void setSessNonce(byte[] sessNonce) {
		this.sessNonce = sessNonce;
	}

	public byte[] getSessMacChain() {
		return sessMacChain;
	}

	public void setSessMacChain(byte[] sessMacChain) {
		this.sessMacChain = sessMacChain;
	}

	public Key getReceiptKey() {
		return receiptKey;
	}

	public void setReceiptKey(Key receiptKey) {
		this.receiptKey = receiptKey;
	}

	public Key getSENCKey() {
		return SENCKey;
	}

	public void setSENCKey(Key sENCKey) {
		SENCKey = sENCKey;
	}

	public Key getSMACKey() {
		return SMACKey;
	}

	public void setSMACKey(Key sMACKey) {
		SMACKey = sMACKey;
	}

	public Key getSRMACKey() {
		return SRMACKey;
	}

	public void setSRMACKey(Key sRMACKey) {
		SRMACKey = sRMACKey;
	}

	public Key getSDEKKey() {
		return SDEKKey;
	}

	public void setSDEKKey(Key sDEKKey) {
		SDEKKey = sDEKKey;
	}

	public byte[] getOcePublicKeyData() {
		return ocePublicKeyData;
	}

	public void setOcePublicKeyData(byte[] ocePublicKeyData) {
		this.ocePublicKeyData = ocePublicKeyData;
	}

	public byte[] getCardEphemeralPublicKeyData() {
		return cardEphemeralPublicKeyData;
	}

	public void setCardEphemeralPublicKeyData(byte[] cardEphemeralPublicKeyData) {
		this.cardEphemeralPublicKeyData = cardEphemeralPublicKeyData;
	}

}