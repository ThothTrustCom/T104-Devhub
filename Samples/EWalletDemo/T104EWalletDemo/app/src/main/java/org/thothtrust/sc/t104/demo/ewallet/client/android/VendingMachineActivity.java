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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.thothtrust.sc.t104.demo.ewallet.client.android.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.APDUResult;
import org.thothtrust.sc.t104.demo.ewallet.client.android.sc.SCAPDU;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class VendingMachineActivity extends AppCompatActivity {

    private static String[] actions = {"Load Wallet Funds","Reset Wallet Funds"};
    private TextView executionStatusLbl = null;
    private Button executionBtn = null;
    private Spinner actionSpinner = null;
    private EditText loadFundsTf = null;
    private ImageView img = null;
    private LinearLayout controlPanelLayout = null;
    private NfcAdapter nfcAdapter = null;
    private PendingIntent pendingIntent = null;
    private IntentFilter[] intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
    private String[][] techListsArray = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()}, new String[]{IsoDep.class.getName()}};
    private IsoDep iso14443 = null;
    private int toPayAmt = -1;
    private int selectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vending_machine);
        executionBtn = (Button) findViewById(R.id.executeBtn);
        actionSpinner = (Spinner) findViewById(R.id.actionSpinner);
        loadFundsTf = (EditText) findViewById(R.id.loadFundsTf);
        executionStatusLbl = (TextView) findViewById(R.id.executionStatusLbl);
        controlPanelLayout = (LinearLayout) findViewById(R.id.linearLayout2);
        img = (ImageView) findViewById(R.id.imageView2);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,actions);
        actionSpinner.setAdapter(spinnerAdapter);
        NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        executionBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("EWALTAG", "VendingMachineActivity :: onCreate :: onClick <<<");

                // Read spinner for selected action
                selectedItem = actionSpinner.getSelectedItemPosition();

                img.setImageDrawable(null);
                if (selectedItem == 0) {
                    // Load funds to wallet
                    // Read the entered amount
                    try {
                        toPayAmt = Short.valueOf(loadFundsTf.getText().toString());
                        if (toPayAmt > 0 && toPayAmt <= 1000) {
                            // Notify for card to be tapped
                            executionStatusLbl.setText(getResources().getString(R.string.NotifyTapToView));
                            img.setImageDrawable(getResources().getDrawable(R.drawable.ic_nfc_tap));

                            // Disable control panel components to prevent accidental changing of entries
                            transactionComponentsToggleStatus(false);
                        } else {
                            // Invalid range of amount acceptable
                            executionStatusLbl.setText(getResources().getString(R.string.NotifyPaymentValueRangeInvalid));

                            // Reset payment amount in textfield
                            loadFundsTf.setText("");

                            // Reset payment amount
                            toPayAmt = -1;
                        }
                    } catch (NumberFormatException ex) {
                        // Invalid input
                        executionStatusLbl.setText(getResources().getString(R.string.NotifyPaymentValueInvalid));

                        // Reset payment amount in textfield
                        loadFundsTf.setText("");
                    }
                } else if (selectedItem == 1) {
                    // Reset wallet funds
                    // Notify for card to be tapped
                    executionStatusLbl.setText(getResources().getString(R.string.NotifyTapToView));
                    img.setImageDrawable(getResources().getDrawable(R.drawable.ic_nfc_tap));

                    // Disable control panel components to prevent accidental changing of entries
                    transactionComponentsToggleStatus(false);
                }
            }
        });

        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if(actionSpinner.getSelectedItemId() == 0){
                    loadFundsTf.setEnabled(true);
                } else if (actionSpinner.getSelectedItemId() == 1){
                    loadFundsTf.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void transactionComponentsToggleStatus(boolean toEnable) {
        if (toEnable) {
            actionSpinner.setEnabled(true);
            executionBtn.setEnabled(true);
            if (selectedItem == 0 && !loadFundsTf.isEnabled()) {
                loadFundsTf.setEnabled(true);
            }
        } else {
            actionSpinner.setEnabled(false);
            executionBtn.setEnabled(false);
            if (selectedItem == 0 && loadFundsTf.isEnabled()) {
                loadFundsTf.setEnabled(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("VendingMachineActivity :: onResume() <<<");
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            Log.e("EWALTAG", "VendingMachineActivity :: onResume() :: NFC Foreground Dispatch OK <<<");
        } else {
            Log.e("EWALTAG", "VendingMachineActivity :: onResume() :: NFC Foreground Dispatch FAILED <<<");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(this);
            } catch (IllegalStateException ex) {
                Log.e("EWALTAG", "VendingMachineActivity :: onPause() :: Error disabling NFC foreground dispatch FAILED <<<", ex);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (!(selectedItem == 0 || selectedItem == 1)) {
            // Do not proceed if no valid selected item is detected
            return;
        }

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
                executionStatusLbl.setText(getResources().getString(R.string.NotifyCardReading));
                iso14443 = IsoDep.get(tag);
                iso14443.connect();
                SharedResource.setIsoDep(iso14443);
                APDUResult apduRes = SCAPDU.selectApplet(iso14443);
                if (apduRes.isSuccess()) {
                    // Get current timestamp of operation
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    byte DD = (byte) (ts.getDate() & 0xFF);
                    byte MM = (byte) ((ts.getMonth() + 1) & 0xFF);
                    byte YY = (byte) ((ts.getYear() - 100) & 0xFF);
                    byte hh = (byte) (ts.getHours() & 0xFF);
                    byte mm = (byte) (ts.getMinutes() & 0xFF);

                    if (selectedItem == 0) {
                        // Begin loading funds
                        if (toPayAmt > 0) {
                            apduRes = SCAPDU.rawLoadFunds(iso14443, (short) toPayAmt, DD, MM, YY, hh, mm);

                            toPayAmt = -1; // disable back the payment amount
                            loadFundsTf.setText(""); // reset payment amount in textfield

                            if (apduRes.isSuccess()) {
                                img.setImageDrawable(getResources().getDrawable(R.drawable.ic_cash_load_success));
                                executionStatusLbl.setText(getResources().getString(R.string.NotifyLoadFundsSuccessful));
                            } else {
                                executionStatusLbl.setText(getResources().getString(R.string.NotifyLoadFundsFail));
                                img.setImageDrawable(null);
                            }
                        }
                    } else if (selectedItem == 1) {
                        // Begin resetting wallet funds
                        apduRes = SCAPDU.rawResetWalletFunds(iso14443, DD, MM, YY, hh, mm);

                        if (apduRes.isSuccess()) {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.ic_cash_load_success));
                            executionStatusLbl.setText(getResources().getString(R.string.NotifyResetFundsSuccessful));
                        } else {
                            executionStatusLbl.setText(getResources().getString(R.string.NotifyResetFundsFail));
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