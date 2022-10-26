package org.thothtrust.sc.t104.demo.ewallet.client.android;

import android.nfc.tech.IsoDep;
import java.math.BigInteger;
import java.sql.Timestamp;
import org.thothtrust.sc.t104.demo.ewallet.client.android.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.APDUResult;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.SCAPDU;

import java.io.IOException;

public class API {

    public static byte[][] getLogs(IsoDep iso14443) throws InvalidSizeException, IOException {
        APDUResult res = SCAPDU.rawGetLogsCount(iso14443);
        byte[][] logs = null;
        int logCount = 0;
        if (res != null) {
            if (res.isSuccess()) {
                logCount = (int) (res.getResult()[0] & 0xFF);
            }
        }
        if (logCount > 0) {
            logs = new byte[logCount][];
            for (short i = 0; i < logCount; i++) {
                res = SCAPDU.rawGetLogByCountIndex(iso14443, i);
                if (res != null) {
                    if (res.isSuccess()) {
                        logs[i] = res.getResult();
                    }
                }
            }
        }
        return logs;
    }

    public static short getFunds(IsoDep iso14443, byte fundType) throws InvalidSizeException,
            IOException {
        APDUResult res = SCAPDU.rawEnquireFunds(iso14443, fundType);
        if (res != null) {
            if (res.isSuccess()) {
                return new BigInteger(res.getResult()).shortValue();
            }
        }
        return -1;
    }

    public static boolean loadFunds(IsoDep iso14443, short amount) throws InvalidSizeException,
            IOException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        byte DD = (byte) (ts.getDate() & 0xFF);
        byte MM = (byte) ((ts.getMonth() + 1) & 0xFF);
        byte YY = (byte) ((ts.getYear() - 100) & 0xFF);
        byte hh = (byte) (ts.getHours() & 0xFF);
        byte mm = (byte) (ts.getMinutes() & 0xFF);
        APDUResult res = SCAPDU.rawLoadFunds(iso14443, amount, DD, MM, YY, hh, mm);
        if (res != null) {
            return res.isSuccess();
        }
        return false;
    }

    public static boolean payFunds(IsoDep iso14443, short amount) throws InvalidSizeException,
            IOException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        byte DD = (byte) (ts.getDate() & 0xFF);
        byte MM = (byte) ((ts.getMonth() + 1) & 0xFF);
        byte YY = (byte) ((ts.getYear() - 100) & 0xFF);
        byte hh = (byte) (ts.getHours() & 0xFF);
        byte mm = (byte) (ts.getMinutes() & 0xFF);
        APDUResult res = SCAPDU.rawPayFunds(iso14443, amount, DD, MM, YY, hh, mm);
        if (res != null) {
            return res.isSuccess();
        }
        return false;
    }

    public static boolean resetWalletFunds(IsoDep iso14443) throws InvalidSizeException,
            IOException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        byte DD = (byte) (ts.getDate() & 0xFF);
        byte MM = (byte) ((ts.getMonth() + 1) & 0xFF);
        byte YY = (byte) ((ts.getYear() - 100) & 0xFF);
        byte hh = (byte) (ts.getHours() & 0xFF);
        byte mm = (byte) (ts.getMinutes() & 0xFF);
        APDUResult res = SCAPDU.rawResetWalletFunds(iso14443, DD, MM, YY, hh, mm);
        if (res != null) {
            return res.isSuccess();
        }
        return false;
    }

    public static boolean resetWalletLogs(IsoDep iso14443) throws InvalidSizeException,
            IOException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        byte DD = (byte) (ts.getDate() & 0xFF);
        byte MM = (byte) ((ts.getMonth() + 1) & 0xFF);
        byte YY = (byte) ((ts.getYear() - 100) & 0xFF);
        byte hh = (byte) (ts.getHours() & 0xFF);
        byte mm = (byte) (ts.getMinutes() & 0xFF);
        APDUResult res = SCAPDU.rawResetWalletLogs(iso14443, DD, MM, YY, hh, mm);
        if (res != null) {
            return res.isSuccess();
        }
        return false;
    }
}
