package tn.amin.keyboard_gpt.llm;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;

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

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.15f)
                .topK(32f)
                .topP(1f)
                .maxOutputTokens(4096)
                .safetySettings(
                    SafetySetting.builder().category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                            .threshold(HarmBlockThreshold.Known.OFF).build(),
                    SafetySetting.builder().category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                            .threshold(HarmBlockThreshold.Known.OFF).build(),
                    SafetySetting.builder().category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                            .threshold(HarmBlockThreshold.Known.OFF).build(),
                    SafetySetting.builder().category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                            .threshold(HarmBlockThreshold.Known.OFF).build()
                )
                .systemInstruction(Content.builder().parts(Part.fromText(systemMessage)))
                .build();

        Client client = new Client.Builder()
                .apiKey(getApiKey())
                .build();

        ResponseStream<GenerateContentResponse> responseStream =
                client.models.generateContentStream(getSubModel(), prompt, config);

        return new GeminiPublisherWrapper(responseStream);
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Gemini;
    }
}
