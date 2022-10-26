package S102WalletApplet;

import KM104.T104OpenAPI;
import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.MessageDigest;

public class S102WalletApplet extends Applet {

    private KM104.T104OpenAPI api = null;
    private AID apiAID = null;
    public static byte[] serverAID = new byte[] { (byte) 0x4B, (byte) 0x4D, (byte) 0x31, (byte) 0x30, (byte) 0x34,
            (byte) 0x00 };
    public static byte[] simulateOTPMessage = { (byte) 0x30, (byte) 0x38, (byte) 0x35, (byte) 0x30, (byte) 0x32,
            (byte) 0x36 }; // OTP to be displayed is '085026'.
    public static byte[] aocPIN = null;
    private static short maxRetry = 10;
    private static byte[] ticket = null;
    private static byte[] buff = null;
    private static byte[] buff1 = null;
    private static short[] sbuff = null;
    private static MessageDigest sha256 = null;

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // For HMAC-SHA256 pin authentication protocol
        sha256 = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);

        // Persistent storage for shareable interface authentication pin code
        aocPIN = new byte[8];

        // Need at least 256 to 258 computation buffer size for HMAC-SHA256 calculation
        // in buffer
        buff = JCSystem.makeTransientByteArray((short) 258, JCSystem.CLEAR_ON_RESET);

        // Need at least 30 computation buffer size for buffering incoming wallet
        // payload data
        buff1 = JCSystem.makeTransientByteArray((short) 30, JCSystem.CLEAR_ON_RESET);

        // Need at least 1 computation buffer size for general purpose transient short
        // calculation in buffer
        sbuff = JCSystem.makeTransientShortArray((short) 1, JCSystem.CLEAR_ON_RESET);

        // Session ticket buffer for authenticated API calls
        ticket = JCSystem.makeTransientByteArray(T104OpenAPI.AUTH_TICKET_LEN, JCSystem.CLEAR_ON_RESET);

        new S102WalletApplet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    private void initAPI(byte[] apduBuf, short apduBufOff) {
        if (api == null) {
            try {
                apiAID = JCSystem.lookupAID(serverAID, (short) 0, (byte) serverAID.length);
            } catch (Exception e) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }
            if (apiAID != null) {
                api = (KM104.T104OpenAPI) JCSystem.getAppletShareableInterfaceObject(apiAID, (byte) 0);

                if (api == null) {
                    ISOException.throwIt(ISO7816.SW_APPLET_SELECT_FAILED);
                }

                // Uninstall an applet in case it exists - Dummy methods for now
                unregisterAppletFromT104API();

                // Install the applet again - Dummy methods for now
                Util.arrayCopyNonAtomic(aocPIN, (short) 0, apduBuf, apduBufOff, (short) aocPIN.length);
                if (!registerAppletToT104API(apduBuf, (short) 0, (short) aocPIN.length, maxRetry)) {
                    // If registration fail, delete API access.
                    api = null;
                }
            } else {
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
            }
        }
    }

    // Dummy API registration by randomly selecting some bytes to be used as
    // Shareable Interface auth pin
    private boolean registerAppletToT104API(byte[] pin, short pinOff, short pinLen, short maxRetry) {
        if (api != null) {
            return api.createAOCContainer(T104OpenAPI.CRED_AUTHTYPE_PWD, pin, pinOff, pinLen, maxRetry);
        } else {
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
        }
        return false;
    }

    // Dummy API unregister
    private boolean unregisterAppletFromT104API() {
        if (api != null) {
            return api.destroyAOCContainer();
        } else {
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
        }
        return false;
    }

    // Requires applet relying on the shareable interface to use the stored
    // shareable pin to login and exchange for a session ticket. Session ticket is
    // then used as a session identifier to the API.
    //
    // Steps:
    // -- 1.) First call of the appLogin() API will return a random session ticket.
    // -- 2.) Store a copy of session ticket in a buffer (i.e. ticket buffer) with
    // ------ transient reset type memory.
    // -- 3.) Sign the random session ticket using your shareable applet PIN as the
    // HMAC Key.
    // -- 4.) Call appLogin() a second time with only the HMAC-SHA256 signature of
    // ------ the random session ticket as payload.
    // -- 5.) Return of 0x01 means success else not successful due to incorrect
    // ------ signature or some other reasons.
    private short appLogin(byte[] apduBuf, short apduBufOff, byte[] buff, short buffOff, short sbuff) {
        // Check if app is logged in
        Util.arrayFillNonAtomic(buff, buffOff, T104OpenAPI.AUTH_TICKET_LEN, (byte) 0x00);
        if (Util.arrayCompare(buff, buffOff, ticket, (short) 0, T104OpenAPI.AUTH_TICKET_LEN) == (byte) 0x00) {
            // AppLogin not called, now call it
            // Execute first appLogin call to obtain a random session ticket
            sbuff = api.appLogin(apduBuf, apduBufOff, T104OpenAPI.AUTH_TICKET_LEN);

            if (sbuff == T104OpenAPI.AUTH_TICKET_LEN) {
                // Check if session ticket is all zeroes, if yes, reject.
                Util.arrayFillNonAtomic(buff, buffOff, T104OpenAPI.AUTH_TICKET_LEN, (byte) 0x00);
                if (Util.arrayCompare(buff, buffOff, apduBuf, apduBufOff, T104OpenAPI.AUTH_TICKET_LEN) == (byte) 0x00) {
                    // Abnormal session ticket with all zeroes returned from API, not accepted.
                    ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                } else {
                    // Normal session ticket returned from API, proceed further.

                    // HMAC-SHA256 sign session ticket
                    HMACSHA.process(sha256, // hash
                            aocPIN, (short) 0, (short) aocPIN.length, // keybuff
                            apduBuf, apduBufOff, sbuff, // msgbuff
                            buff, buffOff, // ipad
                            buff, (short) (buffOff + (short) 64), // opad
                            buff, (short) (buffOff + (short) 128), // secBuff
                            buff, (short) (buffOff + (short) 192) // resultBuff
                    );

                    // Cache the session ticket for future use within a valid session window
                    Util.arrayCopyNonAtomic(apduBuf, apduBufOff, ticket, (short) 0, sbuff);

                    // Copy ticket signature into apduBuf to be sent via Shareable Interface API
                    Util.arrayCopyNonAtomic(buff, (short) (buffOff + (short) 192), apduBuf, apduBufOff, (short) 32);

                    // Execute second appLogin call with signed session ticket signature
                    sbuff = api.appLogin(apduBuf, apduBufOff, (short) 32);

                    // Second stage of appLogin, 1 is successful login, 0 is failed login.
                    if (sbuff != 1) {
                        // Failed authentication, clean ticket cache
                        Util.arrayFillNonAtomic(ticket, (short) 0, T104OpenAPI.AUTH_TICKET_LEN, (byte) 0x00);
                    }

                    return sbuff;
                }
            }
        } else {
            // Return success if already logged in
            return 1;
        }

        // Abnormality occurred.
        return -1;
    }

    // Display some numbers to screen
    private boolean displayToScreen(byte[] input, short off, short len, byte[] apduBuf, short apduBufOff, byte[] buff,
            short buffOff, short sbuff) {
        // Call appLogin to attempt applet login over shareable interface if not yet
        // logged in
        if (appLogin(apduBuf, apduBufOff, buff, buffOff, sbuff) == 1) {
            // Copy input into APDU buffer
            Util.arrayCopyNonAtomic(input, off, apduBuf, apduBufOff, len);

            // Copy session ticket into APDU buffer
            Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, (short) (apduBufOff + len),
                    T104OpenAPI.AUTH_TICKET_LEN);

            // Call API with input and session ticket
            api.displayToScreen(apduBuf, apduBufOff, len, apduBuf, (short) (apduBufOff + len));

            return true;
        }
        return false;
    }

    // Set some numbers to the T104 eWallet slots and select a wallet record slot to
    // display. Use the WALLET_BALANCE_RECORD_SLOT, WALLET_PAYMENT_RECORD_SLOT or
    // WALLET_LOADING_RECORD_SLOT record slot to display after updating the wallet
    // record(s).
    //
    // Wallet data for balance, payment and loading to be ASCIIfied integers or
    // decimals. One example is 123.45 has to be represented in bytes as
    // 0x3132332E3435.
    //
    // The wallet slots may either be integer only or decimal only.
    //
    // If integers only input, only up to 8 digit input is accepted.
    //
    // If decimal is used, only up to 2 decimal place with up to 6 integers is
    // accepted.
    private boolean setGlobalWalletAmount(byte displayWalletRecordSlot, byte[] balance, short balanceOff,
            short balanceLen, byte[] payment, short paymentOff, short paymentLen, byte[] loading, short loadingOff,
            short loadingLen, byte[] apduBuf, short apduBufOff, byte[] buff, short buffOff, short sbuff) {
        // Call appLogin to attempt applet login over shareable interface if not yet
        // logged in
        if (appLogin(apduBuf, apduBufOff, buff, buffOff, sbuff) == 1) {
            // Copy inputs into APDU buffer
            short off = apduBufOff;
            short balOff = off;
            short payOff = off;
            short loadOff = off;

            // Copy balance
            Util.arrayCopyNonAtomic(balance, balanceOff, apduBuf, off, balanceLen);
            off += balanceLen;

            // Copy payment
            Util.arrayCopyNonAtomic(payment, paymentOff, apduBuf, off, paymentLen);
            payOff = off;
            off += paymentLen;

            // Copy loading
            Util.arrayCopyNonAtomic(loading, loadingOff, apduBuf, off, loadingLen);
            loadOff = off;
            off += loadingLen;

            // Copy session ticket into APDU buffer
            Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, off, T104OpenAPI.AUTH_TICKET_LEN);
            off += T104OpenAPI.AUTH_TICKET_LEN;

            if (api.setGlobalWalletAmount(displayWalletRecordSlot, apduBuf, balOff, balanceLen, apduBuf, payOff,
                    paymentLen, apduBuf, loadOff, loadingLen, apduBuf, off) == 1) {
                // Successful setting of new global wallet slots and the screen should now
                // render to the selected display wallet slot
                return true;
            }
        }
        return false;
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] buf = apdu.getBuffer();
        switch (buf[ISO7816.OFFSET_INS]) {
            case (byte) 0xFF:
                initAPI(buf, (short) 0);
                break;
            case (byte) 0x00:
                try {
                    displayToScreen(simulateOTPMessage, (short) 0, (short) simulateOTPMessage.length, buf, (short) 0,
                            buff, (short) 0, sbuff[0]);
                } catch (SecurityException e) {
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                }
                break;
            case (byte) 0x01:
                // Expected APDU data:
                // -- length of balance record (1 byte)
                // -- length of paid funds record (1 byte)
                // -- length of loaded funds record (1 byte)
                // -- balance record (0 to 8/9 bytes depending on integer or decimal type)
                // -- paid funds record (0 to 8/9 bytes depending on integer or decimal type)
                // -- loaded funds record (0 to 8/9 bytes depending on integer or decimal type)
                short len = apdu.setIncomingAndReceive();

                // Buffer wallet information data in APDU to buff1
                Util.arrayCopyNonAtomic(buf, apdu.getOffsetCdata(), buff1, (short) 0, len);

                // Buffer off display selection
                byte selectedDisplayRecord = buf[ISO7816.OFFSET_P1];

                // Get length of balance slot data
                short balLen = (short) (buff1[0] & 0xFF);
                short balOff = 0;

                // Get length of payment slot data
                short payLen = (short) (buff1[1] & 0xFF);
                short payOff = (short) (balOff + balLen);

                // Get length of loading slot data
                short loadLen = (short) (buff1[2] & 0xFF);
                short loadOff = (short) (payOff + payLen);

                if (!setGlobalWalletAmount(selectedDisplayRecord, buff1, balOff, balLen, buff1, payOff, payLen, buff1,
                        loadOff, loadLen, buf, (short) 0, buff, (short) 0, sbuff[0])) {
                    ISOException.throwIt(ISO7816.SW_DATA_INVALID);
                }
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}