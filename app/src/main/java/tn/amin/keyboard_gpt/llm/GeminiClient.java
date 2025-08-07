package tn.amin.keyboard_gpt.llm;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import org.reactivestreams.Publisher;

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
        GenerateContentResponse response =
                client.models.generateContent(getSubModel(), prompt, null);

        return new SimpleStringPublisher(response.text());
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Gemini;
    }
}
