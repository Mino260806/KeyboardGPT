package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

import tn.amin.keyboard_gpt.text.transform.format.ConversionMethod;

public class AIParseResultFactory implements ParseResultFactory {
    public AIParseResultFactory() {
    }

    @Override
    public ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd) {
        return new AIParseResult(groups, indexStart, indexEnd);
    }
}
