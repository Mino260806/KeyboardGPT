package tn.amin.keyboard_gpt.llm.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class LlmService extends Service {
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        LlmService getService() {
            return LlmService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public String getMessage() {
        return "[" + getPackageName() + "] Hello from LlmService";
    }
}
