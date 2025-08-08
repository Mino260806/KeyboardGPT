package tn.amin.keyboard_gpt.external;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternetService extends Service {
    public static final int SEND_REQUEST_WHAT = 2608;
    public static final int REQUEST_RESULT_WHAT = 2609;

    private final Handler incomingHandler = new Handler(new Handler.Callback() {
        private void handleMessageAsync(Messenger replyMessenger, Bundle requestBundle, int what) {
            Log.d("LSPosed-Bridge", "(KeyboardGPT) [External] Received new message what" + what);
            if (what == SEND_REQUEST_WHAT) {
                long requestId = requestBundle.getLong("request_id");
                URL url = (URL) requestBundle.getSerializable("url");
                //noinspection unchecked
                HashMap<String, List<String>> headers = (HashMap<String, List<String>>)
                        requestBundle.getSerializable("request_headers");
                String method = requestBundle.getString("request_method");
                String body = requestBundle.getString("request_body");

                if (url == null)
                    throw new IllegalArgumentException("url cannot be null");

                try {
                    Log.d("LSPosed-Bridge", "(KeyboardGPT) [External] Opening connection");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    if (headers != null) {
                        applyHeaders(headers, con);
                    }

                    if (method != null) {
                        con.setRequestMethod(method);
                    }

                    if (body != null) {
                        con.setDoOutput(true);
                        try (OutputStream os = con.getOutputStream()) {
                            byte[] input = body.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }

                        int responseCode = con.getResponseCode();
                        replyMessenger.send(craftStatusCodeReply(requestId, responseCode));

                        Log.d("LSPosed-Bridge", "(KeyboardGPT) [External] Got response code " + responseCode);
                        InputStream is = responseCode == 200 ?
                                con.getInputStream() :
                                con.getErrorStream();

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                Log.d("LSPosed-Bridge", "(KeyboardGPT) [External] Sending line with length " + line.length());
                                replyMessenger.send(craftInputStreamReply(requestId, line));
                            }
                        }

                        replyMessenger.send(craftCompleteReply(requestId));
                    }

                } catch (Throwable t) {
                    Log.e("LSPosed-Bridge", "Unexpected error while processing request", t);
//                    Toast.makeText(InternetService.this, t.getClass().getName() + ":" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Messenger messenger = msg.replyTo;
            Bundle data = msg.getData();
            int what = msg.what;
            new Thread(() -> handleMessageAsync(messenger, data, what)).start();
            return true;
        }
    });

    private final Messenger serviceMessenger = new Messenger(incomingHandler);

    private static Message craftReply(long requestId, InternetServiceMessageType type) {
        Message reply = Message.obtain(null, REQUEST_RESULT_WHAT);
        Bundle data = new Bundle();
        data.putLong("request_id", requestId);
        data.putSerializable("message_type", type);
        reply.setData(data);
        return reply;
    }

    private static Message craftStatusCodeReply(long requestId, int statusCode) {
        Message reply = craftReply(requestId, InternetServiceMessageType.STATUS_CODE);
        reply.getData().putInt("status_code", statusCode);
        return reply;
    }

    private static Message craftInputStreamReply(long requestId, String chunk) {
        Message reply = craftReply(requestId, InternetServiceMessageType.INPUT_STREAM);
        reply.getData().putString("inputstream_chunk", chunk);
        return reply;
    }

    private static Message craftCompleteReply(long requestId) {
        return craftReply(requestId, InternetServiceMessageType.COMPLETE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    public static HashMap<String, List<String>> extractHeaders(URLConnection conn) {
        HashMap<String, List<String>> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : conn.getRequestProperties().entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                headers.put(key, new ArrayList<>(entry.getValue()));
            }
        }
        return headers;
    }

    public static void applyHeaders(Map<String, List<String>> headers, URLConnection conn) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                conn.addRequestProperty(entry.getKey(), value);
            }
        }
    }
}
