package tn.amin.keyboard_gpt.llm.internet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import tn.amin.keyboard_gpt.llm.service.InternetRequestListener;

public interface InternetProvider {
    InputStream sendRequest(HttpURLConnection con, String body, InternetRequestListener irl) throws IOException;
}
