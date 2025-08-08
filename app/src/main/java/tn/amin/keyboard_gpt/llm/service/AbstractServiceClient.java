package tn.amin.keyboard_gpt.llm.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.LinkedList;
import java.util.Queue;

import tn.amin.keyboard_gpt.MainHook;

public abstract class AbstractServiceClient {
    private Messenger serviceMessenger = null;
    private boolean bound = false;
    private boolean connecting = false;
    public Queue<Bundle> messageQueue = new LinkedList<>();

    private final Context context;
    private final String intentAction;
    private final String intentPackage;

    private final Messenger incomingMessenger = new Messenger(new Handler(Looper.getMainLooper(), msg -> {
        onServiceMessage(msg.getData(), msg.what);
        return true;
    }));

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            connecting = false;

            serviceMessenger = new Messenger(service);
            bound = true;
            MainHook.log("Connected to service");

            while (!messageQueue.isEmpty()) {
                Bundle message = messageQueue.poll();
                int what = message.getInt("what");
                sendMessage(message, what);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceMessenger = null;
            bound = false;
            MainHook.log("Disconnected from service");
        }
    };

    public AbstractServiceClient(Context context, String intentAction, String intentPackage) {
        this.context = context;
        this.intentAction = intentAction;
        this.intentPackage = intentPackage;
    }

    public void connect() {
        if (!connecting) {
            connecting = true;
            Intent intent = new Intent(intentAction);
            intent.setPackage(intentPackage);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void disconnect() {
        if (bound) {
            context.unbindService(connection);
            bound = false;
        }
    }

    public void sendMessage(Bundle message, int what) {
        if (!bound || serviceMessenger == null) {
            if (!connecting) {
                connect();
            }
            queueMessage(message, what);
            return;
        }

        Message msg = Message.obtain(null, what);
        msg.setData(message);
        msg.replyTo = incomingMessenger;
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            MainHook.log(e);
        }
    }

    private void queueMessage(Bundle message, int what) {
        message.putInt("what", what);
        messageQueue.add(message);
    }

    protected abstract void onServiceMessage(Bundle message, int what);
}
