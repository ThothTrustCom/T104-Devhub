package EWalletApplet;

import javacard.framework.ISOException;

public class MathUtil {

    public static final byte CMP_EQUALS_TO = (byte) 0x00;
    public static final byte CMP_BIGGER_THAN = (byte) 0x01;
    public static final byte CMP_SMALLER_THAN = (byte) 0x02;
    public static final byte CMP_FAIL = (byte) 0x0F;
    public static final byte[] ASCIIHEX = new byte[] { (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34,
            (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, (byte) 0x41, (byte) 0x42, (byte) 0x43,
            (byte) 0x44, (byte) 0x45, (byte) 0x46 };

    public static final byte[] BCD_SIZE_PER_BYTES = { 0, 3, 5, 8, 10, 13, 15, 17, 20, 22, 25, 27, 29, 32, 34, 37, 39,
            41, 44, 46, 49, 51, 53, 56, 58, 61, 63, 66, 68, 70, 73, 75, 78 };

    public static final byte[] conversionList1 = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x01, // 1
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, // 10
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, // 100
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xe8, // 1000
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x27, (byte) 0x10, // 10000
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x86, (byte) 0xa0, // 100000
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0F, (byte) 0x42, (byte) 0x40, // 1000000
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x98, (byte) 0x96, (byte) 0x80, // 10000000
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0xF5, (byte) 0xe1, (byte) 0x00 // 100000000
    };

    public static final byte[] ZERO = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    public static final byte[] ONE = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x01 };

    public static final short[] conversionList1IndexOffset = { 0, 8, 16, 24, 32, 40, 48, 56, 64 };

    public static short toDecimalASCII(byte[] uBigBuf, short uBigOff, short uBigLen, byte[] decBuf, short decOff) {
        short bcdDigits = (short) BCD_SIZE_PER_BYTES[uBigLen];
        short byteValue, dividend, remainder;

        for (short bcdIndex = 0; bcdIndex < bcdDigits; bcdIndex++) {
            remainder = 0;
            for (short uBigIndex = 0; uBigIndex < uBigLen; uBigIndex++) {
                byteValue = (short) (uBigBuf[(short) (uBigOff + uBigIndex)] & 0xFF);
                dividend = (short) (remainder * 256 + byteValue);
                remainder = (short) (dividend % 10);
                uBigBuf[(short) (uBigOff + uBigIndex)] = (byte) (dividend / 10);
            }
            decBuf[(short) (decOff + bcdDigits - bcdIndex - 1)] = (byte) (remainder + '0');
        }

        return bcdDigits;
    }

    public static boolean int64Add(byte[] a, short aOff, byte[] b, short bOff, byte[] result, short rOff, short len) {
        short carry = 0;
        for (len = (byte) (len - 1); len >= 0; len--) {
            carry = (short) ((short) (a[(short) (len) + aOff] & 0x00FF) + (short) (b[(short) (len) + bOff] & 0x00FF)
                    + carry);
            result[(short) (len + rOff)] = (byte) carry;
            if (carry > 0x00FF) {
                carry = 1;
            } else {
                carry = 0;
            }
        }

        if (carry == 1) {
            return false;
        }
        return true;
    }

    public static boolean int64Subtract(byte[] a, short aOff, byte[] b, short bOff, byte[] result, short rOff,
            short len) {
        byte borrow = 0;
        short comp = 0;
        for (len = (byte) (len - 1); len >= 0; len--) {
            comp = (short) ((short) (a[(short) (len) + aOff] & 0x00FF) - (short) (b[(short) (len) + bOff] & 0x00FF)
                    - borrow);
            if (comp < 0) {
                borrow = 1;
                comp = (short) (comp + 0x0100);
            } else {
                borrow = 0;
            }
            result[(short) (len + rOff)] = (byte) comp;
        }

        if (borrow == 1) {
            return false;
        }
        return true;
    }

    public static short getBit(byte[] data, short off, short targetPos) {
        short pos = (short) ((off * 8) + targetPos);
        short posByte = (short) (pos / 8);
        short posBit = (short) (pos % 8);
        byte valByte = data[posByte];
        short valFin = (short) (valByte >> (8 - (posBit + 1)) & 0x0001);
        return valFin;
    }

    public static short binToAsciiHex(byte[] input, short offset, short len, byte[] output, short outOff, short sbuff) {
        for (sbuff = (short) 0; sbuff < (short) (len * 2); sbuff += (short) 2) {
            output[(short) (outOff + sbuff)] = ASCIIHEX[(short) ((input[(short) (offset + (sbuff / 2))] >> (short) 4)
                    & (byte) 0x0F)];
            output[(short) (outOff + sbuff + 1)] = ASCIIHEX[(short) (input[(short) (offset + (sbuff / 2))]
                    & (byte) 0x0F)];
        }
        return (short) (len * 2);
    }

    public static byte asciiNibbleToBin(byte b) {
        if (((short) (b & 0xFF) > (short) 47) && ((short) (b & 0xFF) < (short) 58)) {
            return (byte) (b & 0x0F);
        } else {
            if ((b == (byte) 0x41) || (b == (byte) 0x61)) {
                return (byte) 0x0a;
            } else if ((b == (byte) 0x42) || (b == (byte) 0x62)) {
                return (byte) 0x0b;
            } else if ((b == (byte) 0x43) || (b == (byte) 0x63)) {
                return (byte) 0x0c;
            } else if ((b == (byte) 0x44) || (b == (byte) 0x64)) {
                return (byte) 0x0d;
            } else if ((b == (byte) 0x45) || (b == (byte) 0x65)) {
                return (byte) 0x0e;
            } else if ((b == (byte) 0x46) || (b == (byte) 0x66)) {
                return (byte) 0x0f;
            }
        }
        return (byte) 0xFF;
    }

    public static short hexStrToBin(byte[] input, short offset, short len, byte[] output, short outOff, short sbuff,
            byte[] buff, short buffOffset) {
        if ((short) (len % 2) == (short) 0) {
            for (sbuff = (short) 0; sbuff < len; sbuff += (short) 2) {
                buff[buffOffset] = asciiNibbleToBin(input[(short) (offset + sbuff)]);
                buff[(short) (buffOffset + 1)] = asciiNibbleToBin(input[(short) (offset + sbuff + 1)]);
                if ((buff[buffOffset] == (byte) 0xFF) || (buff[(short) (buffOffset + 1)] == (byte) 0xFF)) {
                    return (short) -1;
                }
                output[(short) (outOff + (sbuff / 2))] = (byte) ((byte) (buff[buffOffset] << 4)
                        | buff[(short) (buffOffset + 1)]);
            }
            return (short) (len / 2);
        }

        return (short) -1;
    }

    // NOTE: Only to be used on ASCIIfied positive integers of no more than 2 digits
    // length.
    public static void asciiToInt(byte[] input, short offset, short len, byte[] output, short outOffset) {
        output[outOffset] = (byte) (input[offset] & 0x0F);
        if (len == (short) 2) {
            output[outOffset] = (byte) ((byte) (output[outOffset] << 4) | (byte) (input[(short) (offset + 1)] & 0x0F));
        }
    }

    public static byte byteBool(boolean b) {
        if (b) {
            return (byte) 0xFF;
        } else {
            return (byte) 0x00;
        }
    }

    public static boolean boolByte(byte b) {
        if (b == (byte) 0xFF) {
            return true;
        }

        return false;
    }

    public static short shortBool(boolean b) {
        if (b) {
            return (short) 1;
        } else {
            return (short) 0;
        }
    }

    public static boolean boolShort(short b) {
        if (b == (short) 1) {
            return true;
        } else {
            return false;
        }
    }

    public static void shortToBytes(short s, byte[] b, short offset) {
        b[offset] = (byte) ((s >> 8) & 0xFF);
        b[(short) (offset + 1)] = (byte) (s & 0xFF);
    }

    public static void intToBytes(int i, byte[] b, short offset) {
        b[offset] = (byte) ((i >> 8) & 0xFF);
        b[(short) (offset + 1)] = (byte) ((i >> 16) & 0xFF);
        b[(short) (offset + 2)] = (byte) ((i >> 24) & 0xFF);
        b[(short) (offset + 3)] = (byte) (i & 0xFF);
    }

    public static boolean isZero(byte[] a, short aOff, short aLen) {
        for (aLen = (byte) (aLen - 1); aLen >= 0; aLen--) {
            if (a[aLen] != (byte) 0x00)
                return false;
        }
        return true;
    }

    public static byte getLSBBit(byte b) {
        return (byte) (b & 0x01);
    }

    // Returns 1: bigger than, -1: smaller than, 0: equals to, -2: error
    public static short binArrayElementsCompare(byte[] srcArray, short srcOffset, byte[] destArray, short destOffset,
            short length) {
        if ((srcArray != null) && (destArray != null)) {
            for (short i = 0; i < length; i++) {
                if (srcArray[srcOffset + i] != destArray[destOffset + i]) {
                    if ((short) (srcArray[srcOffset + i] & 0xFF) > (short) (destArray[destOffset + i] & 0xFF)) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        } else {
            return -2;
        }
        return 0;
    }

    public static void shiftLeft(byte[] a, short aOff, short aLen, byte[] b, short bOff, short shiftBitCount) {
        short shiftMod = (short) (shiftBitCount % 8);
        byte carryMask = (byte) ((1 << shiftMod) - 1);
        short offsetBytes = (short) (shiftBitCount / 8);

        short sourceIndex;
        for (short i = 0; i < aLen; i++) {
            sourceIndex = (short) (i + offsetBytes);
            if (sourceIndex >= aLen) {
                b[(short) (bOff + i)] = 0;
            } else {
                byte src = a[(short) (aOff + sourceIndex)];
                byte dst = (byte) (src << shiftMod);
                if (sourceIndex + 1 < aLen) {
                    dst |= a[(short) (aOff + sourceIndex + 1)] >>> (8 - shiftMod) & carryMask;
                }
                b[(short) (bOff + i)] = dst;
            }
        }
    }

    public static void shiftRight(byte[] a, short aOff, short aLen, byte[] b, short bOff, short shiftBitCount) {
        short shiftMod = (short) (shiftBitCount % 8);
        byte carryMask = (byte) (0xFF << (8 - shiftMod));
        short offsetBytes = (short) (shiftBitCount / 8);

        short sourceIndex;
        for (short i = (short) (aLen - 1); i >= 0; i--) {
            sourceIndex = (short) (i - offsetBytes);
            if (sourceIndex < 0) {
                b[(short) (bOff + i)] = 0;
            } else {
                byte src = a[(short) (aOff + sourceIndex)];
                byte dst = (byte) ((0xFF & src) >>> shiftMod);
                if (sourceIndex - 1 >= 0) {
                    dst |= a[(short) (aOff + sourceIndex - 1)] << (8 - shiftMod) & carryMask;
                }
                b[(short) (bOff + i)] = dst;
            }
        }
    }

    // Returns 8 bytes encoded binary decimal
    public static boolean longAsciiHexToBinDec(byte[] a, short aOff, byte[] buff, short buffOff, byte[] result,
            short rOff, short len) {
        short aIndex = (short) (len - 1);
        short pIndex = 0;
        short allowedLen = 8;

        // Check length
        if (len > allowedLen || len < 0) {
            ISOException.throwIt((short) 0x6f32);
            return false;
        }

        // Clean result buffer
        for (short i = 0; i < allowedLen; i++) {
            result[(short) (rOff + i)] = (byte) 0x00;
        }

        for (len = (byte) (len - 1); len >= 0; len--) {
            // Multiply part

            // Start from last input index and get
            byte inByte = (byte) (a[aIndex] & 0xFF);

            // Filter for 0x30 - 0x39 only
            if (!((short) inByte >= 48 && (short) inByte <= 57)) {
                ISOException.throwIt((short) 0x6f33);
                return false;
            }

            // Strip off 0x3-
            inByte ^= (byte) 0x30;

            // Populate with the correct placement multiplier
            for (short i = 0; i < allowedLen; i++) {
                buff[(short) (buffOff + i)] = conversionList1[(short) (conversionList1IndexOffset[pIndex] + i)];
            }

            // X <- placement multiplier, Y <- inByte
            while (inByte != (byte) 0x00) {
                // if LSB(y) = 1
                if (getLSBBit(inByte) == (byte) 0x01) {
                    // a <- a + x
                    int64Add(result, rOff, buff, buffOff, result, rOff, allowedLen);
                }
                // x <- x << 1
                shiftLeft(buff, buffOff, allowedLen, buff, buffOff, (short) 1);

                // y <- y >> 1
                inByte = (byte) (inByte >> 1);
            }

            // 3.) Decrement a index location
            aIndex--;

            // 4.) Increment placement index location
            pIndex++;
        }

        return true;
    }

    public static short binDecToLongAsciiHex(byte[] a, short aOff, byte[] buff, short buffOff, byte[] buff1,
            short buff1Off, byte[] buff2, short buff2Off, byte[] buff3, short buff3Off, byte[] buff4, short buff4Off,
            byte[] result, short rOff, short len) {
        short allowedLen = 8;
        short pIndex = 1;
        short writeLen = 0;
        boolean hasNonZeroWritten = false;

        // Check length
        if (len > allowedLen || len < 0) {
            return -1;
        }

        // Clean and buffers
        for (short i = 0; i < allowedLen; i++) {
            buff4[(short) (buff4Off + i)] = (byte) 0x00;
            result[(short) (rOff + i)] = (byte) 0x00;
            if (i == 7) {
                buff1[(short) (buff1Off + i)] = (byte) 0x01;
            } else {
                buff1[(short) (buff1Off + i)] = (byte) 0x00;
            }
        }

        // Try all placement for division
        for (short l = 0; l < allowedLen; l++) {
            // Populate buff2 with fresh copy of a and a is num1
            for (short i = 0; i < allowedLen; i++) {
                buff2[(short) (buff2Off + i)] = (byte) 0x00;
            }
            for (short m = (byte) (len - 1); m >= 0; m--) {
                buff2[(short) (buff2Off + m)] = a[(short) (aOff + m)];
            }

            // Clean buffers
            for (short i = 0; i < allowedLen; i++) {
                buff4[(short) (buff4Off + i)] = (byte) 0x00;
                buff3[(short) (buff3Off + i)] = (byte) 0x00;
            }

            // Populate with the correct placement multiplier as num2
            for (short i = 0; i < allowedLen; i++) {
                buff[(short) (buffOff + i)] = conversionList1[(short) (conversionList1IndexOffset[pIndex] + i)];
            }

            // while(num2 <= num1)
            while (binArrayElementsCompare(buff, buffOff, buff2, buff2Off, (short) 8) < 1) {
                // num2 <<= 1
                shiftLeft(buff, buffOff, (short) 8, buff, buffOff, (short) 1);

                // temp <<= 1
                shiftLeft(buff1, buff1Off, (short) 8, buff1, buff1Off, (short) 1);
            }

            // while(temp>1)
            while (binArrayElementsCompare(buff1, buff1Off, ONE, (short) 0, (short) 8) == 1) {
                // num2 >>= 1
                shiftRight(buff, buffOff, (short) 8, buff, buffOff, (short) 1);

                // temp >>= 1
                shiftRight(buff1, buff1Off, (short) 8, buff1, buff1Off, (short) 1);

                // if(num1 >= num2)
                if (binArrayElementsCompare(buff2, buff2Off, buff, buffOff, (short) 8) >= 0) {
                    // num1 -= num2
                    int64Subtract(buff2, buff2Off, buff, buffOff, buff2, buff2Off, (short) 8);

                    // result += temp
                    int64Add(buff4, buff4Off, buff1, buff1Off, buff4, buff4Off, (short) 8);
                }
            }

            // Subtract current result using previous result
            int64Subtract(buff2, buff2Off, buff3, buff3Off, buff4, buff4Off, (short) 8);

            // Set current num1 in buff2 to buff3 as cache
            for (short i = 0; i < allowedLen; i++) {
                buff3[(short) (buff3Off + i)] = buff2[buff2Off + i];
            }

            // Set subtraction result as current num1 and cleanup result
            for (short i = 0; i < allowedLen; i++) {
                buff2[(short) (buff2Off + i)] = buff4[buff4Off + i];
                buff4[(short) (buff4Off + i)] = (byte) 0x00;
            }

            // Divide by current divisor
            // Populate with the correct placement multiplier as num2
            for (short i = 0; i < allowedLen; i++) {
                buff[(short) (buffOff + i)] = conversionList1[(short) (conversionList1IndexOffset[(short) (pIndex - 1)]
                        + i)];
                if (i == 7) {
                    buff1[(short) (buff1Off + i)] = (byte) 0x01;
                } else {
                    buff1[(short) (buff1Off + i)] = (byte) 0x00;
                }
            }

            // while(num2 <= num1)
            while (binArrayElementsCompare(buff, buffOff, buff2, buff2Off, (short) 8) < 1) {
                // num2 <<= 1
                shiftLeft(buff, buffOff, (short) 8, buff, buffOff, (short) 1);

                // temp <<= 1
                shiftLeft(buff1, buff1Off, (short) 8, buff1, buff1Off, (short) 1);
            }

            // while(temp>1)
            while (binArrayElementsCompare(buff1, buff1Off, ONE, (short) 0, (short) 8) == 1) {
                // num2 >>= 1
                shiftRight(buff, buffOff, (short) 8, buff, buffOff, (short) 1);

                // temp >>= 1
                shiftRight(buff1, buff1Off, (short) 8, buff1, buff1Off, (short) 1);

                // if(num1 >= num2)
                if (binArrayElementsCompare(buff2, buff2Off, buff, buffOff, (short) 8) >= 0) {
                    // num1 -= num2
                    int64Subtract(buff2, buff2Off, buff, buffOff, buff2, buff2Off, (short) 8);

                    // result += temp
                    int64Add(buff4, buff4Off, buff1, buff1Off, buff4, buff4Off, (short) 8);
                }
            }

            result[(short) (rOff + allowedLen - 1 - (pIndex - 1))] = buff4[(short) (buff4Off + 7)];

            pIndex++;
        }

        if (pIndex > 1) {
            for (short i = 0; i < allowedLen; i++) {
                buff4[(short) (buff4Off + i)] = (byte) 0x00;
                if ((result[(short) (rOff + i)] == (byte) 0x00 && hasNonZeroWritten)
                        || (result[(short) (rOff + i)] != (byte) 0x00)) {
                    buff4[(short) (buff4Off + i)] = (byte) (0x30 | result[(short) (rOff + i)]);
                    hasNonZeroWritten = true;
                    writeLen++;
                }
            }
            if (writeLen == 0) {
            	// All zeroes result shall return a ASCII '0' (0x30)
				result[rOff] = (byte) 0x30;
				return 1;
            } else {
                for (short i = 0; i < writeLen; i++) {
                    result[(short) (rOff + i)] = buff4[(short) (buff4Off + allowedLen - writeLen + i)];
                }
            }
        }

        return writeLen;
    }
}