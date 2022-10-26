package org.thothtrust.sc.t104.demo.ewallet.client.android;

import androidx.annotation.MainThread;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import android.os.Build;
import android.os.Bundle;

import org.json.JSONException;
import org.thothtrust.sc.t104.demo.ewallet.client.android.R;
import org.thothtrust.sc.t104.demo.ewallet.client.android.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.APDUResult;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.SCAPDU;
import org.thothtrust.sc.t104.demo.ewallet.client.android.utils.BinUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;

public class MainActivity extends AppCompatActivity {

    private static Context targetActivity = null;
    private Button getCardInfoBtn = null;
    private TextView cardBalDataLbl = null;
    private TextView cardPaymentDataLbl = null;
    private TextView cardLoadedDataLbl = null;
    private TextView cardStatusLbl = null;
    private TableLayout table = null;
    private NfcAdapter nfcAdapter = null;
    private PendingIntent pendingIntent = null;
    private IntentFilter[] intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
    private String[][] techListsArray = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()}, new String[]{IsoDep.class.getName()}};
    private IsoDep iso14443 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate() T104EWallet Demo OK ....... <<<");
        setContentView(R.layout.activity_main);
        setTargetRedirectIntent(this);
        cardBalDataLbl = (TextView) findViewById(R.id.cardBalDataLbl);
        cardPaymentDataLbl = (TextView) findViewById(R.id.cardPaymentDataLbl);
        cardLoadedDataLbl = (TextView) findViewById(R.id.cardLoadedDataLbl);
        cardStatusLbl = (TextView) findViewById(R.id.cardStatusLbl);
        table = (TableLayout) findViewById(R.id.cardTxHistTbl);
        NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        cardStatusLbl.setText(getResources().getString(R.string.NotifyTapToView));
        System.out.println("T104EWallet Demo onCreate OK ....... <<<");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume() T104EWallet Demo OK ....... <<<");
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            cardStatusLbl.setText(getResources().getString(R.string.NotifyTapToView));
            System.out.println("NFC Foreground Dispatch for T104EWallet Demo OK ....... <<<");
        } else {
            System.err.println("NFC Foreground Dispatch for T104EWallet Demo FAILED ....... <<<");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(this);
            } catch (IllegalStateException ex) {
                Log.e("EWALTAG", "Error disabling NFC foreground dispatch", ex);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch(menuId) {
            case R.id.VendingMachineMode:
                changeViewToVendingMachineMode();
                break;
            case R.id.MerchantTerminalMode:
                changeViewToMerchantTerminalMode();
                break;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        System.out.println("NFC Intent for T104EWallet Demo NFC reading STARTED ....... <<<");

        // Use NFC to read tag
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        boolean hasIsoDep = false;

        if (tag != null) {
            for (String tagTech : tag.getTechList()) {
                System.out.println(" ++++ Found tech: " + tagTech);
                if (tagTech.equals("android.nfc.tech.IsoDep")) {
                    hasIsoDep = true;
                }
            }
        } else {
            SharedResource.popup("No tags found", this);
        }

        if (hasIsoDep) {
            try {
                cardStatusLbl.setText(getResources().getString(R.string.NotifyCardReading));
                iso14443 = IsoDep.get(tag);
                iso14443.connect();
                SharedResource.setIsoDep(iso14443);
                APDUResult apduRes = SCAPDU.selectApplet(iso14443);
                if (apduRes.isSuccess()) {
                    // Enquire balance funds
                    short balance = getFunds(iso14443, Constants.FUND_TYPE_BALANCE);
                    System.out.println("Balance Found. Balance: " + balance);
                    if (balance != -1) {
                        cardBalDataLbl.setText(Short.toString(balance));
                    } else {
                        cardBalDataLbl.setText(getResources().getString(R.string.LblUnknown));
                    }

                    // Enquire payment funds
                    short payment = getFunds(iso14443, Constants.FUND_TYPE_PAYMENT);
                    System.out.println("Payment Found. Payment: " + payment);
                    if (payment != -1) {
                        cardPaymentDataLbl.setText(Short.toString(payment));
                    } else {
                        cardPaymentDataLbl.setText(getResources().getString(R.string.LblUnknown));
                    }

                    // Enquire loaded funds
                    short loaded = getFunds(iso14443, Constants.FUND_TYPE_LOADED);
                    System.out.println("Loaded Funds Found. Loaded: " + loaded);
                    if (loaded != -1) {
                        cardLoadedDataLbl.setText(Short.toString(loaded));
                    } else {
                        cardLoadedDataLbl.setText(getResources().getString(R.string.LblUnknown));
                    }

                    // Fetch logs
                    byte[][] logs = getLogs(iso14443);
                    int rows = table.getChildCount();

                    // Clean up table
                    if (rows > 1) {
                        table.removeViewsInLayout(1, rows - 1);
                    }

                    // Populate table
                    int logCtr = 0;
                    TextView dtView;
                    TextView entryTypeView;
                    TextView contentView;
                    TableRow row;
                    for (byte[] logEntry : logs) {
                        if (logEntry != null) {
                            byte logType = logEntry[0];
                            byte[] logTS = new byte[5];
                            byte[] logContent = new byte[logEntry.length - 6];
                            System.arraycopy(logEntry, 1, logTS, 0, 5);
                            System.arraycopy(logEntry, 6, logContent, 0, logContent.length);
                            String logTypeStr = "";
                            String logTimestampStr = "";
                            String logContentStr = "";
                            if (logType == (byte) 0x01) {
                                logTypeStr = "GENERAL";
                            } else if (logType == (byte) 0x02) {
                                logTypeStr = "FUNDS  ";
                            }
                            logTimestampStr = (int) (logTS[0] & 0xFF) + "/" + (int) (logTS[1] & 0xFF)
                                    + "/" + (int) (logTS[2] & 0xFF) + "-" + (int) (logTS[3] & 0xFF) + ":"
                                    + (int) (logTS[4] & 0xFF);
                            logContentStr = BinUtils.toAsciiString(logContent);

                            System.out.println(
                                    "<<< Entry :: " + logTypeStr + " :: " + logTimestampStr + " :: " + logContentStr);

                            dtView = new TextView(this);
                            entryTypeView = new TextView(this);
                            contentView = new TextView(this);
                            row = new TableRow(this);
                            dtView.setText(logTimestampStr);
                            entryTypeView.setText(logTypeStr);
                            contentView.setText(logContentStr);
                            row.addView(dtView);
                            row.addView(entryTypeView);
                            row.addView(contentView);
                            table.addView(row);
                            logCtr++;
                        }
                    }
                    cardStatusLbl.setText(getResources().getString(R.string.NotifyCardRemove));
                } else {
                    Log.e("EWALTAG", "Not a valid EWallet Demo card", null);
                    SharedResource.popup("Not a valid EWallet Demo card", this);
                }
                iso14443.close();
            } catch (InvalidSizeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
//            Log.e("EWALTAG", "Not an ISO-14443 tag", null);
            SharedResource.popup("Not an ISO-14443 tag", this);
        }
    }

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

    public static short getFunds(IsoDep iso14443, byte fundType) throws InvalidSizeException, IOException {
        APDUResult res = SCAPDU.rawEnquireFunds(iso14443, fundType);
        if (res != null) {
            if (res.isSuccess()) {
                return new BigInteger(res.getResult()).shortValue();
            }
        }
        return -1;
    }

    protected void changeViewToVendingMachineMode() {
        Intent intent = new Intent(MainActivity.this, VendingMachineActivity.class);
        startActivity(intent);
    }

    protected void changeViewToMerchantTerminalMode() {
        Intent intent = new Intent(MainActivity.this, MerchantActivity.class);
        startActivity(intent);
    }

    public static void setTargetRedirectIntent(Context activityCtx) {
        targetActivity = activityCtx;
    }
}