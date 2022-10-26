package EWalletApplet;

import javacard.framework.JCSystem;
import javacard.framework.Util;

public class TxLog {

	private byte type = (byte) 0x00;
    private byte[] timestamp = null;
    private byte[] content = null;
    private boolean isReady = false;

    public TxLog() {
    }

    public void initialize(byte logType, byte[] inTimestamp, short tsOff, short tsLen, byte[] inContent, short cOff, short cLen) {
        if (inTimestamp != null && tsLen == 5) {
            JCSystem.beginTransaction();
            type = logType;
            if (timestamp != null) {
                timestamp = null;
            }
            timestamp = new byte[tsLen];
            Util.arrayCopyNonAtomic(inTimestamp, tsOff, timestamp, (short) 0, tsLen);
            if (inContent != null && cLen > 0) {
                if (content != null) {
                    content = null;
                }
                content = new byte[cLen];
                Util.arrayCopyNonAtomic(inContent, cOff, content, (short) 0, cLen);
            }
            isReady = true;
            JCSystem.commitTransaction();
        }
    }
    
    public byte getLogType() {
	    return type;
    }

    public short getTimestamp(byte[] output, short outOff) {
        if (timestamp != null) {
            Util.arrayCopyNonAtomic(timestamp, (short) 0, output, outOff, (short) timestamp.length);
            return (short) timestamp.length;
        }

        return 0;
    }

    public short getContent(byte[] output, short outOff) {
        if (content != null) {
            Util.arrayCopyNonAtomic(content, (short) 0, output, outOff, (short) content.length);
            return (short) content.length;
        }

        return 0;
    }
    
    public short getFormattedLog(byte[] output, short outOff) {
    	short writeLen = 0;
	    if (isReady) {
	    	output[outOff] = type;	    	
		    writeLen += getTimestamp(output, (short) (outOff + 1));
		    writeLen += (short) (getContent(output, (short) (outOff + 1 + writeLen)) + 1);
	    }
	    
	    return writeLen;
    }

    public boolean isReady() {
        return isReady;
    }

    public void clear() {
        JCSystem.beginTransaction();
        type = (byte) 0x00;
        if (this.timestamp != null) {
            timestamp = null;
        }
        if (this.content != null) {
            content = null;
        }
        isReady = false;
        JCSystem.commitTransaction();
    }
}