package tn.amin.keyboard_gpt;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
    private final Context mContext;

    public Toaster(Context context) {
        mContext = context;
    }

    public void toastShort(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
