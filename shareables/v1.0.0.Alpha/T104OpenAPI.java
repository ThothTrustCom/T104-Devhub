package KM104;

import javacard.framework.Shareable;

public interface T104OpenAPI extends Shareable {
    public static final short MAX_OBJ_PER_CRED = (short) 100;
    public static final short MAX_AOC = (short) 10;
    public static final short MAX_LIST_OBJ_CNT = (short) 10;
    public static final short MAX_KMAC_LEN = (short) 128;
    public static final short MAX_GEN_MAT_LEN = MAX_KMAC_LEN;
    public static final short MAX_PIN_LEN = (short) 64;
    public static final short MAX_WALLET_DATA_LEN = (short) 9;
    public static final short MAX_DISPLAY_DATA_LEN = (short) 7;
    public static final short AUTH_TICKET_LEN = (short) 10;
    public static final byte OBJ_TYPE_GENERIC = (byte) 0x01;
    public static final byte OBJ_TYPE_CRED = (byte) 0x02;
    public static final byte OBJ_TYPE_KEY = (byte) 0x03;
    public static final byte OBJ_FIELD_VER = (byte) 0x01;
    public static final byte OBJ_FIELD_TYPE = (byte) 0x02;
    public static final byte OBJ_FIELD_SUBTYPE_CLASS = (byte) 0x03;
    public static final byte OBJ_FIELD_SUBTYPE_TYPE = (byte) 0x04;
    public static final byte OBJ_FIELD_SN = (byte) 0x05;
    public static final byte OBJ_FIELD_CREATE = (byte) 0x06;
    public static final byte OBJ_FIELD_EXPIRE = (byte) 0x07;
    public static final byte OBJ_FIELD_ID = (byte) 0x08;
    public static final byte OBJ_FIELD_EXPORT = (byte) 0x09;
    public static final byte OBJ_FIELD_ACL = (byte) 0x0A;
    public static final byte OBJ_FIELD_MATERIAL_SECRET = (byte) 0x0B;
    public static final byte OBJ_FIELD_MATERIAL_PUBLIC = (byte) 0x0C;
    public static final byte OBJ_FIELD_ATTEST = (byte) 0x0D;
    public static final byte OBJ_FIELD_HANDLE = (byte) 0x0E;
    public static final byte OBJ_EXPIRED = (byte) 0x01;
    public static final byte OBJ_NOT_EXPIRED = (byte) 0x02;
    public static final byte OBJ_EXPIRY_UNDETERMINED = (byte) 0x03;
    public static final byte OBJ_PERM_EXPORT_ALLOW_FLAG = (byte) 0xE0;
    public static final byte OBJ_PERM_EXPORT_DENY_FLAG = (byte) 0xEF;
    public static final byte LOOKUP_AVAILABLE_AOC = (byte) 0x01;
    public static final byte LOOKUP_HAS_FREE_AOC = (byte) 0x02;
    public static final byte LOOKUP_AVAILABLE_CRED_BY_NAME = (byte) 0x03;
    public static final byte LOOKUP_AVAILABLE_CRED_BY_ID = (byte) 0x04;
    public static final byte LOOKUP_HAS_FREE_CRED = (byte) 0x05;
    public static final byte LOOKUP_HAS_FREE_OBJ = (byte) 0x06;
    public static final byte LOOKUP_AVAILABLE_OBJ_BY_HANDLE = (byte) 0x07;
    public static final byte LOOKUP_LIST_OBJ = (byte) 0x08;
    public static final byte LOOKUP_COUNT_OBJ_AOC = (byte) 0x09;
    public static final byte LOOKUP_COUNT_OBJ_GLOBCRED = (byte) 0x0A;
    public static final byte LOOKUP_LIST_CRED = (byte) 0x0B;
    public static final byte KEY_CLASS_SYMMETRIC = (byte) 0x01;
    public static final byte KEY_CLASS_ASYMMETRIC = (byte) 0x02;
    public static final byte KEY_TYPE_AES = (byte) 0x11;
    public static final byte KEY_TYPE_DES = (byte) 0x12;
    public static final byte KEY_TYPE_CHACHA = (byte) 0x13;
    public static final byte KEY_TYPE_RSA = (byte) 0x21;
    public static final byte KEY_TYPE_DH = (byte) 0x22;
    public static final byte KEY_TYPE_ECC_P256R1 = (byte) 0x23;
    public static final byte KEY_TYPE_ECC_P384R1 = (byte) 0x24;
    public static final byte KEY_TYPE_ECC_P521R1 = (byte) 0x25;
    public static final byte KEY_TYPE_ECC_P256K1 = (byte) 0x26;
    public static final byte KEY_TYPE_KMAC = (byte) 0x51;
    public static final byte CRED_AUTHTYPE_PIN = (byte) 0x80;
    public static final byte CRED_AUTHTYPE_PWD = (byte) 0x40;
    public static final byte CRED_AUTHTYPE_FP = (byte) 0x20;
    public static final byte CRED_AUTHTYPE_ASYMMKEY = (byte) 0x10;
    public static final byte CRED_AOC_ADMIN_RESET_FLAG = (byte) 0x01;
    public static final byte CRED_PERM_ADMIN_FLAG = (byte) 0x01;
    public static final byte CRED_FIELD_NAME = (byte) 0x01;
    public static final byte CRED_FIELD_IDPUBKEY = (byte) 0x02;
    public static final byte CRED_FIELD_SECRET = (byte) 0x03;
    public static final byte CRED_FIELD_EXPORT = (byte) 0x04;
    public static final byte CRED_FIELD_ACTIVE = (byte) 0x05;
    public static final byte CRED_FIELD_MANAGEMENT = (byte) 0x06;
    public static final byte CRED_FIELD_CREDID = (byte) 0x07;
    public static final byte CRED_FIELD_ADMIN = (byte) 0x08;
    public static final byte CRED_FIELD_SECRET_TYPE = (byte) 0x09;
    public static final byte CRED_FIELD_MAX_RETRIES = (byte) 0x0A;
    public static final byte CRED_FIELD_RETAIN_ORPHAN = (byte) 0x0B;
    public static final byte CRED_FIELD_CREATE = (byte) 0x0C;
    public static final byte CRED_FIELD_EXPIRE = (byte) 0x0D;
    public static final byte CRED_FIELD_ATTEST = (byte) 0x0E;
    public static final byte CRED_FIELD_OID = (byte) 0x0F;
    public static final byte CRED_FIELD_OBJCTR = (byte) 0x10;
    public static final byte CRED_STAT_INACTIVE = (byte) 0x00;
    public static final byte CRED_STAT_INIT = (byte) 0x01;
    public static final byte CRED_STAT_ACTIVE = (byte) 0x02;
    public static final byte AOCS_NONE = (byte) 0x00;
    public static final byte AOCS_LOGIN_BEGIN = (byte) 0x01;
    public static final byte AOCS_READY = (byte) 0x05;
    public static final byte ACT_AOC_MGMT = (byte) 0x01;
    public static final byte ACT_USR_MGMT = (byte) 0x02;
    public static final byte ACT_USR_FIND = (byte) 0x03;
    public static final byte ACT_USR_SIZE = (byte) 0x04;
    public static final byte ACT_USR_CREATE = (byte) 0x05;
    public static final byte ACT_USR_UPDATE = (byte) 0x06;
    public static final byte ACT_USR_DELETE = (byte) 0x07;
    public static final byte ACT_STE_AUTH = (byte) 0x01;
    public static final byte ACT_STE_CRYPT = (byte) 0x02;
    public static final byte ACT_STE_PROC = (byte) 0x03;
    public static final byte ACT_STATUS_BEGIN = (byte) 0x01;
    public static final byte ACT_STATUS_UPDATE = (byte) 0x02;
    public static final byte ACT_STATUS_FINAL = (byte) 0x03;
    public static final byte ACT_STATUS_CANCEL = (byte) 0x04;
    public static final byte ACT_STATUS_SUCCESS = (byte) 0x05;
    public static final byte ACT_STATUS_FAIL = (byte) 0x06;
    public static final byte CRYPT_LOAD = (byte) 0x01;
    public static final byte CRYPT_UPDATE = (byte) 0x02;
    public static final byte CRYPT_FINAL = (byte) 0x03;
    public static final byte CRYPT_RESET = (byte) 0x04;
    public static final byte CRYPT_KEYGEN = (byte) 0x05;
    public static final byte AUTH_INTERNAL = (byte) 0x02;
    public static final byte AUTH_MODE_USR_AUTH = (byte) 0x01;
    public static final byte AUTH_MODE_CONTAINER_AUTH = (byte) 0x02;
    public static final byte AUTH_MODE_STATE_AUTH = (byte) 0x03;
    public static final byte EXEC_COMPARE = (byte) 0x11;
    public static final byte EXEC_CRYPT_INTEGRITY_CREATION = (byte) 0x21;
    public static final byte EXEC_CRYPT_INTEGRITY_VERIFICATION = (byte) 0x22;
    public static final byte EXEC_CRYPT_CONTENT_PROTECT = (byte) 0x23;
    public static final byte EXEC_CRYPT_CONTENT_EXTRACT = (byte) 0x24;
    public static final byte ATTEST_LEVEL_ROOT_AUTH = (byte) 0x00;
    public static final byte ATTEST_LEVEL_INTERMEDIATE_AUTH = (byte) 0x01;
    public static final byte ATTEST_LEVEL_KM_AUTH = (byte) 0x02;
    public static final byte ATTEST_LEVEL_GLOBUSER_AUTH = (byte) 0x03;
    public static final byte ATTEST_LEVEL_AOC_AUTH = (byte) 0x04;
    public static final byte ATTEST_LEVEL_OBJECT = (byte) 0x05;
    public static final byte ACL_ALLOW_READ = (byte) 0x02;
    public static final byte ACL_ALLOW_EDIT = (byte) 0x01;
    public static final byte ACL_ALLOW_EXEC = (byte) 0x00;
    public static final byte NULL = ACL_ALLOW_EXEC;
    public static final byte WALLET_BALANCE_RECORD_SLOT = (byte) 0x00; // confirmed
    public static final byte WALLET_PAYMENT_RECORD_SLOT = (byte) 0x10; // confirmed
    public static final byte WALLET_LOADING_RECORD_SLOT = (byte) 0x20; // confirmed
    public static final short SW_CARD_NOT_READY = 0x6f1f;
    public static final short SW_INVALID_USER_ROLE = 0x6fa1;
    public static final short SW_INVALID_USER_PIN = 0x6fa2;
    public static final short SW_INVALID_USER_ACCESS_RIGHTS = 0x6fa3;
    public static final short SW_NO_MORE_RETRIES = 0x63c0;
    public static final short SW_UI_ERR = (short) 0x6fb0;

    public short appLogin(byte[] param, short paramOffset, short paramLen);

    public boolean createAOCContainer(byte secretType, byte[] secret, short secretOffset, short secretLen,
            short maxRetry);

    public boolean destroyAOCContainer();

    public boolean manageAOCContainer(byte fieldType, byte[] input, short offset, short len, short maxRetry,
            byte[] ticket, short ticketOff);

    public short getAOCInfo(byte[] output, short outOffset);

    public short getCardInfo(byte[] output, short outOffset);

    public void displayToScreen(byte[] input, short off, short len, byte[] ticket, short ticketOff);

    public short setGlobalWalletAmount(byte displayWalletRecordSlot, byte[] balance, short balanceOff, short balanceLen,
            byte[] payment, short paymentOff, short paymentLen, byte[] loading, short loadingOff, short loadingLen,
            byte[] ticket, short ticketOff);

    public short getGlobalWalletAmount(byte type, byte[] output, short off, byte[] ticket, short ticketOff);

}