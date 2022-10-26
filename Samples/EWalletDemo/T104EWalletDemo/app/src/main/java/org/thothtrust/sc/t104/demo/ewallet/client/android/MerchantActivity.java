package org.thothtrust.sc.t104.demo.ewallet.client.android;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.Timestamp;

import org.thothtrust.sc.t104.demo.ewallet.client.android.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.APDUResult;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.SCAPDU;
import org.thothtrust.sc.t104.demo.ewallet.client.android.utils.BinUtils;

import java.io.IOException;

public class MerchantActivity extends AppCompatActivity {

    private TextView paymentStatusLbl = null;
    private Button paymentBtn = null;
    private EditText amountTf = null;
    private ImageView img = null;
    private LinearLayout controlPanelLayout = null;
    private NfcAdapter nfcAdapter = null;
    private PendingIntent pendingIntent = null;
    private IntentFilter[] intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
    private String[][] techListsArray = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()}, new String[]{IsoDep.class.getName()}};
    private IsoDep iso14443 = null;
    private int toPayAmt = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("MerchantActivity :: onCreate <<<");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);
        paymentStatusLbl = (TextView) findViewById(R.id.paymentStatusLbl);
        paymentBtn = (Button) findViewById(R.id.paymentBtn);
        amountTf = (EditText) findViewById(R.id.amountTf);
        img = (ImageView) findViewById(R.id.imageView);
        controlPanelLayout = (LinearLayout) findViewById(R.id.linearLayout);
        NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        paymentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("EWALTAG", "MerchantActivity :: onCreate :: onClick <<<");
                // Read the entered amount

                img.setImageDrawable(null);
                try {
                    toPayAmt = Short.valueOf(amountTf.getText().toString());
                    if (toPayAmt > 0 && toPayAmt <= 1000) {
                        // Notify for card to be tapped
                        paymentStatusLbl.setText(getResources().getString(R.string.NotifyTapToView));
                        img.setImageDrawable(getResources().getDrawable(R.drawable.ic_nfc_tap));

                        // Disable control panel components to prevent accidental changing of entries
                        transactionComponentsToggleStatus(false);
                    } else {
                        // Invalid range of amount acceptable
                        paymentStatusLbl.setText(getResources().getString(R.string.NotifyPaymentValueRangeInvalid));

                        // Reset payment amount in textfield
                        amountTf.setText("");

                        // Reset payment amount
                        toPayAmt = -1;
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input
                    paymentStatusLbl.setText(getResources().getString(R.string.NotifyPaymentValueInvalid));

                    // Reset payment amount in textfield
                    amountTf.setText("");
                }
            }
        });
    }

    private void transactionComponentsToggleStatus(boolean toEnable) {
        if (toEnable) {
            paymentBtn.setEnabled(true);
            amountTf.setEnabled(true);
        } else {
            paymentBtn.setEnabled(false);
            amountTf.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("MerchantActivity :: onResume() <<<");
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            Log.e("EWALTAG", "MerchantActivity :: onResume() :: NFC Foreground Dispatch OK <<<");
        } else {
            Log.e("EWALTAG", "MerchantActivity :: onResume() :: NFC Foreground Dispatch FAILED <<<");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(this);
            } catch (IllegalStateException ex) {
                Log.e("EWALTAG", "MerchantActivity :: onPause() :: Error disabling NFC foreground dispatch FAILED <<<", ex);
            }
        }
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
                paymentStatusLbl.setText(getResources().getString(R.string.NotifyCardReading));
                iso14443 = IsoDep.get(tag);
                iso14443.connect();
                SharedResource.setIsoDep(iso14443);
                APDUResult apduRes = SCAPDU.selectApplet(iso14443);
                if (apduRes.isSuccess()) {
                    if (toPayAmt > 0) {
                        Timestamp ts = new Timestamp(System.currentTimeMillis());
                        byte DD = (byte) (ts.getDate() & 0xFF);
                        byte MM = (byte) ((ts.getMonth() + 1) & 0xFF);
                        byte YY = (byte) ((ts.getYear() - 100) & 0xFF);
                        byte hh = (byte) (ts.getHours() & 0xFF);
                        byte mm = (byte) (ts.getMinutes() & 0xFF);
                        apduRes = SCAPDU.rawPayFunds(iso14443, (short) toPayAmt, DD, MM, YY, hh, mm);

                        toPayAmt = -1; // disable back the payment amount
                        amountTf.setText(""); // reset payment amount in textfield

                        if (apduRes.isSuccess()) {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.ic_payment_success));
                            paymentStatusLbl.setText(getResources().getString(R.string.NotifyPaymentSuccessful));
                        } else {
                            paymentStatusLbl.setText(getResources().getString(R.string.NotifyPaymentFail));
                            img.setImageDrawable(null);
                        }
                    }
                } else {
                    Log.e("EWALTAG", "Not a valid EWallet Demo card", null);
                    SharedResource.popup("Not a valid EWallet Demo card", this);
                }
                iso14443.close();

                // Enable control panel components
                transactionComponentsToggleStatus(true);
            } catch (IOException e) {
                // Enable control panel components
                transactionComponentsToggleStatus(true);
                e.printStackTrace();
            } catch (InvalidSizeException e) {
                // Enable control panel components
                transactionComponentsToggleStatus(true);
                e.printStackTrace();
            }
        } else {
            Log.e("EWALTAG", "Not a valid EWallet Demo card", null);
            SharedResource.popup("Not a valid EWallet Demo card", this);
        }
    }
}