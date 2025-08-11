package tn.amin.keyboard_gpt;

import tn.amin.keyboard_gpt.listener.ConfigChangeListener;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class SPUpdater implements ConfigChangeListener {
    private final SPManager mSPManager;

    public SPUpdater() {
        UiInteractor.getInstance().registerConfigChangeListener(this);

        mSPManager = SPManager.getInstance();
    }

    @Override
    public void onLanguageModelChange(LanguageModel model) {
        mSPManager.setLanguageModel(model);
    }

    @Override
    public void onLanguageModelFieldChange(LanguageModel model, LanguageModelField field, String value) {
        mSPManager.setLanguageModelField(model, field, value);
    }

    @Override
    public void onCommandsChange(String commandsRaw) {
        mSPManager.setGenerativeAICommandsRaw(commandsRaw);
    }

    @Override
    public void onPatternsChange(String patternsRaw) {
        mSPManager.setParsePatternsRaw(patternsRaw);
    }
}
