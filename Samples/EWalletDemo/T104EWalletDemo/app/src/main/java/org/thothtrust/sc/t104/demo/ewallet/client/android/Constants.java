package org.thothtrust.sc.t104.demo.ewallet.client.android;

public class Constants {

    public static final short VERSION = 256;
    public static final byte NULL = (byte) 0x00;
    public static final byte APDU_CLA = NULL;
    public static final byte APDU_INS_EWALLET = (byte) 0x03;
    public static final byte APDU_P1_LOAD = (byte) 0x01;
    public static final byte APDU_P1_PAY = (byte) 0x02;
    public static final byte APDU_P1_ENQUIRE_FUNDS = (byte) 0x03;
    public static final byte APDU_P1_ENQUIRE_LOGS = (byte) 0x04;
    public static final byte APDU_P1_RESET = (byte) 0x0F;
    public static final byte APDU_P2_LOG = (byte) 0x01;
    public static final byte FUND_TYPE_BALANCE = NULL;
    public static final byte FUND_TYPE_PAYMENT = (byte) 0x10;
    public static final byte FUND_TYPE_LOADED = (byte) 0x20;

    public static final byte[] APDU_SELECT = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
            (byte) 0x0E, (byte) 0x43, (byte) 0x57, (byte) 0x4D, (byte) 0x49, (byte) 0x4E, (byte) 0x49,
            (byte) 0x45, (byte) 0x57, (byte) 0x41, (byte) 0x4C, (byte) 0x4C, (byte) 0x45, (byte) 0x54, (byte) 0x00 };

    public static final byte[] APDU_RAW_LOAD_FUNDS = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_LOAD, NULL, NULL
    };

    public static final byte[] APDU_RAW_ENQUIRE_FUNDS = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_ENQUIRE_FUNDS, NULL, NULL
    };

    public static final byte[] APDU_RAW_PAY_FUNDS = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_PAY, NULL, NULL
    };

    public static final byte[] APDU_RAW_GET_LOGS_COUNT = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_ENQUIRE_LOGS, NULL, NULL
    };

    public static final byte[] APDU_RAW_GET_LOG_BY_COUNT_INDEX = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_ENQUIRE_LOGS, APDU_P2_LOG, NULL
    };

    public static final byte[] APDU_RAW_RESET_WALLET_FUNDS = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_RESET, NULL, NULL
    };

    public static final byte[] APDU_RAW_RESET_WALLET_LOGS = {
            APDU_CLA, APDU_INS_EWALLET, APDU_P1_RESET, APDU_P2_LOG, NULL
    };

}