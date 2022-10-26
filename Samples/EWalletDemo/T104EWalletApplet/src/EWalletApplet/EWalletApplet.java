package EWalletApplet;

import KM104.T104OpenAPI;
import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.MessageDigest;
import javacard.security.RandomData;

public class EWalletApplet extends Applet {
    public static RandomData rand = null;
    public MessageDigest sha256 = null;
    private KM104.T104OpenAPI api = null;
    private AID apiAID = null;
    public static final byte[] serverAID = new byte[] { (byte) 0x4B, (byte) 0x4D, (byte) 0x31, (byte) 0x30, (byte) 0x34,
            (byte) 0x00 };
    public static byte[] aocPIN = null;
    public short maxRetry = 10;
    public static byte[] ticket = null;
    public static byte[] b0 = null;
    public static byte[] b1 = null;
    public static byte[] b2 = null;
    public static byte[] b3 = null;
    public static short[] s0 = null;
    public static boolean isReady = false;
    public static TxLog[] logs = null;
    public static final short MAX_ALLOWED_FUNDS = 1000;
    public static final short MAX_LOGS = 100;
    public static final short SW_INTERNAL_ERROR_APPLET_REG = (short) 0x6fa1;
    public static final short SW_FUNDS_TOO_MUCH = (short) 0x6f61;
    public static final short SW_FUNDS_TOO_LITTLE = (short) 0x6f62;
    public static final short SW_FUNDS_ENQUIRY_ERR = (short) 0x6f65;
    public static final short SW_FUNDS_UPDATE_ERR = (short) 0x6f66;
    public static final short SW_FUNDS_LOG_UPDATE_ERR = (short) 0x6f67;
    public static final short SW_EVENT_ERR = (short) 0x6f81;
    public static final short SW_INTERNAL_ERROR_MATH_ERR = (short) 0x6fb1;
    public static final byte[] DEFAULT_FUNDS = { (byte) 0x30 };
    public static final byte[] DEFAULT_LOG_INIT_MSG = { (byte) 0x4c, (byte) 0x6f, (byte) 0x67, (byte) 0x20, (byte) 0x68,
            (byte) 0x61, (byte) 0x73, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x65, (byte) 0x6e, (byte) 0x20,
            (byte) 0x63, (byte) 0x6c, (byte) 0x65, (byte) 0x61, (byte) 0x72, (byte) 0x65, (byte) 0x64, (byte) 0x2e };
    public static final byte[] DEFAULT_LOG_EWALLET_FUNDS_RESET_MSG = { (byte) 0x45, (byte) 0x57, (byte) 0x61,
            (byte) 0x6c, (byte) 0x6c, (byte) 0x65, (byte) 0x74, (byte) 0x20, (byte) 0x66, (byte) 0x75, (byte) 0x6e,
            (byte) 0x64, (byte) 0x73, (byte) 0x20, (byte) 0x72, (byte) 0x65, (byte) 0x73, (byte) 0x65, (byte) 0x74,
            (byte) 0x2e };
    public static final byte LOG_TYPE_GENERAL = (byte) 0x01;
    public static final byte LOG_TYPE_FUNDS = (byte) 0x02;
    public static final byte SYM_INC = (byte) (byte) 0x2B;
    public static final byte SYM_DEC = (byte) (byte) 0x2D;

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        rand = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        aocPIN = new byte[8];
        rand.generateData(aocPIN, (short) 0, (short) aocPIN.length);
        logs = new TxLog[MAX_LOGS];
        new EWalletApplet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    public void init() {
        if (!isReady) {
            b0 = JCSystem.makeTransientByteArray((short) 258, JCSystem.CLEAR_ON_RESET);
            b1 = JCSystem.makeTransientByteArray((short) 30, JCSystem.CLEAR_ON_RESET);
            b2 = JCSystem.makeTransientByteArray((short) 8, JCSystem.CLEAR_ON_RESET);
            b3 = JCSystem.makeTransientByteArray((short) 5, JCSystem.CLEAR_ON_RESET);
            s0 = JCSystem.makeTransientShortArray((short) 1, JCSystem.CLEAR_ON_RESET);
            ticket = JCSystem.makeTransientByteArray(T104OpenAPI.AUTH_TICKET_LEN, JCSystem.CLEAR_ON_RESET);
            sha256 = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
            JCSystem.beginTransaction();
            for (short i = 0; i < MAX_LOGS; i++) {
                logs[i] = new TxLog();
            }
            isReady = true;
            JCSystem.commitTransaction();
        }
    }

    public void uninstall() {
        unregisterAppletFromT104API();
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

                // Uninstall an applet in case it exists
                unregisterAppletFromT104API();

                // Install the applet again
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

    private boolean registerAppletToT104API(byte[] pin, short pinOff, short pinLen, short maxRetry) {
        if (api != null) {
            try {
                return api.createAOCContainer(T104OpenAPI.CRED_AUTHTYPE_PWD, pin, pinOff, pinLen, maxRetry);
            } catch (Exception e) {
                ISOException.throwIt(SW_INTERNAL_ERROR_APPLET_REG);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
        }
        return false;
    }

    private boolean unregisterAppletFromT104API() {
        if (api != null) {
            try {
                return api.destroyAOCContainer();
            } catch (Exception e) {
                ISOException.throwIt(SW_INTERNAL_ERROR_APPLET_REG);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
        }
        return false;
    }

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

    // Incoming format:
    // <CLA><INS><P1><P2><LC><balLen><payLen><loadLen><Balance><Payment><Loading>
    private short handleEWalletRequest(byte[] apduBuf, short len) {
        // Buffer off the apdu request first
        Util.arrayCopyNonAtomic(apduBuf, (short) 8, b1, (short) 0, len);

        // Access the P1 containing the display type
        byte displayType = apduBuf[2];

        // Decode the required lengths
        short balanceLen = (short) (apduBuf[5] & 0xFF);
        short paymentLen = (short) (apduBuf[6] & 0xFF);
        short loadingLen = (short) (apduBuf[7] & 0xFF);
        short balanceOff = 0;
        short paymentOff = (short) (balanceOff + balanceLen);
        short loadingOff = (short) (paymentOff + paymentLen);
        short ticketOff = (short) (loadingOff + loadingLen);

        // Attempt to login over Shareable Interface or if logged in proceed further
        if (appLogin(apduBuf, (short) 0, b0, (short) 0, s0[0]) == 1) {
            // Copy input and ticket to APDU buff

            Util.arrayCopyNonAtomic(b1, (short) 0, apduBuf, balanceOff, (short) (balanceLen + paymentLen + loadingLen));
            Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, ticketOff, T104OpenAPI.AUTH_TICKET_LEN);
            if (api.setGlobalWalletAmount(displayType, apduBuf, balanceOff, balanceLen, apduBuf, paymentOff, paymentLen,
                    apduBuf, loadingOff, loadingLen, apduBuf, ticketOff) == 1) {
                ISOException.throwIt(ISO7816.SW_NO_ERROR);
            } else {
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        return 0;
    }

    private short loadAmount(byte[] eventTimestamp, short eventTSOff, short eventTSLen, short amount, byte[] buff,
            short buffOff, byte[] buff1, short buff1Off, byte[] buff2, short buff2Off, byte[] apduBuf,
            short apduBufOff) {
        // Only allow to load amount more than 0, less than max funds and the loaded
        // total balance must be within the max funds.
        short i = 0;
        short j = 0;
        if (appLogin(apduBuf, apduBufOff, buff1, buff1Off, s0[0]) == 1) {
            if (amount > 0 && amount <= MAX_ALLOWED_FUNDS) {
                Util.arrayFillNonAtomic(buff, buffOff, (short) 6, (byte) 0x00);
                Util.setShort(buff, (short) (buffOff + 6), amount);

                // Buffer amount to buff2 for future use
                Util.arrayCopyNonAtomic(buff, buffOff, buff2, buff2Off, (short) 8);

                // Retrieve global balance fund
                i = getFunds(T104OpenAPI.WALLET_BALANCE_RECORD_SLOT, buff1, (short) (buff1Off + 8), buff1, buff1Off,
                        apduBuf, apduBufOff);
                if (i == 8) {
                    // Add current balance to proposed loading funds
                    MathUtil.int64Add(apduBuf, apduBufOff, buff, buffOff, buff1, buff1Off, (short) 8);
                    Util.setShort(buff, (short) (buffOff + 6), MAX_ALLOWED_FUNDS);
                    i = MathUtil.binArrayElementsCompare(buff1, buff1Off, buff, buffOff, (short) 8);

                    // Check if current holding funds + proposed loading funds > allowed funds
                    if (i == -1 || i == 0) {
                        // Funds totalled after loading is <= allowed funds
                        // Copy added resulting amount in buff1 to buff as buff1 is too small
                        Util.arrayCopyNonAtomic(buff1, buff1Off, buff, buffOff, (short) 8);

                        i = MathUtil.binDecToLongAsciiHex(buff, buffOff, buff1, buff1Off, buff1, (short) (buff1Off + 8),
                                buff1, (short) (buff1Off + 16), buff1, (short) (buff1Off + 24), buff1,
                                (short) (buff1Off + 32), apduBuf, (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN),
                                (short) 8);

                        if (i != -1) {
                            try {
                                // Reset payment funds to '0' and loaded funds to amount
                                Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, apduBufOff,
                                        T104OpenAPI.AUTH_TICKET_LEN);
                                apduBuf[(short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + i)] = (byte) 0x30;

                                // Compute ASCIIFied Hex Dec of amount buffered in buff2
                                j = MathUtil.binDecToLongAsciiHex(buff2, buff2Off, buff1, buff1Off, buff1,
                                        (short) (buff1Off + 8), buff1, (short) (buff1Off + 16), buff1,
                                        (short) (buff1Off + 24), buff1, (short) (buff1Off + 32), apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + i + 1), (short) 8);

                                // Buffer ASCIIFied Hex Dec amount to buff2
                                Util.arrayCopyNonAtomic(apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + i + 1), buff2, buff2Off,
                                        (short) j);

                                // Update funds
                                i = api.setGlobalWalletAmount(T104OpenAPI.WALLET_BALANCE_RECORD_SLOT, apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), i, apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + i), (short) 1, apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + i + 1), j, apduBuf,
                                        apduBufOff);
                            } catch (Exception e) {
                                // Error when trying to update global wallet's balance fund
                                ISOException.throwIt(SW_FUNDS_UPDATE_ERR);
                            }
                            if (i == 1) {
                                try {
                                    // Update event log
                                    apduBuf[apduBufOff] = SYM_INC;
                                    Util.arrayCopyNonAtomic(buff2, buff2Off, apduBuf, (short) (apduBufOff + 1), j);
                                    logCyclicAppend(LOG_TYPE_FUNDS, eventTimestamp, eventTSOff, eventTSLen, apduBuf,
                                            apduBufOff, (short) (j + 1));
                                } catch (Exception e) {
                                    // Error when trying to update tx log
                                    ISOException.throwIt(SW_FUNDS_LOG_UPDATE_ERR);
                                }
                            }
                            return i;
                        } else {
                            // MathUtil function error
                            ISOException.throwIt(SW_INTERNAL_ERROR_MATH_ERR);
                        }
                    } else {
                        // Too much funds after requested funds for loading is computed
                        ISOException.throwIt(SW_FUNDS_TOO_MUCH);
                    }
                } else {
                    // An error occurred when trying to enquire for balance funds
                    ISOException.throwIt(SW_FUNDS_ENQUIRY_ERR);
                }
            } else {
                // Too much funds requsted for loading
                ISOException.throwIt(SW_FUNDS_TOO_MUCH);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        return -4;
    }

    private boolean payAmount(byte[] eventTimestamp, short eventTSOff, short eventTSLen, short amount, byte[] buff,
            short buffOff, byte[] buff1, short buff1Off, byte[] buff2, short buff2Off, byte[] apduBuf,
            short apduBufOff) {
        // Only allow to load amount more than 0, less than max funds and the loaded
        // total balance must be within the max funds.
        short i = 0;
        short b2Len = 0;
        if (appLogin(apduBuf, apduBufOff, buff1, buff1Off, s0[0]) == 1) {
            // Only allow to spend not more than MAX_ALLOWED_FUNDS
            if (amount > 0 && amount <= MAX_ALLOWED_FUNDS) {
                Util.arrayFillNonAtomic(buff, buffOff, (short) 6, (byte) 0x00);
                Util.setShort(buff, (short) (buffOff + 6), amount);

                // Retrieve global balance fund
                i = getFunds(T104OpenAPI.WALLET_BALANCE_RECORD_SLOT, buff1, (short) (buff1Off + 8), buff1, buff1Off,
                        apduBuf, apduBufOff);
                if (i == 8) {
                    i = MathUtil.binArrayElementsCompare(apduBuf, apduBufOff, buff, buffOff, (short) 8);

                    // Check if current holding funds + proposed spending funds > allowed funds
                    if (i >= 0) {
                        // Funds totalled after loading is <= allowed funds
                        // Decrement funds
                        MathUtil.int64Subtract(apduBuf, apduBufOff, buff, buffOff, buff1, buff1Off, (short) 8);

                        // Copy resulting subtracted amount in buff1 to buff as buff1 is too small
                        Util.arrayCopyNonAtomic(buff1, buff1Off, buff, buffOff, (short) 8);

                        // Compute ASCIIFied hex decimal with results stored in buff2 temporarily
                        b2Len = MathUtil.binDecToLongAsciiHex(buff, buffOff, buff1, buff1Off, buff1,
                                (short) (buff1Off + 8), buff1, (short) (buff1Off + 16), buff1, (short) (buff1Off + 24),
                                buff1, (short) (buff1Off + 32), buff2, buff2Off, (short) 8);

                        if (b2Len != -1) {
                            // Re-construct amount
                            Util.arrayFillNonAtomic(buff, buffOff, (short) 6, (byte) 0x00);
                            Util.setShort(buff, (short) (buffOff + 6), amount);

                            // Retrieve global payment fund
                            i = getFunds(T104OpenAPI.WALLET_PAYMENT_RECORD_SLOT, buff1, (short) (buff1Off + 8), buff1,
                                    buff1Off, apduBuf, apduBufOff);

                            // Increment payment funds
                            MathUtil.int64Add(apduBuf, apduBufOff, buff, buffOff, buff1, buff1Off, (short) 8);

                            // Copy resulting added amount in buff1 to buff as buff1 is too small
                            Util.arrayCopyNonAtomic(buff1, buff1Off, buff, buffOff, (short) 8);

                            // Compute ASCIIFied hex decimal
                            i = MathUtil.binDecToLongAsciiHex(buff, buffOff, buff1, buff1Off, buff1,
                                    (short) (buff1Off + 8), buff1, (short) (buff1Off + 16), buff1,
                                    (short) (buff1Off + 24), buff1, (short) (buff1Off + 32), apduBuf,
                                    (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + b2Len), (short) 8);

                            try {
                                // Copy session auth ticket to apdu buffer
                                Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, apduBufOff,
                                        T104OpenAPI.AUTH_TICKET_LEN);

                                // Copy buffered balance amount in buff2 to apdu buffer
                                Util.arrayCopyNonAtomic(buff2, buff2Off, apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), b2Len);

                                if (api.setGlobalWalletAmount(T104OpenAPI.WALLET_BALANCE_RECORD_SLOT, apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), b2Len, apduBuf,
                                        (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN + b2Len), i, null, (short) 0,
                                        (short) 0, apduBuf, apduBufOff) == 1) {
                                    // Update event log
                                    apduBuf[apduBufOff] = SYM_DEC;

                                    // Re-construct amount
                                    Util.arrayFillNonAtomic(buff, buffOff, (short) 6, (byte) 0x00);
                                    Util.setShort(buff, (short) (buffOff + 6), amount);

                                    // Compute ASCIIFied hex decimal
                                    b2Len = MathUtil.binDecToLongAsciiHex(buff, buffOff, buff1, buff1Off, buff1,
                                            (short) (buff1Off + 8), buff1, (short) (buff1Off + 16), buff1,
                                            (short) (buff1Off + 24), buff1, (short) (buff1Off + 32), apduBuf,
                                            (short) (apduBufOff + 1), (short) 8);

                                    logCyclicAppend(LOG_TYPE_FUNDS, eventTimestamp, eventTSOff, eventTSLen, apduBuf,
                                            apduBufOff, (short) (b2Len + 1));
                                    return true;
                                }
                            } catch (Exception e) {
                                // Error when trying to update global wallet's balance fund
                                ISOException.throwIt(SW_FUNDS_UPDATE_ERR);
                            }
                        } else {
                            // MathUtil function error
                            ISOException.throwIt(SW_INTERNAL_ERROR_MATH_ERR);
                        }
                    } else {
                        // Insufficient funds for spending
                        ISOException.throwIt(SW_FUNDS_TOO_LITTLE);
                    }
                } else {
                    // An error occurred when trying to enquire for balance funds
                    ISOException.throwIt(SW_FUNDS_ENQUIRY_ERR);
                }
            } else {
                // Too much funds requsted for spending
                ISOException.throwIt(SW_FUNDS_TOO_MUCH);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        return false;
    }

    private short getFunds(byte fundType, byte[] buff, short buffOff, byte[] buff1, short buff1Off, byte[] apduBuf,
            short apduBufOff) {
        if (appLogin(apduBuf, apduBufOff, buff, buffOff, s0[0]) == 1) {
            Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, apduBufOff, T104OpenAPI.AUTH_TICKET_LEN);
            short ret = api.getGlobalWalletAmount(fundType, apduBuf, apduBufOff, apduBuf, apduBufOff);
            if (ret >= 0) {
                // Convert to binary decimal format
                Util.arrayCopyNonAtomic(apduBuf, apduBufOff, buff1, buff1Off, ret);
                if (MathUtil.longAsciiHexToBinDec(buff1, buff1Off, buff, buffOff, apduBuf, apduBufOff, ret)) {
                    return 8;
                } else {
                    ISOException.throwIt((short) 0x6f31);
                }
            }
        } else {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        return -1;
    }

    private short getTxLogByCountIndex(short c, byte[] output, short outOff) {
        short logCnt = 0;
        for (short i = 0; i < logs.length; i++) {
            if (logs[i].isReady() && logCnt == c) {
                return logs[i].getFormattedLog(output, outOff);
            } else {
                logCnt++;
            }
        }
        return 0;
    }

    private short getTxLogCount() {
        short logCnt = 0;
        for (short i = 0; i < logs.length; i++) {
            if (logs[i].isReady()) {
                logCnt++;
            }
        }

        return logCnt;
    }

    private short getMostRecentTxLogIndex(byte logType) {
        for (short i = (short) (getTxLogCount() - 1); i >= 0; i--) {
            if (logs[i].isReady() && logs[i].getLogType() == logType) {
                return i;
            }
        }
        return -1;
    }

    private void logCyclicAppend(byte logType, byte[] timestamp, short tsOff, short tsLen, byte[] content, short cOff,
            short cLen) {
        short i = getTxLogCount();
        if (i == MAX_LOGS) {
            resetEWalletLogs(timestamp, tsOff, tsLen);
            i++;
        }
        if (logs[i].isReady()) {
            logs[i].clear();
        }
        logs[i].initialize(logType, timestamp, tsOff, tsLen, content, cOff, cLen);
    }

    private void resetEWalletFunds(byte[] timestamp, short tsOff, short tsLen, byte[] buff, short buffOff,
            byte[] apduBuf, short apduBufOff) {
        if (appLogin(apduBuf, apduBufOff, buff, buffOff, s0[0]) == 1) {
            Util.arrayCopyNonAtomic(ticket, (short) 0, apduBuf, apduBufOff, T104OpenAPI.AUTH_TICKET_LEN);
            Util.arrayCopyNonAtomic(DEFAULT_FUNDS, (short) 0, apduBuf,
                    (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), (short) DEFAULT_FUNDS.length);
            if (api.setGlobalWalletAmount(T104OpenAPI.WALLET_BALANCE_RECORD_SLOT, apduBuf,
                    (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), (short) DEFAULT_FUNDS.length, apduBuf,
                    (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), (short) DEFAULT_FUNDS.length, apduBuf,
                    (short) (apduBufOff + T104OpenAPI.AUTH_TICKET_LEN), (short) DEFAULT_FUNDS.length, apduBuf,
                    apduBufOff) != 1) {
                ISOException.throwIt(SW_FUNDS_UPDATE_ERR);
            }
            logCyclicAppend(LOG_TYPE_GENERAL, timestamp, tsOff, tsLen, DEFAULT_LOG_EWALLET_FUNDS_RESET_MSG, (short) 0,
                    (short) DEFAULT_LOG_EWALLET_FUNDS_RESET_MSG.length);
        } else {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    private void resetEWalletLogs(byte[] timestamp, short tsOff, short tsLen) {
        for (short i = 0; i < logs.length; i++) {
            logs[i].clear();
        }
        logs[0].initialize(LOG_TYPE_GENERAL, timestamp, tsOff, tsLen, DEFAULT_LOG_INIT_MSG, (short) 0,
                (short) DEFAULT_LOG_INIT_MSG.length);
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            init();
            return;
        }

        if (!isReady) {
            ISOException.throwIt(ISO7816.SW_APPLET_SELECT_FAILED);
            return;
        }

        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();
        byte p1 = buf[ISO7816.OFFSET_P1];
        byte p2 = buf[ISO7816.OFFSET_P2];
        switch (buf[ISO7816.OFFSET_INS]) {
            case (byte) 0xFF:
                // Init the T104OpenAPI
                initAPI(buf, (short) 0);
                break;            
            case (byte) 0x03:
                if (p1 == (byte) 0x01) {
                    if (len == 7) {
                        short loadAmt = Util.makeShort(buf[5], buf[6]);
                        Util.arrayCopyNonAtomic(buf, (short) 7, b3, (short) 0, (short) 5);
                        len = loadAmount(b3, (short) 0, (short) 5, loadAmt, b1, (short) 0, b0, (short) 0, b2, (short) 0,
                                buf, (short) 0);
                        if (len == 1) {
                        } else if (len == -2) {
                            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                        } else if (len == -3) {
                            ISOException.throwIt((short) 0x6ff3);
                        } else if (len == -4) {
                            ISOException.throwIt((short) 0x6ff4);
                        } else {
                            ISOException.throwIt((short) 0x6ff5);
                        }
                    } else {
                        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                    }
                } else if (p1 == (byte) 0x02) {
                    if (len == 7) {
                        short payAmt = Util.makeShort(buf[5], buf[6]);
                        Util.arrayCopyNonAtomic(buf, (short) 7, b3, (short) 0, (short) 5);
                        if (payAmount(b3, (short) 0, (short) 5, payAmt, b1, (short) 0, b0, (short) 0, b2, (short) 0,
                                buf, (short) 0)) {
                            ISOException.throwIt(ISO7816.SW_NO_ERROR);
                        } else {
                            ISOException.throwIt((short) 0x6ff5);
                        }
                    } else {
                        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                    }
                } else if (p1 == (byte) 0x03) {
                    // getFunds
                    len = getFunds(p2, b0, (short) 8, b0, (short) 0, buf, (short) 0);
                    apdu.setOutgoingAndSend((short) 0, len);
                } else if (p1 == (byte) 0x04) {
                    // Get logs
                    if (p2 == 0x00) {
                        buf[0] = (byte) (getTxLogCount() & 0xFF);
                        apdu.setOutgoingAndSend((short) 0, (short) 1);
                    } else if (p2 == 0x01) {
                        if (len != 1) {
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        len = (short) (buf[5] & 0xFF);
                        len = getTxLogByCountIndex(len, buf, (short) 0);
                        if (len > 0) {
                            apdu.setOutgoingAndSend((short) 0, len);
                        } else {
                            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
                        }
                    } else if (p2 == 0x02) {
                        if (len != 1) {
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        byte type = buf[5];
                        len = getMostRecentTxLogIndex(type);
                        len = logs[len].getFormattedLog(buf, (short) 0);
                        apdu.setOutgoingAndSend((short) 0, len);
                    } else {
                        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                    }
                } else if (p1 == (byte) 0x0F) {
                    // Data reset
                    if (len != 5) {
                        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                    }
                    if (p2 == (byte) 0x00) {
                        // Reset wallet funds all to ASCII '0'.
                        Util.arrayCopyNonAtomic(buf, (short) 5, b3, (short) 0, (short) 5);
                        resetEWalletFunds(b3, (short) 0, (short) 5, b0, (short) 0, buf, (short) 0);
                    } else if (p2 == (byte) 0x01) {
                        // Reset wallet log while appending an entry of log clearance activity after
                        // clearing logs.
                        Util.arrayCopyNonAtomic(buf, (short) 5, b3, (short) 0, (short) 5);
                        resetEWalletLogs(b3, (short) 0, (short) 5);
                    } else {
                        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                    }
                } else {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}