package tn.amin.keyboard_gpt.ui;

public enum DialogType {
    ChoseModel(true),
    SetApiKey(true),
    WebSearch(false),
    ;

    final boolean isConfiguration;

    DialogType (boolean isConfiguration) {
        this.isConfiguration = isConfiguration;
    }
}
