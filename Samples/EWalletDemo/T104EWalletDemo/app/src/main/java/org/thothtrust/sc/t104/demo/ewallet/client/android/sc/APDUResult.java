package org.thothtrust.sc.t104.demo.ewallet.client.android.sc;

public class APDUResult {

    private byte[] result;
    private byte[] sw;
    private boolean isSuccess;

    public APDUResult(byte[] result) {
        setResult(SCAPDU.getSuccessfulResponseData(result, 0, result.length));
        setSw(SCAPDU.getSW(result, result.length - 2));
        setSuccess(SCAPDU.isSWOK(result, result.length - 2));
    }

    public APDUResult(byte[] result, byte[] sw, boolean isSuccess) {
        setResult(result);
        setSw(sw);
        setSuccess(isSuccess);
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public byte[] getSw() {
        return sw;
    }

    public void setSw(byte[] sw) {
        this.sw = sw;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

}
