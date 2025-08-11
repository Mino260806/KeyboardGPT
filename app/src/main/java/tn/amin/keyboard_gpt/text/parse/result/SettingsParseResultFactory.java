package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

public class SettingsParseResultFactory implements ParseResultFactory {
    public SettingsParseResultFactory() {
    }

    @Override
    public ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd) {
        return new SettingsParseResult(groups, indexStart, indexEnd);
    }
}
