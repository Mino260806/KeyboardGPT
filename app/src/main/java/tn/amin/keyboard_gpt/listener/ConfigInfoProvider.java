package tn.amin.keyboard_gpt.listener;

import android.os.Bundle;

import tn.amin.keyboard_gpt.llm.client.LanguageModel;

public interface ConfigInfoProvider {
    LanguageModel getLanguageModel();

    Bundle getConfigBundle();
}
