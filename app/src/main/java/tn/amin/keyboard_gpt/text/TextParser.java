package tn.amin.keyboard_gpt.text;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.listener.ConfigChangeListener;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;
import tn.amin.keyboard_gpt.text.parse.ParsePattern;
import tn.amin.keyboard_gpt.text.parse.result.CommandParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.AIParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.ParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.SettingsParseResultFactory;
import tn.amin.keyboard_gpt.text.transform.format.ConversionMethod;
import tn.amin.keyboard_gpt.text.parse.ParseDirective;
import tn.amin.keyboard_gpt.text.parse.result.FormatParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.ParseResult;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class TextParser implements ConfigChangeListener {
    private final List<ParseDirective> directives = new ArrayList<>();

    public TextParser() {
        UiInteractor.getInstance().registerConfigChangeListener(this);
        List<ParsePattern> parsePatterns = SPManager.getInstance().getParsePatterns();

        updatePatterns(parsePatterns);
    }

    private void updatePatterns(List<ParsePattern> parsePatterns) {
        directives.clear();
        for (ParsePattern parsePattern: parsePatterns) {
            directives.add(new ParseDirective(parsePattern.getPattern(),
                    ParseResultFactory.of(parsePattern.getType())));
        }
    }

    public ParseResult parse(String text, int cursor) {
        String textBeforeCursor = text.substring(0, cursor);

        for (ParseDirective directive: directives) {
            ParseResult parseResult = directive.parse(textBeforeCursor);
            if (parseResult != null) {
                return parseResult;
            }
        }

        return null;
    }

    @Override
    public void onLanguageModelChange(LanguageModel model) {

    }

    @Override
    public void onLanguageModelFieldChange(LanguageModel model, LanguageModelField field, String value) {

    }

    @Override
    public void onCommandsChange(String commandsRaw) {

    }

    @Override
    public void onPatternsChange(String patternsRaw) {
        updatePatterns(ParsePattern.decode(patternsRaw));
    }

    @Override
    public void onOtherSettingsChange(Bundle otherSettings) {

    }
}
