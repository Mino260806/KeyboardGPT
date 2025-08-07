package tn.amin.keyboard_gpt.llm;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentResponse;

import org.reactivestreams.Publisher;

import java.util.concurrent.CompletableFuture;

import tn.amin.keyboard_gpt.llm.publisher.GeminiPublisherWrapper;
import tn.amin.keyboard_gpt.llm.publisher.SimpleStringPublisher;

public class GeminiClient extends LanguageModelClient {
    @Override
    public Publisher<String> submitPrompt(String prompt, String systemMessage) {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return MISSING_API_KEY_PUBLISHER;
        }

        if (systemMessage == null) {
            systemMessage = getDefaultSystemMessage();
        }

        Client client = new Client.Builder()
                .apiKey(getApiKey())
                .build();

        ResponseStream<GenerateContentResponse> responseStream =
                client.models.generateContentStream(getSubModel(), prompt, null);

        return new GeminiPublisherWrapper(responseStream);
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Gemini;
    }
}
