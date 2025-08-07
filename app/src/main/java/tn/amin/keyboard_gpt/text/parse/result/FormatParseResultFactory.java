package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

import tn.amin.keyboard_gpt.text.transform.format.ConversionMethod;

public class FormatParseResultFactory implements ParseResultFactory {
    private final ConversionMethod conversionMethod;

    public FormatParseResultFactory(ConversionMethod conversionMethod) {
        this.conversionMethod = conversionMethod;
    }

    @Override
    public ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd) {
        return new FormatParseResult(groups, indexStart, indexEnd, conversionMethod);
    }
}
