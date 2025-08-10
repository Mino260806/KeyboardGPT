package tn.amin.keyboard_gpt.external;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

public abstract class DialogBox {
    private final Context mContext;
    private final AlertDialog mAlertDialog;
    private final Bundle mConfig;

    public DialogBox(@NonNull Context context, Bundle config) {
        mContext = context;
        mAlertDialog = build();
        mConfig = config;
    }
    
    protected abstract AlertDialog build();

    public AlertDialog getDialog() {
        return mAlertDialog;
    }

    public Bundle getConfig() {
        return mConfig;
    }

    public void switchToDialog(DialogBox second) {
        second.getDialog().show();
        getDialog().dismiss();
    }
}
