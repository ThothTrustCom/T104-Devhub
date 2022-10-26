package org.thothtrust.sc.t104.demo.ewallet.client.android.sc;

import android.nfc.tech.IsoDep;

import org.thothtrust.sc.t104.demo.ewallet.client.android.Constants;
import org.thothtrust.sc.t104.demo.ewallet.client.android.exceptions.*;
import org.thothtrust.sc.t104.demo.ewallet.client.android.utils.BinUtils;

import java.io.IOException;

public class SCAPDU {

    private static byte[] apduBytes = null;
    public static final int inputApduOff = 5;

    public static APDUResult selectApplet(IsoDep iso14443) throws IOException {
        return new APDUResult(iso14443.transceive(Constants.APDU_SELECT));
    }

    public static APDUResult rawEnquireFunds(IsoDep iso14443, byte fundType) throws IOException, InvalidSizeException {
        buildAPDU(Constants.APDU_RAW_ENQUIRE_FUNDS, false, false, false,
                true, Constants.NULL, Constants.NULL, Constants.NULL, fundType,
                null, 0, 0);
        return new APDUResult(iso14443.transceive(apduBytes));
    }

    public static APDUResult rawLoadFunds(IsoDep iso14443, short amount, byte DD, byte MM, byte YY,
                                          byte hh, byte mm) throws IOException, InvalidSizeException {
        byte[] input = new byte[7];
        BinUtils.shortToBytes(amount, input, (short) 0);
        input[2] = DD;
        input[3] = MM;
        input[4] = YY;
        input[5] = hh;
        input[6] = mm;
        buildAPDU(Constants.APDU_RAW_LOAD_FUNDS, false, false, false,
                false, Constants.NULL, Constants.NULL, Constants.NULL, Constants.NULL,
                input, 0, input.length);
        return new APDUResult(iso14443.transceive(apduBytes));
    }

    public static APDUResult rawPayFunds(IsoDep iso14443, short amount, byte DD, byte MM, byte YY,
                                         byte hh, byte mm) throws IOException, InvalidSizeException {
        byte[] input = new byte[7];
        BinUtils.shortToBytes(amount, input, (short) 0);
        input[2] = DD;
        input[3] = MM;
        input[4] = YY;
        input[5] = hh;
        input[6] = mm;
        buildAPDU(Constants.APDU_RAW_PAY_FUNDS, false, false, false,
                false, Constants.NULL, Constants.NULL, Constants.NULL, Constants.NULL,
                input, 0, input.length);
        return new APDUResult(iso14443.transceive(apduBytes));
    }

    public static APDUResult rawGetLogsCount(IsoDep iso14443) throws IOException, InvalidSizeException {
        return new APDUResult(iso14443.transceive(Constants.APDU_RAW_GET_LOGS_COUNT));
    }

    public static APDUResult rawGetLogByCountIndex(IsoDep iso14443, short i) throws IOException,
            InvalidSizeException {
        buildAPDU(Constants.APDU_RAW_GET_LOG_BY_COUNT_INDEX, false, false, false,
                false, Constants.NULL, Constants.NULL, Constants.NULL, Constants.NULL,
                new byte[] { (byte) (i & 0xFF) }, 0, 1);
        return new APDUResult(iso14443.transceive(apduBytes));
    }

    public static APDUResult rawResetWalletFunds(IsoDep iso14443, byte DD, byte MM, byte YY, byte hh,
                                                 byte mm) throws IOException, InvalidSizeException {
        byte[] input = new byte[5];
        input[0] = DD;
        input[1] = MM;
        input[2] = YY;
        input[3] = hh;
        input[4] = mm;
        buildAPDU(Constants.APDU_RAW_RESET_WALLET_FUNDS, false, false, false,
                false, Constants.NULL, Constants.NULL, Constants.NULL, Constants.NULL,
                input, 0, input.length);
        return new APDUResult(iso14443.transceive(apduBytes));
    }

    public static APDUResult rawResetWalletLogs(IsoDep iso14443, byte DD, byte MM, byte YY, byte hh,
                                                byte mm) throws
            IOException, InvalidSizeException {
        byte[] input = new byte[5];
        input[0] = DD;
        input[1] = MM;
        input[2] = YY;
        input[3] = hh;
        input[4] = mm;
        buildAPDU(Constants.APDU_RAW_RESET_WALLET_LOGS, false, false, false,
                false, Constants.NULL, Constants.NULL, Constants.NULL, Constants.NULL,
                input, 0, input.length);
        return new APDUResult(iso14443.transceive(apduBytes));
    }

    public static boolean isSWOK(byte[] SW, int off) {
        if (SW[off] == (byte) 0x90 && SW[off + 1] == (byte) 0x00) {
            return true;
        }

        return false;
    }

    public static byte[] getSW(byte[] SW, int off) {
        return new byte[]{SW[off], SW[off + 1]};
    }

    public static byte[] getSuccessfulResponseData(byte[] apduData, int off, int len) {
        if (isSWOK(apduData, off + len - 2)) {
            byte[] retData = new byte[len - 2];
            System.arraycopy(apduData, off, retData, 0, len - 2);
            return retData;
        } else {
            return null;
        }
    }

    public static void buildAPDU(byte INS, byte P1, byte P2, byte[] data, int off, int len) throws
            InvalidSizeException {
        if (len > 255) {
            throw new InvalidSizeException("Data input is too long");
        }
        apduBytes = new byte[5 + len];
        apduBytes[0] = Constants.APDU_CLA;
        apduBytes[1] = INS;
        apduBytes[2] = P1;
        apduBytes[3] = P2;
        apduBytes[4] = Constants.NULL;
        if (data != null && len > 0) {
            apduBytes[4] = (byte) (len & 0xFF);
            System.arraycopy(data, off, apduBytes, inputApduOff, len);
        }
    }

    public static void buildAPDU(byte[] template, boolean updateCLA, boolean updateINS,
                                 boolean updateP1, boolean updateP2, byte CLA, byte INS,
                                 byte P1, byte P2, byte[] data, int off, int len) throws
            InvalidSizeException {
        if (len > 255) {
            throw new InvalidSizeException("Data input is too long");
        }
        apduBytes = new byte[5 + len];
        if (updateCLA) {
            apduBytes[1] = CLA;
        } else {
            apduBytes[0] = template[0];
        }
        if (updateINS) {
            apduBytes[1] = INS;
        } else {
            apduBytes[1] = template[1];
        }
        if (updateP1) {
            apduBytes[2] = P1;
        } else {
            apduBytes[2] = template[2];
        }
        if (updateP2) {
            apduBytes[3] = P2;
        } else {
            apduBytes[3] = template[3];
        }
        apduBytes[4] = Constants.NULL;
        if (data != null && len > 0) {
            apduBytes[4] = (byte) (len & 0xFF);
            System.arraycopy(data, off, apduBytes, inputApduOff, len);
        }
    }
}