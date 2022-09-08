package KM104;

import com.es.gmspecialmethod.ESUtil;
import com.es.gmspecialmethod.ESWallet;
import com.es.gmspecialmethod.method;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;
import javacard.framework.Shareable;
import javacard.framework.Util;
import javacard.security.AESKey;
import javacard.security.ECPrivateKey;
import javacard.security.ECPublicKey;
import javacard.security.KeyAgreement;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.MessageDigest;
import javacard.security.RandomData;
import javacardx.crypto.Cipher;

public class KM104 extends Applet implements T104OpenAPI {

    public static KM104 kmInst = null;
    public static CredentialObject[] aocCreds;
    public static byte[] l0;
    public static byte[] b0;
    public static byte[] b2;
    public static byte[] tickets;
    public static byte[] c0;
    public static byte[] d0;
    public static byte[] walletBalanceData = null;
    public static byte[] walletPayData = null;
    public static byte[] walletLoadData = null;
    public static short[] sb;
    public static short[] appRet = null;
    public static short walletBalanceDataLen = 0;
    public static short walletPayDataLen = 0;
    public static short walletLoadDataLen = 0;
    public static Cipher cipher;
    public static MessageDigest sha256;
    public static AESKey storeWrapKey;
    public static AESKey sessKey = null;
    public static RandomData rand;
    public static KeyAgreement ka;
    public static final byte NULL = ACL_ALLOW_EXEC;
    public static final byte MAX_TRY_PIN_GLOB = (byte) 0x05;
    public static final short VERSION = 256;
    public static final short CAPABILITY = 0;
    public static final byte CLA = (byte) 0x88;
    public static final byte SEC_CLA = (byte) 0x84;
    public static final byte INS_PERF_SEC_OP = (byte) 0x2A;
    public static final byte INS_GET_DATA = (byte) 0xE1;
    public static final byte INS_STORE_DATA = (byte) 0xE2;
    public static final byte INS_READ_RECORD = (byte) 0xB2;
    public static final byte INS_WRITE_RECORD = (byte) 0xD2;
    public static final byte INS_ERASE_BINARY = (byte) 0x0E;
    public static final byte INS_EXECUTE_RECORD = (byte) 0xCA;
    public static final byte INS_VERIFY_PIN = (byte) 0x82;
    public static final byte INS_MUTUAL_AUTH = (byte) 0x83;
    public static final byte INS_INTERNAL_AUTH = (byte) 0x88;
    public static final byte INS_LOGOUT = (byte) 0xFE;
    public static final byte P1_FUNC_DISPLAY = (byte) 0xF1;
    public static final byte P1_FUNC_RANDOM = (byte) 0xF2;
    public static final byte TLV_TAG_HW_INFO = (byte) 0x01;
    public static final byte TLV_TAG_HW_CAP = (byte) 0x02;
    public static final byte TLV_TAG_HW_USR = (byte) 0x03;
    public static final byte TLV_TAG_HW_MEM_PERSIST = (byte) 0x04;
    public static final byte TLV_TAG_HW_MEM_TEMP_RST = (byte) 0x41;
    public static final byte TLV_TAG_HW_MEM_TEMP_DST = (byte) 0x42;
    public static final byte TLV_TAG_HW_ID = (byte) 0x05;
    public static final byte TLV_TAG_HW_ID_PUB = (byte) 0x51;
    public static final byte TLV_TAG_HW_ID_ATTEST = (byte) 0x52;
    public static final byte TLV_TAG_HW_SCP = (byte) 0x06;
    public static final byte TLV_TAG_HW_BK = (byte) 0x07;
    public static final byte TLV_TAG_HW_INTERACT = (byte) 0x08;
    public static final byte TLV_TAG_HW_CRED_FORMAT = (byte) 0x09;
    public static final byte TLV_TAG_AOC_CRED_ID = (byte) 0x0A;
    public static final byte TLV_TAG_AOC_AUXDATA = (byte) 0x0B;
    public static final byte TLV_OBJ_FIELD_ID = (byte) 0x08;
    public static final byte TLV_OBJ_FIELD_HANDLE = (byte) 0x0E;
    public static final byte INTERACT_SCREEN = (byte) 0x01;
    public static final byte USER_ADMIN = (byte) 0x01;
    public static final byte EWALLET_UPDATE_BALANCE = (byte) 0x01;
    public static final byte EWALLET_UPDATE_PAYMENT = (byte) 0x02;
    public static final byte EWALLET_UPDATE_LOADING = (byte) 0x04;
    public static final short SCP_TT_TP_A03 = 0x0A03;
    public static final short SW_CARD_NOT_READY = 0x6f1f;
    public static final short SW_CARD_TIME_ERR = 0x6f20;
    public static final short SW_INVALID_USER_ROLE = 0x6fa1;
    public static final short SW_INVALID_USER_PIN = 0x6fa2;
    public static final short SW_NO_MORE_RETRIES = 0x63c0;
    public static final short SW_UI_ERR = (short) 0x6fb0;
    public static final short SW_INVALID_ALGO = 0x6fb1;
    public static final short SW_INVALID_KEY_SIZE = 0x6fb2;
    public static final short SW_INVALID_KEY_NAME = 0x6fb3;
    public static final short SW_INVALID_KEY_NAME_SIZE = 0x6fb4;
    public static final short SW_INVALID_KEY_STATE = 0x6fb5;
    public static final short SW_INVALID_KEY_PARAM_SIZE = 0x6fb6;
    public static final short SW_INVALID_CRYPTO_MODE = 0x6fc1;
    public static final short SW_INVALID_CRYPTO_PARAM = 0x6fc2;
    public static final short SW_SCHANNEL_REQ_INIT = 0x6fcc;
    public static final short SW_SCHANNEL_ERROR = 0x6fcd;
    public static final short SW_INVALID_KEYBLOB = 0x6fd1;
    public static final short SW_ERROR_PROTOCOL_CHANNEL = 0x6fe2;
    public static KeyPair kmKP;
    public AID apiAID = null;
    public static boolean isVirgin = true;
    public static boolean isKMEnvSet = false;
    public static OwnerPIN[] credPINs = null;
    public static OwnerPIN globPIN = null; // Global PIN for managing the global wallet access
    public static final byte[] e = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    public static final byte[] aid = { (byte) 0x4B, (byte) 0x4D, (byte) 0x31, (byte) 0x30, (byte) 0x31 };
    public static byte[] PIN_GLOB = { (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36,
            (byte) 0x37, (byte) 0x38 };
    public ESUtil state;
    public method ordinmethod;
    public ESWallet qbstate;

    /**
     * Installs this applet under condition that object deletion is available.
     *
     * @param bArray  the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        if (JCSystem.isObjectDeletionSupported()) {
            kmInst = new KM104();
        }
    }

    protected KM104() {
        register();
    }

    public void initEnv() {
        l0 = JCSystem.makeTransientByteArray((short) 1, JCSystem.CLEAR_ON_RESET);
        b0 = JCSystem.makeTransientByteArray((short) 258, JCSystem.CLEAR_ON_RESET);
        b2 = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_RESET);
        c0 = JCSystem.makeTransientByteArray(MAX_AOC, JCSystem.CLEAR_ON_RESET);
        d0 = JCSystem.makeTransientByteArray((short) 52, JCSystem.CLEAR_ON_RESET);
        sb = JCSystem.makeTransientShortArray((short) 7, JCSystem.CLEAR_ON_RESET);
        appRet = JCSystem.makeTransientShortArray((short) 2, JCSystem.CLEAR_ON_RESET);
        tickets = JCSystem.makeTransientByteArray((short) (T104OpenAPI.MAX_AOC * AUTH_TICKET_LEN),
                JCSystem.CLEAR_ON_RESET);
        ka = KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH_PLAIN, false);
        JCSystem.beginTransaction();
        ECPublicKey idEcPub = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC,
                KeyBuilder.LENGTH_EC_FP_256, false);
        ECC.setCommonCurveParameters(idEcPub, (byte) 0x02);
        ECPrivateKey idEcPriv = (ECPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE,
                KeyBuilder.LENGTH_EC_FP_256, false);
        ECC.setCommonCurveParameters(idEcPriv, (byte) 0x02);
        kmKP = new KeyPair(idEcPub, idEcPriv);
        kmKP.genKeyPair();
        walletBalanceData = new byte[MAX_WALLET_DATA_LEN];
        walletPayData = new byte[MAX_WALLET_DATA_LEN];
        walletLoadData = new byte[MAX_WALLET_DATA_LEN];
        ordinmethod = new method(true);
        state = new ESUtil(ordinmethod, d0, true);
        qbstate = new ESWallet(ordinmethod, d0, true);
        aocCreds = new CredentialObject[T104OpenAPI.MAX_AOC];
        credPINs = new OwnerPIN[T104OpenAPI.MAX_AOC];
        JCSystem.commitTransaction();
        cipher = Cipher.getInstance(Cipher.ALG_AES_CBC_PKCS5, true);
        sha256 = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, true);
        storeWrapKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_256, false);
        rand = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        rand.generateData(b0, (short) 0, KeyBuilder.LENGTH_AES_256);
        JCSystem.beginTransaction();
        storeWrapKey.setKey(b0, (short) 0);
        JCSystem.commitTransaction();
        Util.arrayFillNonAtomic(b0, (short) 0, KeyBuilder.LENGTH_AES_256, NULL);
        sessKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_RESET, KeyBuilder.LENGTH_AES_256, false);
        JCSystem.beginTransaction();
        globPIN = new OwnerPIN(MAX_TRY_PIN_GLOB, (byte) (MAX_PIN_LEN & 0xFF));
        globPIN.update(PIN_GLOB, (short) 0, (byte) (PIN_GLOB.length & 0xFF));
        isKMEnvSet = true;
        JCSystem.commitTransaction();
    }

    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        return (T104OpenAPI) this;
    }

    public static void notVirgin() {
        if (isVirgin) {
            isVirgin = false;
        }
    }

    public static boolean isVirgin() {
        return isVirgin;
    }

    public static void shortToBytes(short s, byte[] b, short offset) {
        b[offset] = (byte) ((s >> 8) & 0xFF);
        b[(short) (offset + 1)] = (byte) (s & 0xFF);
    }

    public static void sendOut(byte[] input, short offset, short len, APDU apdu) {
        apdu.setOutgoing();
        apdu.setOutgoingLength(len);
        apdu.sendBytesLong(input, offset, len);
    }

    public static boolean secretEnrollmentCheck(byte secretType, byte[] secret, short secOffset, short secLen,
            short sbuff, short sbuff1) {
        if ((secret != null) && (secLen <= MAX_PIN_LEN)) {
            if (secretType == CRED_AUTHTYPE_PIN) {
                return alphaNumCheck(false, true, secret, secOffset, secLen, sbuff, sbuff1);
            } else if (secretType == CRED_AUTHTYPE_PWD) {
                return true;
            }
        }
        return false;
    }

    public static boolean alphaNumCheck(boolean allowSym, boolean isPin, byte[] input, short offset, short len,
            short sbuff, short sbuff1) {
        if ((input != null) && (len > (short) 0)) {
            for (sbuff = (short) 0; sbuff < len; sbuff++) {
                sbuff1 = (short) (input[(short) (offset + sbuff)] & 0xFF);
                if (isPin) {
                    if (!((sbuff1 >= (short) 48) && (sbuff1 <= (short) 57))) { // 0 to 9 in decimals
                        return false;
                    }
                } else {
                    if (allowSym) {
                        // Allow printable ASCII symbols, numbers and characters.
                        if (!((sbuff1 >= (short) 32) && (sbuff1 <= (short) 126))) {
                            return false;
                        }
                    } else {
                        // No printable ASCII symbols. Only allow printable ASCII characters and
                        // numbers.
                        if (!(((sbuff1 == (short) 32) || // Spacing in decimal
                                ((sbuff1 >= (short) 48) && (sbuff1 <= (short) 57)) || // 0 to 9 in decimals
                                ((sbuff1 >= (short) 65) && (sbuff1 <= (short) 90)) || // A to Z in decimals
                                ((sbuff1 >= (short) 97) && (sbuff1 <= (short) 122))))) { // a to z in decimals
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    // Allowed values are spacing and 0 to 9.
    public short t104DisplayCheck(byte[] input, short offset, short len, byte[] displayBuff, short displayBuffOff,
            short sbuff, short sbuff1) {
        if ((input != null) && (len > (short) 0) && (len <= MAX_DISPLAY_DATA_LEN)) {
            for (sbuff = (short) 0; sbuff < len; sbuff++) {
                sbuff1 = (short) (input[(short) (offset + sbuff)] & 0xFF);
                if (!(((sbuff1 == (short) 32) || // Spacing in decimal
                        ((sbuff1 >= (short) 48) && (sbuff1 <= (short) 57))// 0 to 9 in decimals
                ))) {
                    return -1;
                }
            }

            // Format before rendering to display in right-aligned order
            Util.arrayFillNonAtomic(displayBuff, displayBuffOff, (short) 8, (byte) 0x20);
            Util.arrayCopyNonAtomic(input, offset, displayBuff, (short) (displayBuffOff + 8 - len), len);

            // Render to display
            boolean isSuccess = false;
            isSuccess = state.DispDigit(displayBuff, displayBuffOff, (short) 8);
            if (isSuccess) {
                return 1;
            } else {
                return -2;
            }
        } else if (input == null && len == 0) {
            // Clear screen
            state.clearScreen();
        }
        return -1;
    }

    // Allowed values are full-stop, spacing and 0 to 9. Only one decimal allowed
    public short t104WalletValueCheckAndFormat(byte displayWalletRecordSlot, byte[] balance, short balanceOff,
            short balanceLen, byte[] payment, short paymentOff, short paymentLen, byte[] loading, short loadingOff,
            short loadingLen, byte[] displayBuff, short displayBuffOff, short sbuff, short sbuff1, short sbuff2) {
        boolean hasBalanceFullStop = false;
        boolean hasPaymentFullStop = false;
        boolean hasLoadingFullStop = false;

        // Check target type
        if (!(displayWalletRecordSlot == T104OpenAPI.WALLET_BALANCE_RECORD_SLOT
                || displayWalletRecordSlot == T104OpenAPI.WALLET_PAYMENT_RECORD_SLOT
                || displayWalletRecordSlot == T104OpenAPI.WALLET_LOADING_RECORD_SLOT)) {
            return -2;
        }

        // Parse input for balance
        if ((balance != null) && (balanceLen > (short) 0) && (balanceLen <= MAX_WALLET_DATA_LEN)) {
            for (sbuff = (short) 0; sbuff < balanceLen; sbuff++) {
                sbuff1 = (short) (balance[(short) (balanceOff + sbuff)] & 0xFF);
                if (!((sbuff1 == (short) 46) || // Full stop in decimal
                        ((sbuff1 >= (short) 48) && (sbuff1 <= (short) 57))// 0 to 9 in decimals
                )) {
                    return -3;
                }

                // Found full-stop
                if (sbuff1 == (short) 46) {
                    // Full-stop was not previously found
                    if (!hasBalanceFullStop) {
                        // Check that it is at a len - 2 location
                        if ((balanceLen >= 3) && (sbuff == (short) (balanceLen - 3))) {
                            hasBalanceFullStop = true;
                        } else {
                            return -4;
                        }
                    } else {
                        return -5;
                    }
                }
            }

            // Check if the input has no decimals, it has to be 8 digits long only as the
            // decimal is off
            if (!hasBalanceFullStop && balanceLen > 8) {
                return -6;
            }
        }

        // Parse input for payment
        if ((payment != null) && (paymentLen > (short) 0) && (paymentLen <= MAX_WALLET_DATA_LEN)) {
            for (sbuff = (short) 0; sbuff < paymentLen; sbuff++) {
                sbuff1 = (short) (payment[(short) (paymentOff + sbuff)] & 0xFF);
                if (!((sbuff1 == (short) 46) || // Full stop in decimal
                        ((sbuff1 >= (short) 48) && (sbuff1 <= (short) 57))// 0 to 9 in decimals
                )) {
                    return -7;
                }

                // Found full-stop
                if (sbuff1 == (short) 46) {
                    // Full-stop was not previously found
                    if (!hasPaymentFullStop) {
                        // Check that it is at a len - 2 location
                        if ((paymentLen >= 3) && (sbuff == (short) (paymentLen - 3))) {
                            hasPaymentFullStop = true;
                        } else {
                            return -8;
                        }
                    } else {
                        return -9;
                    }
                }
            }

            // Check if the input has no decimals, it has to be 8 digits long only as the
            // decimal is off
            if (!hasPaymentFullStop && paymentLen > 8) {
                return -11;
            }
        }

        // Parse input for loading
        if ((loading != null) && (loadingLen > (short) 0) && (loadingLen <= MAX_WALLET_DATA_LEN)) {
            for (sbuff = (short) 0; sbuff < loadingLen; sbuff++) {
                sbuff1 = (short) (loading[(short) (loadingOff + sbuff)] & 0xFF);
                if (!((sbuff1 == (short) 46) || // Full stop in decimal
                        ((sbuff1 >= (short) 48) && (sbuff1 <= (short) 57))// 0 to 9 in decimals
                )) {
                    return -12;
                }

                // Found full-stop
                if (sbuff1 == (short) 46) {
                    // Full-stop was not previously found
                    if (!hasLoadingFullStop) {
                        // Check that it is at a len - 2 location
                        if ((loadingLen >= 3) && (sbuff == (short) (loadingLen - 3))) {
                            hasLoadingFullStop = true;
                        } else {
                            return -13;
                        }
                    } else {
                        return -14;
                    }
                }
            }

            // Check if the input has no decimals, it has to be 8 digits long only as the
            // decimal is off
            if (!hasLoadingFullStop && loadingLen > 8) {
                return -16;
            }
        }

        // Prepare displayBuff
        Util.arrayFillNonAtomic(displayBuff, displayBuffOff, (short) (MAX_WALLET_DATA_LEN * 3), (byte) 0x20);

        // Prepare type and length
        byte type = (byte) (0x00 ^ displayWalletRecordSlot);

        // Set as temporary total length counter
        sbuff2 = 0;

        if (balance != null && balanceLen > 0) {
            // Update type
            type ^= EWALLET_UPDATE_BALANCE;

            // Begin formatting for display
            sbuff1 = 0;
            for (sbuff = (short) (balanceLen - 1); sbuff >= 0; sbuff--) {
                if (sbuff != (short) (balanceLen - 3)) {
                    displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                            - sbuff1)] = balance[(short) (balanceOff + sbuff)];
                    sbuff1++;
                } else {
                    if (hasBalanceFullStop) {
                        displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                                - sbuff1)] = balance[(short) (balanceOff + sbuff)];

                        sbuff1++;
                    } else {
                        // Skip whitespace
                        sbuff1++;

                        // Copy in the full-stop char
                        displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                                - sbuff1)] = balance[(short) (balanceOff + sbuff)];
                        sbuff1++;
                    }
                }
            }

            // Update total length
            sbuff2 += MAX_WALLET_DATA_LEN;
        }

        if (payment != null && paymentLen > 0) {
            type ^= EWALLET_UPDATE_PAYMENT;

            // Begin formatting for display
            sbuff1 = 0;
            for (sbuff = (short) (paymentLen - 1); sbuff >= 0; sbuff--) {
                if (sbuff != (short) (paymentLen - 3)) {
                    displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                            - sbuff1)] = payment[(short) (paymentOff + sbuff)];
                    sbuff1++;
                } else {
                    if (hasPaymentFullStop) {
                        displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                                - sbuff1)] = payment[(short) (paymentOff + sbuff)];

                        sbuff1++;
                    } else {
                        // Skip whitespace
                        sbuff1++;

                        // Copy in the full-stop char
                        displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                                - sbuff1)] = payment[(short) (paymentOff + sbuff)];
                        sbuff1++;
                    }
                }
            }

            // Update total length
            sbuff2 += MAX_WALLET_DATA_LEN;
        }

        if (loading != null && loadingLen > 0) {
            type ^= EWALLET_UPDATE_LOADING;

            // Begin formatting for display
            sbuff1 = 0;
            for (sbuff = (short) (loadingLen - 1); sbuff >= 0; sbuff--) {
                if (sbuff != (short) (loadingLen - 3)) {
                    displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                            - sbuff1)] = loading[(short) (loadingOff + sbuff)];
                    sbuff1++;
                } else {
                    if (hasLoadingFullStop) {
                        displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                                - sbuff1)] = loading[(short) (loadingOff + sbuff)];

                        sbuff1++;
                    } else {
                        // Skip whitespace
                        sbuff1++;

                        // Copy in the full-stop char
                        displayBuff[(short) (sbuff2 + displayBuffOff + MAX_WALLET_DATA_LEN - 1
                                - sbuff1)] = loading[(short) (loadingOff + sbuff)];
                        sbuff1++;
                    }
                }
            }

            // Update total length
            sbuff2 += MAX_WALLET_DATA_LEN;
        }

        // Attempt to display to screen
        if (state.UpdateDisp(type, displayBuff, displayBuffOff, sbuff2)) {
            // Wipe target data slot and repopulate if display is successful
            JCSystem.beginTransaction();
            if (balance != null && balanceLen > 0) {
                Util.arrayFillNonAtomic(walletBalanceData, (short) 0, (short) walletBalanceData.length, (byte) 0x20);
                walletBalanceDataLen = 0;
                Util.arrayCopyNonAtomic(balance, balanceOff, walletBalanceData, (short) 0, balanceLen);
                walletBalanceDataLen = balanceLen;
            }
            if (payment != null && paymentLen > 0) {
                Util.arrayFillNonAtomic(walletPayData, (short) 0, (short) walletPayData.length, (byte) 0x20);
                walletPayDataLen = 0;
                Util.arrayCopyNonAtomic(payment, paymentOff, walletPayData, (short) 0, paymentLen);
                walletPayDataLen = paymentLen;
            }
            if (loading != null && loadingLen > 0) {
                Util.arrayFillNonAtomic(walletLoadData, (short) 0, (short) walletLoadData.length, (byte) 0x20);
                walletLoadDataLen = 0;
                Util.arrayCopyNonAtomic(loading, loadingOff, walletLoadData, (short) 0, loadingLen);
                walletLoadDataLen = loadingLen;
            }
            JCSystem.commitTransaction();
            return 1;
        } else {
            return -1;
        }
    }

    public static boolean checkSecType(byte secType) {
        if (secType == CRED_AUTHTYPE_PWD || secType == CRED_AUTHTYPE_PIN || secType == CRED_AUTHTYPE_FP
                || secType == CRED_AUTHTYPE_ASYMMKEY) {
            return true;
        }
        return false;
    }

    public static short allocateNewPINPWD(short sbuff) {
        for (sbuff = 0; sbuff < credPINs.length; sbuff++) {
            if (credPINs[sbuff] == null) {
                return sbuff;
            }
        }
        return -1;
    }

    public static byte getCallerAID(byte[] output, short offset) {
        if (JCSystem.getPreviousContextAID() != null) {
            return ((AID) JCSystem.getPreviousContextAID()).getBytes(output, offset);
        } else {
            return JCSystem.getAID().getBytes(output, offset);
        }
    }

    public static boolean newAOCParamsCheck(byte secretType, byte[] secret, short secOffset, short secLen,
            short maxRetry, byte management, short sbuff, short sbuff1) {
        if ((secretEnrollmentCheck(secretType, secret, secOffset, secLen, sbuff, sbuff1))
                && ((maxRetry > (short) 0) && (maxRetry <= (short) 99)) && (management == NULL)) {
            return true;
        }
        return false;
    }

    public static short topLevelCredentialLookup(byte lookupState, byte[] param, short pOffset, short pLength,
            byte[] buff, short buffOffset, short sbuff, short sbuff1) {
        switch (lookupState) {
            case LOOKUP_AVAILABLE_AOC:
                for (sbuff = 0; sbuff < (short) aocCreds.length; sbuff++) {
                    if (aocCreds[sbuff] != null) {
                        // Compare AID
                        sbuff1 = aocCreds[sbuff].getCredName(buff, buffOffset);
                        if (sbuff1 == pLength) {
                            if (Util.arrayCompare(buff, buffOffset, param, pOffset, pLength) == (byte) 0x00) {
                                return sbuff;
                            }
                        }
                    }
                }
                break;
            case LOOKUP_HAS_FREE_AOC:
                for (sbuff = 0; sbuff < (short) aocCreds.length; sbuff++) {
                    if (aocCreds[sbuff] == null) {
                        return sbuff;
                    }
                }
                break;
            default:
                break;
        }

        return (short) -1;
    }

    public static boolean createTopLevelContainer(byte secretType, byte[] secret, short secretOffset, short secretLen,
            short maxRetry, byte[] buff, short buffOffset, short sbuff, short sbuff1, short sbuff2) {

        if (newAOCParamsCheck(secretType, secret, secretOffset, secretLen, maxRetry, NULL, sb[4], sb[5])) {
            // Params input check is OK

            // Check if existing AOC container already exists via AID
            buff[buffOffset] = getCallerAID(buff, (short) (buffOffset + 1));
            sbuff = (short) (buff[buffOffset] & 0xFF); // Length of AID obtained

            // No existing AID found thus no possible overlaps
            if (topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, buff, (short) (buffOffset + 1), sbuff, buff,
                    (short) (buffOffset + 1 + sbuff), sbuff1, sbuff2) == (short) -1) {

                // Check if any available AOC slots left for use
                sbuff1 = topLevelCredentialLookup(LOOKUP_HAS_FREE_AOC, null, (short) 0, (short) 0, null, (short) 0,
                        sbuff, sbuff1);

                if (sbuff1 != (short) -1) {
                    JCSystem.beginTransaction();
                    aocCreds[sbuff1] = new CredentialObject((byte) 0x00, true, secretType, maxRetry, buff,
                            (short) (buffOffset + 1), sbuff, secret, secretOffset, secretLen, sbuff2, buff,
                            (short) (buffOffset + 1 + sbuff));
                    notVirgin();
                    JCSystem.commitTransaction();
                    return true;
                }
            }
        }
        return false;
    }

    // fn: 01. Login for AOC containers over Shareable Interface
    public short appLogin(byte[] param, short paramOffset, short paramLen) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);

        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            if (param == null) {
                // Reset session state to None but does not clear retry counter to prevent abuse
                // of retry counter
                c0[sb[0]] = AOCS_NONE;
            } else {
                // Check login state
                if (c0[sb[0]] == AOCS_NONE || c0[sb[0]] == AOCS_READY) {
                    // Check if anymore retries left in the AOC container credential
                    if (credPINs[sb[0]].getTriesRemaining() <= 0) {
                        // No more retries for login, reject.
                        return 0;
                    }

                    // If a ready state session reauthenticates
                    if (c0[sb[0]] == AOCS_READY) {
                        // Reset authentication status to none
                        c0[sb[0]] = AOCS_NONE;

                        // Reset the validation and retry counter
                        credPINs[sb[0]].reset();
                    }

                    // Generate random ticket into tickets array
                    sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);
                    rand.generateData(tickets, sb[1], AUTH_TICKET_LEN);

                    // Copy ticket for applet to use PIN/PWD to HMAC-SHA256 sign the ticket
                    Util.arrayCopyNonAtomic(tickets, sb[1], param, paramOffset, AUTH_TICKET_LEN);

                    // Set to login state to login begin state
                    c0[sb[0]] = AOCS_LOGIN_BEGIN;

                    // Return length of ticket
                    return AUTH_TICKET_LEN;
                } else if (c0[sb[0]] == AOCS_LOGIN_BEGIN) {
                    // Read the HMAC-SHA256 signature and check if valid
                    sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);
                    if (aocCreds[sb[0]].verifyAuth(tickets, sb[1], AUTH_TICKET_LEN, param, paramOffset, paramLen, b0,
                            (short) 0, sb[2], sb[3])) {
                        // If valid, set login state to login ready state
                        c0[sb[0]] = AOCS_READY;

                        // Return 1 as successful authentication
                        return 1;
                    } else {
                        // Else set to login none state
                        c0[sb[0]] = AOCS_NONE;

                        // Return 0 as failed authentication
                        return 0;
                    }
                }
            }
        }
        return -1;
    }

    // fn: 02. For creating AOC containers. FP management disabled by default and
    // requires enabling manually.
    public boolean createAOCContainer(byte secretType, byte[] secret, short secretOffset, short secretLen,
            short maxRetry) {
        return createTopLevelContainer(secretType, secret, secretOffset, secretLen, maxRetry, b0, (short) 0, sb[0],
                sb[1], sb[2]);
    }

    // fn: 03. Destroy container.
    public boolean destroyAOCContainer() {
        b0[(short) 0] = getCallerAID(b0, (short) 1);

        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            JCSystem.beginTransaction();

            // Wipe security information
            aocCreds[sb[0]].manageSec(T104OpenAPI.ACT_USR_DELETE, (byte) 0x00, (short) 0, null, (short) 0, (short) 0,
                    sb[1], sb[2], null, (short) 0);

            // Wipe container name
            aocCreds[sb[0]].setCredName(null, (short) 0, (short) 0);

            // Wipe possibly existing ticket data
            sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);
            Util.arrayFillNonAtomic(this.tickets, sb[1], AUTH_TICKET_LEN, (byte) 0x00);

            // Reset container login state
            c0[sb[0]] = AOCS_NONE;

            // Deallocate cred
            aocCreds[sb[0]] = null;
            JCSystem.commitTransaction();

            return true;
        }

        return false;
    }

    // fn: 04. Only AOC container management.
    public boolean manageAOCContainer(byte fieldType, byte[] input, short offset, short len, short maxRetry,
            byte[] ticket, short ticketOff) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);
        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            // Authenticate container's ticket
            // Calculate offset into tickets array location of cached container login
            // tickets
            sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);

            // Check sent request ticket is correct and the login status is ready state
            if ((Util.arrayCompare(ticket, ticketOff, tickets, sb[1], AUTH_TICKET_LEN) == (byte) 0x00)
                    && (c0[sb[0]] == AOCS_READY)) {
                // Execute action
                return manageAOCContainerByField(sb[0], fieldType, input, offset, len, maxRetry, b0, (short) 0, sb[1],
                        sb[2], sb[3]);
            }
        }
        return false;
    }

    public boolean manageAOCContainerByField(short aocPos, byte fieldType, byte[] input, short offset, short len,
            short maxRetry, byte[] buff, short buffOffset, short sbuff, short sbuff1, short sbuff2) {
        switch (fieldType) {
            case CRED_FIELD_SECRET:
                if (newAOCParamsCheck(aocCreds[aocPos].getSecType(), input, offset, len, maxRetry, NULL, sbuff,
                        sbuff1)) {
                    if (aocCreds[aocPos].manageSec(T104OpenAPI.ACT_USR_UPDATE, aocCreds[aocPos].getSecType(), maxRetry,
                            input, offset, len, sbuff, sbuff1, buff, buffOffset) == 1) {
                        return true;
                    }
                }
        }
        return false;
    }

    // fn: 05. Get public container information.
    public short getAOCInfo(byte[] output, short outOffset) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);
        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            output[outOffset] = (byte) 0x01; // has registered
            output[(short) (outOffset + 1)] = aocCreds[sb[0]].getSecType(); // Security auth type
            output[(short) (outOffset + 2)] = credPINs[sb[0]].getTriesRemaining(); // login tries remaining
            output[(short) (outOffset + 3)] = (byte) (aocCreds[sb[0]].getMaxRetry() & 0xFF); // login max retries
            output[(short) (outOffset + 4)] = MathUtil.byteBool(credPINs[sb[0]].isValidated()); // is logged in
            sb[1] = aocCreds[sb[0]].getCredName(output, (short) (outOffset + 6)); // Get AID
            output[(short) (outOffset + 5)] = (byte) (sb[1] & 0xFF); // Get AID length
            return (short) (sb[1] + 6);
        } else {
            output[outOffset] = (byte) 0x00; // has not registered
            return 1;
        }
    }

    // fn: 06. Get card information.
    public short getCardInfo(byte[] output, short outOffset) {
        // Card type

        // Card version

        // Card serial number

        // Card public key

        // Card operational state

        // Card usable RAM

        // Card usable Persistent Memory

        // Attestation signature ???

        return 0;
    }

    // Convert to TLV version. Version code is not TLV-ed but the rest are.
    public short getHWInfo(byte[] buff, short bOff, byte[] output, short off, short[] sbuff, short sbuffOff) {
        sbuff[sbuffOff] = off;

        // Set version
        Util.setShort(output, sbuff[sbuffOff], VERSION);
        sbuff[sbuffOff] += 2;

        // Set capability tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_CAP;
        sbuff[sbuffOff]++;

        // Set capability length - 2
        output[sbuff[sbuffOff]] = (byte) 0x02;
        sbuff[sbuffOff]++;

        // Set capability bits value
        Util.setShort(output, sbuff[sbuffOff], CAPABILITY);
        sbuff[sbuffOff] += 2;

        // Set user type tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_USR;
        sbuff[sbuffOff]++;

        // Set user type length
        output[sbuff[sbuffOff]] = (byte) 0x01;
        sbuff[sbuffOff]++;

        // Set user type value
        output[sbuff[sbuffOff]] = USER_ADMIN;
        sbuff[sbuffOff]++;

        // <--- Recursively Begin ---> //
        // Set SCP type tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_SCP;
        sbuff[sbuffOff]++;

        // Set SCP type length
        output[sbuff[sbuffOff]] = (byte) 0x02;
        sbuff[sbuffOff]++;

        // Set SCP type value
        Util.setShort(output, sbuff[sbuffOff], SCP_TT_TP_A03);
        sbuff[sbuffOff] += 2;

        // Set backup type tag to no backup
        output[sbuff[sbuffOff]] = TLV_TAG_HW_BK;
        sbuff[sbuffOff]++;

        // Set backup type length
        output[sbuff[sbuffOff]] = (byte) 0x00;
        sbuff[sbuffOff]++;

        // <--- Recursively End ---> //

        // Set interactive capabilities tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_INTERACT;
        sbuff[sbuffOff]++;

        // Set interactive capabilities length
        output[sbuff[sbuffOff]] = (byte) 0x01;
        sbuff[sbuffOff]++;

        // Set interactive capabilities value
        output[sbuff[sbuffOff]] = INTERACT_SCREEN;
        sbuff[sbuffOff]++;

        // Set credential format tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_CRED_FORMAT;
        sbuff[sbuffOff]++;

        // Set credential format length
        output[sbuff[sbuffOff]] = (byte) 0x01;
        sbuff[sbuffOff]++;

        // Set credential format value
        output[sbuff[sbuffOff]] = CRED_AUTHTYPE_PIN;
        sbuff[sbuffOff]++;

        // Set persistent memory tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_MEM_PERSIST;
        sbuff[sbuffOff]++;

        // Set persistent memory length
        output[sbuff[sbuffOff]] = (byte) 0x04;
        sbuff[sbuffOff]++;

        // Set persistent memory size value
        JCSystem.getAvailableMemory(sbuff, (short) (sbuffOff + 1), JCSystem.MEMORY_TYPE_PERSISTENT);
        Util.setShort(output, sbuff[sbuffOff], sbuff[(short) (sbuffOff + 1)]);
        Util.setShort(output, (short) (sbuff[sbuffOff] + 2), sbuff[(short) (sbuffOff + 2)]);
        sbuff[sbuffOff] += 4;

        // Set transient reset memory tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_MEM_TEMP_RST;
        sbuff[sbuffOff]++;

        // Set transient reset memory length
        output[sbuff[sbuffOff]] = (byte) 0x04;
        sbuff[sbuffOff]++;

        // Set transient reset memory size value
        JCSystem.getAvailableMemory(sbuff, (short) (sbuffOff + 1), JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
        Util.setShort(output, sbuff[sbuffOff], sbuff[(short) (sbuffOff + 1)]);
        Util.setShort(output, (short) (sbuff[sbuffOff] + 2), sbuff[(short) (sbuffOff + 2)]);
        sbuff[sbuffOff] += 4;

        // Set transient deselect memory tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_MEM_TEMP_DST;
        sbuff[sbuffOff]++;

        // Set transient deselect memory length
        output[sbuff[sbuffOff]] = (byte) 0x04;
        sbuff[sbuffOff]++;

        // Set transient deselect memory size value
        JCSystem.getAvailableMemory(sbuff, (short) (sbuffOff + 1), JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        Util.setShort(output, sbuff[sbuffOff], sbuff[(short) (sbuffOff + 1)]);
        Util.setShort(output, (short) (sbuff[sbuffOff] + 2), sbuff[(short) (sbuffOff + 2)]);
        sbuff[sbuffOff] += 4;

        // Set HW ID tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_ID;
        sbuff[sbuffOff]++;

        // Cache HW ID length location and skip
        sbuff[(short) (sbuffOff + 1)] = sbuff[sbuffOff];
        sbuff[sbuffOff]++;

        // Set HW ID Public Key tag
        output[sbuff[sbuffOff]] = TLV_TAG_HW_ID_PUB;
        sbuff[sbuffOff]++;

        // Skip HW ID Public Key length
        sbuff[sbuffOff]++;

        // Set HW public key as X || Y only value
        sbuff[(short) (sbuffOff + 2)] = ((ECPublicKey) kmKP.getPublic()).getW(buff, bOff);
        sbuff[(short) (sbuffOff + 2)]--;
        Util.arrayCopyNonAtomic(buff, (short) (bOff + 1), output, sbuff[sbuffOff], sbuff[(short) (sbuffOff + 2)]);

        // Go back and set HW ID Public Key Length
        output[(short) (sbuff[sbuffOff] - 1)] = (byte) (sbuff[(short) (sbuffOff + 2)] & 0xFF);

        // Increment forward the public key length
        sbuff[sbuffOff] += sbuff[(short) (sbuffOff + 2)];

        // Go back and set HW ID overall length
        output[sbuff[(short) (sbuffOff + 1)]] = (byte) ((short) (sbuff[(short) (sbuffOff + 2)] + 2) & 0xFF);

        return sbuff[sbuffOff];
    }

    public void displayToScreen(byte[] input, short off, short len, byte[] ticket, short ticketOff) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);
        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            // Authenticate container's ticket
            // Calculate offset into tickets array location of cached container login
            // tickets
            sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);

            // Check sent request ticket is correct and the login status is ready state
            if ((Util.arrayCompare(ticket, ticketOff, tickets, sb[1], AUTH_TICKET_LEN) == (byte) 0x00)
                    && (c0[sb[0]] == AOCS_READY)) {
                if (t104DisplayCheck(input, off, len, ticket, ticketOff, sb[0], sb[1]) != 1) {
                    ISOException.throwIt(SW_UI_ERR);
                }
            } else {
                ISOException.throwIt(SW_INVALID_USER_PIN);
            }
        } else {
            ISOException.throwIt(SW_INVALID_USER_ROLE);
        }
    }

    public short setGlobalWalletAmount(byte displayWalletRecordSlot, byte[] balance, short balanceOff, short balanceLen,
            byte[] payment, short paymentOff, short paymentLen, byte[] loading, short loadingOff, short loadingLen,
            byte[] ticket, short ticketOff) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);
        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            // Authenticate container's ticket
            // Calculate offset into tickets array location of cached container login
            // tickets
            sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);

            // Check sent request ticket is correct and the login status is ready state
            if ((Util.arrayCompare(ticket, ticketOff, tickets, sb[1], AUTH_TICKET_LEN) == (byte) 0x00)
                    && (c0[sb[0]] == AOCS_READY)) {
                // Check has E-Wallet access rights
                if (hasGlobalWalletAccessRights(sb[0], b0, (short) 0, sb[2])) {
                    return t104WalletValueCheckAndFormat(displayWalletRecordSlot, balance, balanceOff, balanceLen,
                            payment, paymentOff, paymentLen, loading, loadingOff, loadingLen, b0, (short) 0, sb[0],
                            sb[1], sb[2]);
                } else {
                    ISOException.throwIt(T104OpenAPI.SW_INVALID_USER_ACCESS_RIGHTS);
                }
            } else {
                ISOException.throwIt(T104OpenAPI.SW_INVALID_USER_ROLE);
            }
        }
        return -1;
    }

    public short getGlobalWalletAmount(byte type, byte[] output, short off, byte[] ticket, short ticketOff) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);
        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            // Authenticate container's ticket
            // Calculate offset into tickets array location of cached container login
            // tickets
            sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);

            // Check sent request ticket is correct and the login status is ready state
            if ((Util.arrayCompare(ticket, ticketOff, tickets, sb[1], AUTH_TICKET_LEN) == (byte) 0x00)
                    && (c0[sb[0]] == AOCS_READY)) {

                // Check has E-Wallet access rights
                if (hasGlobalWalletAccessRights(sb[0], b0, (short) 0, sb[2])) {
                    switch (type) {
                        case T104OpenAPI.WALLET_BALANCE_RECORD_SLOT:
                            Util.arrayCopyNonAtomic(walletBalanceData, (short) 0, output, off, walletBalanceDataLen);
                            return walletBalanceDataLen;
                        case T104OpenAPI.WALLET_PAYMENT_RECORD_SLOT:
                            Util.arrayCopyNonAtomic(walletPayData, (short) 0, output, off, walletPayDataLen);
                            return walletPayDataLen;
                        case T104OpenAPI.WALLET_LOADING_RECORD_SLOT:
                            Util.arrayCopyNonAtomic(walletLoadData, (short) 0, output, off, walletLoadDataLen);
                            return walletLoadDataLen;
                        default:
                            return -2;
                    }
                }
            }
        }
        return -1;
    }

    public short listAOCSlotsAvailability(byte[] output, short off) {
        for (short i = (short) 0; i < MAX_AOC; i++) {
            if (aocCreds[i] == null) {
                output[(short) (off + i)] = (byte) 0x00;
            } else if (aocCreds[i].isActive()) {
                output[(short) (off + i)] = (byte) 0x01;
            }
        }
        return MAX_AOC;
    }

    public short getAOCContainerPosByCredID(byte[] input, short inOff, short inLen, byte[] buff, short buffOff,
            short sbuff, short sbuff1) {
        sbuff = 0;
        sbuff1 = 0;
        for (; sbuff < aocCreds.length; sb[0]++) {
            if (aocCreds[sbuff] != null) {
                if (aocCreds[sbuff].isActive()) {
                    sbuff1 = aocCreds[sbuff].getCredName(buff, buffOff);
                    if (sbuff1 == inLen) {
                        if (Util.arrayCompare(input, inOff, buff, buffOff, inLen) == (byte) 0x00) {
                            return sbuff;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public short getAOCContainer(short pos, byte[] output, short outOff, short sbuff, short sbuff1, short sbuff2) {
        if (aocCreds[pos] != null) {
            if (aocCreds[pos].isActive()) {
                sbuff = outOff;
                sbuff1 = 0;
                sbuff2 = 0;

                // Set name tag
                output[sbuff] = TLV_TAG_AOC_CRED_ID;
                sbuff++;
                sbuff1++;

                // Set name length
                output[sbuff] = (byte) (aocCreds[pos].getCredName(null, (short) 0) & 0xFF);
                sbuff++;
                sbuff1++;

                // Set name value
                sbuff2 = aocCreds[pos].getCredName(output, sbuff);
                sbuff += sbuff2;
                sbuff1 += sbuff2;

                // Set auxdata tag
                output[sbuff] = TLV_TAG_AOC_AUXDATA;
                sbuff++;
                sbuff1++;

                // Set auxdata length
                output[sbuff] = (byte) (aocCreds[pos].getAuxData(null, sbuff) & 0xFF);
                sbuff++;
                sbuff1++;

                // Set auxdata value
                sbuff2 = aocCreds[pos].getAuxData(output, sbuff);
                sbuff += sbuff2;
                sbuff1 += sbuff2;

                return sbuff1;
            }
        }
        return -1;
    }

    public void cleanupContainers(short sbuff, short sbuff1, byte[] buff, short buffOff) {
        for (sbuff = 0; sbuff < aocCreds.length; sbuff++) {
            if (aocCreds[sbuff] != null) {
                sbuff1 = aocCreds[sbuff].getCredName(buff, buffOff);
                apiAID = JCSystem.lookupAID(buff, buffOff, (byte) (sbuff1 & 0xFF));
                if (apiAID == null) {
                    // Destroy AOC container
                    JCSystem.beginTransaction();
                    aocCreds[sbuff].destroy();
                    aocCreds[sbuff] = null;
                    JCSystem.commitTransaction();
                }
            }
        }
    }

    public boolean hasGlobalWalletAccessRights(short credPos, byte[] buff, short buffOff, short sbuff) {
        if (aocCreds[credPos] != null) {
            sbuff = aocCreds[credPos].getAuxData(buff, buffOff);
            if (sbuff == (short) 1) {
                if (buff[buffOff] == (byte) 0x31) {
                    return true;
                }
            }
        }
        return false;
    }

    public short changeAOCAdminPin(byte[] newPin, short off, short len) {
        if (globPIN.isValidated()) {
            if (newPin != null && len >= 6) {
                globPIN.update(newPin, (byte) (off & 0xFF), (byte) (len & 0xFF));
                globPIN.resetAndUnblock();
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    public boolean loginAOCAdminPin(byte[] pin, short off, short len) {
        logoutAll();
        if (globPIN.getTriesRemaining() != (byte) 0x00) {
            if (globPIN.check(pin, (byte) (off & 0xFF), (byte) (len & 0xFF))) {
                return true;
            }
        }
        return false;
    }

    public byte getTriesRemaining(byte target) {
        if (target == USER_ADMIN) {
            return globPIN.getTriesRemaining();
        }
        return (byte) 0xFF;
    }

    public byte whoami() {
        return l0[(short) 0];
    }

    public void logoutAll() {
        l0[(short) 0] = (byte) 0x00; // reset login user id.
        globPIN.reset();
    }

    public short setCardTimeout(short timeout, byte[] ticket, short ticketOff) {
        b0[(short) 0] = getCallerAID(b0, (short) 1);
        // Search for valid AOC container corresponding to AID
        sb[3] = (short) (b0[(short) 0] & 0xFF);
        sb[0] = topLevelCredentialLookup(LOOKUP_AVAILABLE_AOC, b0, (short) 1, sb[3], b0, (short) (1 + sb[3]), sb[1],
                sb[2]);

        // Found valid AOC container
        if (sb[0] != -1) {
            // Authenticate container's ticket
            // Calculate offset into tickets array location of cached container login
            // tickets
            sb[1] = (short) (sb[0] * AUTH_TICKET_LEN);

            // Check sent request ticket is correct and the login status is ready state
            if ((Util.arrayCompare(ticket, ticketOff, tickets, sb[1], AUTH_TICKET_LEN) == (byte) 0x00)
                    && (c0[sb[0]] == AOCS_READY)) {
                if (state.setCardTimeout(timeout)) {
	                return 1;
                } else {
	                return 0;
                }
            }
        }
        
        return -1;
    }

    public short getCardTimeout() {
        if (state.getTimeoutStatus(b0, (short) 0)) {
            short timeout = Util.makeShort(b0[0], b0[1]);
            if (timeout == (short) 0xFFFF) {
                return 0;
            } else {
                return timeout;
            }
        }
        return -1;
    }

    public void secureChannel(APDU apdu, byte[] apduBuf, short len) {
        // ECDH based SChannel establishment with static-ephermeral construct.
        if (len == (short) 65) {
            // Reset buffers
            Util.arrayFillNonAtomic(b0, (short) 0, (short) b0.length, (byte) 0x00);
            Util.arrayFillNonAtomic(b2, (short) 0, (short) b2.length, (byte) 0x00);

            // Create IV by concatenating client public key || card public key, sha256 hash
            // and extract first 12 bytes
            sha256.reset();
            sha256.update(apduBuf, (short) 5, (short) 65);
            ((ECPublicKey) kmKP.getPublic()).getW(b0, (short) 0);
            sha256.doFinal(b0, (short) 0, (short) 65, b0, (short) 65);
            Util.arrayCopyNonAtomic(b0, (short) 65, b2, (short) 0, (short) 12);

            // ECDH
            ka.init((ECPrivateKey) kmKP.getPrivate());
            ka.generateSecret(apduBuf, (short) 5, (short) 65, b0, (short) 0);

            // Hash shared secret with SHA-256
            sha256.reset();
            sha256.doFinal(b0, (short) 0, (short) 32, b0, (short) 0);

            // Set as session key
            sessKey.setKey(b0, (short) 0);

            // Hash a second time to generate a one-time confirmation code
            sha256.reset();
            sha256.doFinal(b0, (short) 0, (short) 32, b0, (short) 0);

            // Generate on screen a one-time confirmation code
            short offset = (short) (b0[(short) 31] & 0xf);

            int binary = ((b0[(short) (offset)] & 0x7f) << 24) | ((b0[(short) (offset + 1)] & 0xff) << 16)
                    | ((b0[(short) (offset + 2)] & 0xff) << 8) | (b0[(short) (offset + 3)] & 0xff);

            // 6 digit OTP code modulus math
            MathUtil.intToBytes(binary % 1000000, b0, (short) 0);

            // Convert back to bytes
            sb[0] = MathUtil.toDecimalASCII(b0, (short) 0, (short) 4, apduBuf, (short) 0);
            Util.arrayCopyNonAtomic(apduBuf, (short) 4, b0, (short) 0, (short) 6);
            t104DisplayCheck(b0, (short) 0, (short) 6, b0, (short) 6, sb[0], sb[1]);
        } else {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
    }

    // securePacketProcess(Cipher.MODE_ENCRYPT, apduBuf, (short) 0, (short) 2, data,
    // off, len, sbuff, apduBuf, (short) 0);
    public short securePacketProcess(byte mode, byte[] params, short pOff, short pLen, byte[] input, short inOff,
            short inLen, short sbuff, byte[] apiBuffer, short apiBuffOutOff) {
        if (sessKey.isInitialized() && (Util.makeShort(b2[12], b2[13]) < 65535)
                && (Util.makeShort(b2[14], b2[15]) < 65535)) {
            if (mode == Cipher.MODE_ENCRYPT && inLen <= (short) 200) {
                // Derive packet nonce as IV by SHA256 hashing the concatenation of
                // <ClientNonce><ClientCtr><DeviceCtr>
                sha256.reset();
                sha256.doFinal(b2, (short) 0, (short) b2.length, apiBuffer, apiBuffOutOff);

                // Encrypt data without including IV into ciphertext output
                if (input != null && inLen > 0) {
                    cipher.init(sessKey, mode, apiBuffer, apiBuffOutOff, (short) 16);
                    // apiBuffer :: <IV - 16><ciphertext - sbuff>
                    sbuff = cipher.doFinal(input, inOff, inLen, apiBuffer, (short) (apiBuffOutOff + 16));
                } else {
                    sbuff = 0;
                }

                // SHA256(<params><IV><Ciphertext>) with hash result used for MACing
                sha256.reset();
                sha256.update(params, pOff, pLen);
                // apiBuffer :: <IV - 16><Ciphertext - sbuff><Hash - 32>
                sha256.doFinal(apiBuffer, apiBuffOutOff, (short) (16 + sbuff), apiBuffer,
                        (short) (apiBuffOutOff + 16 + sbuff));

                // Move apiBuffer ciphertext and hash data to input buffer while omitting iv as
                // it is not needed anymore
                // inputBuff :: <ciphertext - sbuff><hash - 32>
                Util.arrayCopyNonAtomic(apiBuffer, (short) (apiBuffOutOff + 16), input, inOff, (short) (32 + sbuff));

                // Get key
                // apiBuffer :: <sessKey - 32>
                sessKey.getKey(apiBuffer, apiBuffOutOff);

                // Copy hash from input buffer to apiBuffer
                // apiBuffer :: <sessKey - 32><hash - 32>
                Util.arrayCopyNonAtomic(input, (short) (inOff + sbuff), apiBuffer, (short) (apiBuffOutOff + 32),
                        (short) 32);

                // Process HMAC-SHA256(<sessKey - 32>, <hash - 32>)
                HMACSHA.process(sha256, apiBuffer, apiBuffOutOff, (short) 32, apiBuffer, (short) (apiBuffOutOff + 32),
                        (short) 32, apiBuffer, (short) (apiBuffOutOff + 64), apiBuffer, (short) (apiBuffOutOff + 128),
                        apiBuffer, (short) (apiBuffOutOff + 192), apiBuffer, apiBuffOutOff);

                // apiBuffer :: <mac - 32>

                // Copy mac to input buffer to replace hash
                // inputBuff :: <ciphertext - sbuff><mac - 32>
                Util.arrayCopyNonAtomic(apiBuffer, apiBuffOutOff, input, (short) (inOff + sbuff), (short) 32);

                // Copy both ciphertext and mac to apiBuffer from inputBuff
                // apiBuffer :: <ciphertext - sbuff><mac - 32>
                Util.arrayCopyNonAtomic(input, inOff, apiBuffer, apiBuffOutOff, (short) (32 + sbuff));

                // Update device counter
                MathUtil.shortToBytes((short) (Util.makeShort(b2[14], b2[15]) + 1), b2, (short) 14);

                return (short) (sbuff + 32);
            } else if (mode == Cipher.MODE_DECRYPT && inLen <= (short) 240) {
                // Derive packet nonce as IV by SHA256 hashing the concatenation of
                // <ClientNonce><ClientCtr><DeviceCtr>
                sha256.reset();
                sha256.doFinal(b2, (short) 0, (short) b2.length, apiBuffer, (short) 0);

                // Compute HMAC-SHA256 using SHA256(<params><IV><Ciphertext>) with 16 initial
                // bytes of the hash result
                sha256.reset();
                sha256.update(params, pOff, pLen);
                sha256.update(apiBuffer, (short) 0, (short) 16);
                sha256.doFinal(input, inOff, (short) (inLen - 32), apiBuffer, (short) 32);
                sessKey.getKey(apiBuffer, (short) 0);
                HMACSHA.process(sha256, apiBuffer, (short) 0, (short) 32, apiBuffer, (short) 32, (short) 32, apiBuffer,
                        (short) 64, apiBuffer, (short) 128, apiBuffer, (short) 192, apiBuffer, (short) 192);
                if (Util.arrayCompare(input, (short) (inOff + inLen - 32), apiBuffer, (short) 192,
                        (short) 32) == (byte) 0x00) {
                    // Derive packet nonce as IV by SHA256 hashing the concatenation of
                    // <ClientNonce><ClientCtr><DeviceCtr> again
                    sha256.reset();
                    sha256.doFinal(b2, (short) 0, (short) b2.length, apiBuffer, (short) 0);

                    // Decrypt packet
                    cipher.init(sessKey, mode, apiBuffer, (short) 0, (short) 16);
                    sbuff = cipher.doFinal(input, inOff, (short) (inLen - 32), apiBuffer, apiBuffOutOff);

                    // Update client counter
                    MathUtil.shortToBytes((short) (Util.makeShort(b2[12], b2[13]) + 1), b2, (short) 12);
                    return sbuff;
                } else {
                    ISOException.throwIt(SW_SCHANNEL_ERROR);
                }
            } else {
                ISOException.throwIt(SW_SCHANNEL_ERROR);
            }
        } else {
            ISOException.throwIt(SW_SCHANNEL_REQ_INIT);
        }
        return -1;
    }

    public short setReceiveAPDU(boolean disableSCP, short len, byte[] apduBuf, byte[] buff, short buffOff,
            short sbuff) {
        if (!disableSCP) {
            // Copy entire APDU and header to buffer
            Util.arrayCopyNonAtomic(apduBuf, (short) 0, buff, buffOff, (short) (5 + len));

            // Decrypt incoming. use CLA, INS, P1, P2 as params.
            sbuff = securePacketProcess(Cipher.MODE_DECRYPT, buff, buffOff, (short) 4, buff, (short) (buffOff + 5), len,
                    sbuff, apduBuf, (short) 5);

            // Check if decryption have error
            if (sbuff != -1) {
                // Copy back APDU headers CLA, INS, P1, P2.
                Util.arrayCopyNonAtomic(buff, buffOff, apduBuf, (short) 0, (short) 4);

                // Set length into APDU headers
                apduBuf[4] = (byte) (sbuff & 0xFF);
            }

            // Return only the decrypted length or the decryption error status
            return sbuff;
        }
        return len;
    }

    public void setReturnAPDU(boolean disableSCP, byte[] data, short off, short len, byte[] apduBuf, short sbuff,
            short SW) {
        if (disableSCP) {
            Util.arrayCopyNonAtomic(data, off, apduBuf, (short) 0, len);
            appRet[0] = SW;
            appRet[1] = len;
        } else {
            // Params are set to SW to also protect the SW
            Util.setShort(apduBuf, (short) 256, SW);
            short outLen = securePacketProcess(Cipher.MODE_ENCRYPT, apduBuf, (short) 256, (short) 2, data, off, len,
                    sbuff, apduBuf, (short) 0);
            if (outLen == -1) {
                appRet[0] = SW_ERROR_PROTOCOL_CHANNEL;
                appRet[1] = 0;
            } else {
                appRet[0] = SW;
                appRet[1] = outLen;
            }
        }
    }

    public short checkHeaders(byte CLA, byte INS, byte P1) {
        if (!sessKey.isInitialized()) {
            // If SCP is not started
            // 1.) Allow plain read record on record 0x00
            // 2.) Allow plain internal auth call
            // 3.) Allow plain logout
            if (!((CLA == KM104.CLA && INS == INS_READ_RECORD && P1 == (byte) 0x00)
                    || (CLA == KM104.CLA && INS == INS_INTERNAL_AUTH) || (CLA == KM104.CLA && INS == INS_LOGOUT))) {
                return SW_SCHANNEL_REQ_INIT;
            }
        } else {
            // If SCP is started
            // 1.) Allow plain internal auth call
            // Everything else must be in SEC_CLA
            if (!((CLA == KM104.CLA && INS == INS_INTERNAL_AUTH) || CLA == SEC_CLA)) {
                return ISO7816.SW_CLA_NOT_SUPPORTED;
            }
        }

        return 1;
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            if (!isKMEnvSet) {
                initEnv();
            }
            return;
        }

        if (!isKMEnvSet) {
            ISOException.throwIt(ISO7816.SW_APPLET_SELECT_FAILED);
        }

        JCSystem.requestObjectDeletion();

        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();
        byte cla = buf[ISO7816.OFFSET_CLA];
        byte ins = buf[ISO7816.OFFSET_INS];
        byte p1 = buf[ISO7816.OFFSET_P1];
        byte p2 = buf[ISO7816.OFFSET_P2];
        boolean disableSCP = true;
        appRet[0] = 0;
        appRet[1] = 0;

        sb[0] = checkHeaders(cla, ins, p1);
        if (sb[0] != 1) {
            ISOException.throwIt(sb[0]);
            sb[0] = 0;
            return;
        }

        if (cla == CLA) {
            disableSCP = true;
        } else if (cla == SEC_CLA) {
            disableSCP = false;
        }

        // Automatic handling of incoming APDUs for plain and secure modes
        len = setReceiveAPDU(disableSCP, len, buf, b0, (short) 0, sb[0]);

        switch (ins) {
            case INS_READ_RECORD:
                if (p1 == (byte) 0x00) {
                    // Get HW Info
                    len = getHWInfo(buf, (short) 0, b0, (short) 0, sb, (short) 0);
                    setReturnAPDU(disableSCP, b0, (short) 0, len, buf, sb[0], ISO7816.SW_NO_ERROR);
                } else if (p1 == (byte) 0x01) {
                    // List AOC container slots
                    len = listAOCSlotsAvailability(b0, (short) 0);
                    setReturnAPDU(false, b0, (short) 0, len, buf, sb[0], ISO7816.SW_NO_ERROR);
                } else if (p1 == (byte) 0x02) {
                    // Get AOC container info by slot number
                    if (len != 1) {
                        setReturnAPDU(false, b0, (short) 0, len, buf, sb[0], ISO7816.SW_WRONG_LENGTH);
                    } else {
                        len = getAOCContainer((short) (buf[5] & 0xFF), b0, (short) 0, sb[0], sb[1], sb[2]);
                        setReturnAPDU(false, b0, (short) 0, len, buf, sb[0], ISO7816.SW_NO_ERROR);
                    }
                } else if (p1 == (byte) 0x04) {
                    // User tries remaining
                    p2 = buf[ISO7816.OFFSET_P2];
                    b0[0] = getTriesRemaining(p2);
                    if (b0[0] == (byte) 0xFF) {
                        setReturnAPDU(false, b0, (short) 0, (short) 1, buf, sb[0], SW_INVALID_USER_ROLE);
                    } else {
                        setReturnAPDU(false, b0, (short) 0, (short) 1, buf, sb[0], ISO7816.SW_NO_ERROR);
                    }
                } else if (p1 == (byte) 0x05) {
                	sb[0] = getCardTimeout();
                	if (sb[0] != -1) {
						Util.setShort(b0, (short) 0, sb[0]);
						setReturnAPDU(false, b0, (short) 0, (short) 2, buf, sb[0], ISO7816.SW_NO_ERROR);
                	} else {
	                	setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], SW_CARD_TIME_ERR);
                	}
                } else {
                    setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_INCORRECT_P1P2);
                }
                break;
            case INS_WRITE_RECORD:
                if (p1 == (byte) 0x00) {
                    // Update AOC records
                    if (globPIN.isValidated()) {
                        short aocAuxDataLen = 0;
                        short aocAuxDataOffset = 0;
                        short aocCredIDLen = 0;
                        short aocCredIDOffset = 0;
                        boolean hasError = false;
                        byte b;
                        byte c;
                        for (sb[0] = 5; sb[0] < (short) (len + 5);) {
                            // Read tag
                            b = buf[sb[0]];

                            // Read length
                            c = buf[(short) (1 + sb[0])];
                            if (b == TLV_TAG_AOC_CRED_ID) {
                                // Read len
                                aocCredIDLen = (short) (c & 0xFF);

                                // Read offset
                                aocCredIDOffset = (short) (2 + sb[0]);

                                // Fast forward
                                sb[0] += (short) (2 + aocCredIDLen);
                            } else if (b == TLV_TAG_AOC_AUXDATA) {
                                // Read len
                                aocAuxDataLen = (short) (c & 0xFF);

                                // Read offset
                                aocAuxDataOffset = (short) (2 + sb[0]);

                                // Fast forward
                                sb[0] += (short) (2 + aocCredIDLen);
                            } else {
                                hasError = true;
                                break;
                            }

                        }

                        if (!hasError) {
                            if (aocAuxDataLen > 0 && aocCredIDLen > 0) {
                                sb[2] = getAOCContainerPosByCredID(buf, aocCredIDOffset, aocCredIDLen, b0, (short) 0,
                                        sb[0], sb[1]);
                                if (sb[2] != -1) {
                                    if ((aocAuxDataLen == 1) && ((buf[aocAuxDataOffset] == (byte) 0x30)
                                            || (buf[aocAuxDataOffset] == (byte) 0x31))) {
                                        aocCreds[sb[2]].setAuxData(buf, aocAuxDataOffset, aocAuxDataLen);
                                    } else {
                                        hasError = true;
                                    }
                                } else {
                                    hasError = true;
                                }
                            } else {
                                hasError = true;
                            }
                        }

                        if (hasError) {
                            setReturnAPDU(false, b0, (short) 0, (short) 1, buf, sb[0], ISO7816.SW_DATA_INVALID);
                        } else {
                            setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_NO_ERROR);
                        }
                    } else {
                        setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], SW_INVALID_USER_ROLE);
                    }
                } else if (p1 == (byte) 0x01) {
                    // Update AOC admin PIN
                    sb[0] = changeAOCAdminPin(buf, apdu.getOffsetCdata(), len);
                    if (sb[0] == 1) {
                        setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_NO_ERROR);
                    } else if (sb[0] == 0) {
                        setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                    } else {
                        setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], SW_INVALID_USER_ROLE);
                    }
                } else if (p1 == (byte) 0x02) {
                	if (len == 2) {
	                	if (state.setCardTimeout(Util.makeShort(buf[apdu.getOffsetCdata()], buf[(short) (apdu.getOffsetCdata() + 1)]))) {
		                	setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_NO_ERROR);
	                	} else {
		                	setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], SW_CARD_TIME_ERR);
	                	}
                	} else {
	                	setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_WRONG_LENGTH);
                	}
                } else {
                    setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_INCORRECT_P1P2);
                }
                break;
            case INS_ERASE_BINARY:
                // Call cleanup AOC records
                if (globPIN.isValidated()) {
                    cleanupContainers(sb[0], sb[1], b0, (short) 0);
                    setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_NO_ERROR);
                } else {
                    setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], SW_INVALID_USER_ROLE);
                }
                break;
            case INS_VERIFY_PIN:
                if (p1 == (byte) 0xFF) {
                    // Check who is logged in
                    b0[0] = whoami();
                    setReturnAPDU(false, b0, (short) 0, (short) 1, buf, sb[0], ISO7816.SW_NO_ERROR);
                } else {
                    if (len > 0) {
                        if (loginAOCAdminPin(buf, apdu.getOffsetCdata(), len)) {
                            sb[1] = ISO7816.SW_NO_ERROR;
                        } else {
                            sb[1] = ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED;
                        }
                    } else {
                        sb[1] = ISO7816.SW_WRONG_LENGTH;
                    }
                    setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], sb[1]);
                }
                break;
            case INS_INTERNAL_AUTH:
                secureChannel(apdu, buf, len);
                appRet[1] = 0;
                appRet[0] = ISO7816.SW_NO_ERROR;
                break;
            case INS_LOGOUT:
                logoutAll();
                setReturnAPDU(false, b0, (short) 0, (short) 0, buf, sb[0], ISO7816.SW_NO_ERROR);
                break;
            default:
                appRet[1] = 0;
                appRet[0] = ISO7816.SW_INS_NOT_SUPPORTED;
                break;
        }
        apdu.setOutgoingAndSend((short) 0, appRet[1]);
        ISOException.throwIt(appRet[0]);
    }
}