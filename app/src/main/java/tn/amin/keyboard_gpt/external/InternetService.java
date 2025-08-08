package tn.amin.keyboard_gpt.external;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

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

    private final Handler incomingHandler = new Handler(msg -> {
        if (msg.what == SEND_REQUEST_WHAT) {
            Messenger replyMessenger = msg.replyTo;

            Bundle requestBundle = msg.getData();
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
                    replyMessenger.send(craftStatusCodeReply(responseCode));

                    InputStream is = responseCode == 200 ?
                            con.getInputStream() :
                            con.getErrorStream();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            replyMessenger.send(craftInputStreamReply(line));

                        }
                    }

                    replyMessenger.send(craftCompleteReply());
                }

            } catch (Throwable t) {
                Toast.makeText(this, t.toString(), Toast.LENGTH_SHORT);
            }

        }
        return true;
    });

    private final Messenger serviceMessenger = new Messenger(incomingHandler);

    private static Message craftReply(InternetServiceMessageType type) {
        Message reply = Message.obtain(null, REQUEST_RESULT_WHAT);
        Bundle data = new Bundle();
        data.putSerializable("message_type", type);
        reply.setData(data);
        return reply;
    }

    private static Message craftStatusCodeReply(int statusCode) {
        Message reply = craftReply(InternetServiceMessageType.STATUS_CODE);
        reply.getData().putInt("status_code", statusCode);
        return reply;
    }

    private static Message craftInputStreamReply(String chunk) {
        Message reply = craftReply(InternetServiceMessageType.INPUT_STREAM);
        reply.getData().putString("inputstream_chunk", chunk);
        return reply;
    }

    private static Message craftCompleteReply() {
        return craftReply(InternetServiceMessageType.COMPLETE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    public static HashMap<String, List<String>> extractHeaders(URLConnection conn) {
        HashMap<String, List<String>> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
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
