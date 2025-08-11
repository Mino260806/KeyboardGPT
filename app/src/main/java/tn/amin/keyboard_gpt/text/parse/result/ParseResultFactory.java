package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

import tn.amin.keyboard_gpt.text.parse.PatternType;
import tn.amin.keyboard_gpt.text.transform.format.ConversionMethod;

public interface ParseResultFactory {
    static ParseResultFactory of(PatternType type) {
        switch (type) {
            case CommandAI:
                return new AIParseResultFactory();
            case CommandCustom:
                return new CommandParseResultFactory();
            case FormatBold:
                return new FormatParseResultFactory(ConversionMethod.BOLD);
            case FormatItalic:
                return new FormatParseResultFactory(ConversionMethod.ITALIC);
            case FormatCrossout:
                return new FormatParseResultFactory(ConversionMethod.CROSSOUT);
            case FormatUnderline:
                return new FormatParseResultFactory(ConversionMethod.UNDERLINE);
            case Settings:
            default:
                return new SettingsParseResultFactory();
        }
    }

    ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd);
}
