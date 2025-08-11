package tn.amin.keyboard_gpt.listener;

import android.os.Bundle;

import tn.amin.keyboard_gpt.llm.LanguageModel;

public interface ConfigInfoProvider {
    LanguageModel getLanguageModel();

    Bundle getConfigBundle();

    Bundle getOtherSettings();
}
