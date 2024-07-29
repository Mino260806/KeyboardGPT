package tn.amin.keyboard_gpt;

import android.os.Bundle;

import tn.amin.keyboard_gpt.language_model.LanguageModel;

public interface ConfigInfoProvider {
    LanguageModel getLanguageModel();

    Bundle getConfigBundle();
}
