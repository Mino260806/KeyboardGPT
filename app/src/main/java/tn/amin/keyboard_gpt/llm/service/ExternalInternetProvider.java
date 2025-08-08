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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.external.InternetService;
import tn.amin.keyboard_gpt.external.InternetServiceMessageType;
import tn.amin.keyboard_gpt.llm.internet.InternetProvider;

public class ExternalInternetProvider extends AbstractServiceClient implements InternetProvider {
    private boolean handlerRunning = false;

    public ExternalInternetProvider(Context context) {
        super(context,
                "tn.amin.keyboard_gpt.INTERNET_SERVICE",
                "tn.amin.keyboard_gpt");
    }

    private long lastRequestId = -1;

    private Map<Long, InternetRequestSubscriber> listeners = new HashMap<>();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Queue<Bundle> messageQueue = new LinkedList<>();

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
        requestBundle.putString("request_method", requestMethod);
        requestBundle.putString("request_body", body);

        listeners.put(lastRequestId, new InternetRequestSubscriber(irl));
        sendMessage(requestBundle, InternetService.SEND_REQUEST_WHAT);

        return Objects.requireNonNull(listeners.get(lastRequestId)).is;
    }

    private void serviceHandler() {
        handlerRunning = true;
        Bundle message;
        while ((message = messageQueue.poll()) != null) {
            handleServiceMessage(message, message.getInt("what"));
        }
        handlerRunning = false;
    }

    private void handleServiceMessage(Bundle message, int what) {
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
                            MainHook.log("Done reading, closing outputStream");

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

    @Override
    protected void onServiceMessage(Bundle message, int what) {
        message.putInt("what", what);
        messageQueue.add(message);
        if (!handlerRunning) {
            executor.execute(this::serviceHandler);
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
            irs.os.write((chunk + System.lineSeparator()).getBytes());
            irs.os.flush();
        } catch (IOException e) {
            MainHook.log(e);
        }
        MainHook.log("Done handling chunk \"" + chunk + "\"");
    }

    private class InternetRequestSubscriber {
        public final InternetRequestListener irl;
        public InputStream is;
        public OutputStream os;

        private InternetRequestSubscriber(InternetRequestListener irl) throws IOException {
            this.irl = irl;
            PipedInputStream pipedInputStream = new PipedInputStream();
            this.is = pipedInputStream;
            this.os = new PipedOutputStream(pipedInputStream);
        }
    }
}
