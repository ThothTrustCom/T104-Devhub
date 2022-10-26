package org.thothtrust.sc.t104.demo.ewallet.client.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.tech.IsoDep;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SharedResource {

    public static IsoDep iso14443 = null;

    public static void setIsoDep(IsoDep iso14443Dev) {
        iso14443 = iso14443Dev;
    }

    public static IsoDep getIsoDep() {
        return iso14443;
    }

    public static PopupWindow pw = null;

    public static void popup(String message, Context context) {
        LinearLayout layout = new LinearLayout(context);
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        params.weight = 1.0f;
        params.gravity = Gravity.CENTER;
        SpannableStringBuilder str = new SpannableStringBuilder(message);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(str);
        tv.setTextSize(24);
        tv.setTextColor(Color.BLUE);
        tv.setGravity(Gravity.CENTER);

        layout.addView(tv, params);
        CountDownTimer timer = new CountDownTimer(3000,10) {
            boolean isShowing = false;
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if(isShowing){
                    //CLOSE
                }
                else{
                    isShowing=true;

                    pw = new PopupWindow(layout, 600, 200, true);
                    pw.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

                    // display the popup in the center
                    pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
                    this.start();
                }
            }
        };
        timer.start();
    }
}