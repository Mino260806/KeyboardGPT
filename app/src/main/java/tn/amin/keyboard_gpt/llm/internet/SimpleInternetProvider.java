package tn.amin.keyboard_gpt.llm.internet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import tn.amin.keyboard_gpt.llm.service.InternetRequestListener;

public class SimpleInternetProvider implements InternetProvider {
    @Override
    public InputStream sendRequest(HttpURLConnection con, String body, InternetRequestListener irl) throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        irl.onRequestStatusCode(responseCode);

        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputStream.write(line.getBytes());
            }
        }

        return inputStream;
    }
}
