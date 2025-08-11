package tn.amin.keyboard_gpt.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.text.parse.ParsePattern;
import tn.amin.keyboard_gpt.text.parse.result.CommandParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.AIParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.ParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.SettingsParseResultFactory;
import tn.amin.keyboard_gpt.text.transform.format.ConversionMethod;
import tn.amin.keyboard_gpt.text.parse.ParseDirective;
import tn.amin.keyboard_gpt.text.parse.result.FormatParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.ParseResult;

public class TextParser {
    private final List<ParseDirective> directives = new ArrayList<>();

    public TextParser(List<ParsePattern> parsePatterns) {
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
}
