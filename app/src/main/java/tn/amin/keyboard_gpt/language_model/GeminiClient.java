package tn.amin.keyboard_gpt.language_model;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;

import org.reactivestreams.Publisher;

import java.util.ArrayList;

import tn.amin.keyboard_gpt.language_model.publisher.GeminiPublisherWrapper;

public class GeminiClient extends LanguageModelClient {
    @Override
    public Publisher<String> submitPrompt(String prompt) {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return MISSING_API_KEY_PUBLISHER;
        }

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.15f;
        configBuilder.topK = 32;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        ArrayList<SafetySetting> safetySettings = new ArrayList<>();
//        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
//        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE));
//        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE));
//        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE));

        GenerativeModel gm = new GenerativeModel(
                getSubModel(),
                getApiKey(),
                configBuilder.build(),
                safetySettings
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        Publisher<GenerateContentResponse> publisher = model.generateContentStream(content);
        return new GeminiPublisherWrapper(publisher);
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Gemini;
    }
}
