package tn.amin.keyboard_gpt.llm.service;

import android.content.Context;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.external.InternetService;
import tn.amin.keyboard_gpt.external.InternetServiceMessageType;
import tn.amin.keyboard_gpt.llm.internet.InternetProvider;

public class ExternalInternetProvider extends AbstractServiceClient implements InternetProvider {
    public ExternalInternetProvider(Context context) {
        super(context,
                "tn.amin.keyboard_gpt.INTERNET_SERVICE",
                "tn.amin.keyboard_gpt");
    }

    private long lastRequestId = -1;

    private Map<Long, InternetRequestSubscriber> listeners = new HashMap<>();

    @Override
    public InputStream sendRequest(HttpURLConnection con, String body, InternetRequestListener irl) throws IOException {
        lastRequestId++;

        URL url = con.getURL();
        HashMap<String, List<String>> headers = InternetService.extractHeaders(con);
        String requestMethod = con.getRequestMethod();

        Bundle requestBundle = new Bundle();
        requestBundle.putSerializable("request_id", lastRequestId);
        requestBundle.putSerializable("url", url);
        requestBundle.putSerializable("request_headers", headers);
        requestBundle.putSerializable("request_method", headers);
        requestBundle.putString("request_body", body);

        listeners.put(lastRequestId, new InternetRequestSubscriber(irl));
        sendMessage(requestBundle, InternetService.SEND_REQUEST_WHAT);

        return Objects.requireNonNull(listeners.get(lastRequestId)).is;
    }

    @Override
    protected void onServiceMessage(Bundle message, int what) {
        if (what != InternetService.REQUEST_RESULT_WHAT) {
            return;
        }
        if (message.containsKey("request_id")) {
            long requestId = message.getLong("request_id");
            InternetRequestSubscriber irs = listeners.get(requestId);
            if (irs != null) {
                InternetServiceMessageType responseType = (InternetServiceMessageType)
                        message.getSerializable("message_type");
                if (responseType != null) {
                    int statusCode;
                    String chunk;
                    switch (responseType) {
                        case STATUS_CODE:
                            statusCode = message.getInt("status_code");
                            irs.irl.onRequestStatusCode(statusCode);
                            break;
                        case INPUT_STREAM:
                            chunk = message.getString("inputstream_chunk");
                            handleInputStreamChunk(chunk, irs);
                            break;
                        case COMPLETE:
                            irs.irl.onRequestComplete();
                            disposeIrs(irs);
                            listeners.remove(requestId);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void disposeIrs(InternetRequestSubscriber irs) {
        try {
            if (irs.os != null) {
                irs.os.close();
                irs.os = null;
            }
            if (irs.is != null) {
                irs.is.close();
                irs.is = null;
            }
        } catch (IOException e) {
            MainHook.log(e);
        }

    }

    private void handleInputStreamChunk(String chunk, InternetRequestSubscriber irs) {
        try {
            if (irs.is == null) {
                PipedInputStream pipedInputStream = new PipedInputStream();
                irs.is = pipedInputStream;
                irs.os = new PipedOutputStream(pipedInputStream);
            }

            irs.os.write(chunk.getBytes());
        } catch (IOException e) {
            MainHook.log(e);
        }
    }

    private class InternetRequestSubscriber {
        public final InternetRequestListener irl;
        public InputStream is = null;
        public OutputStream os = null;

        private InternetRequestSubscriber(InternetRequestListener irl) throws IOException {
            this.irl = irl;
            PipedInputStream pipedInputStream = new PipedInputStream();
            this.is = pipedInputStream;
            this.os = new PipedOutputStream(pipedInputStream);
        }
    }
}
