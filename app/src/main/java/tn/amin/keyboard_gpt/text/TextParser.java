package tn.amin.keyboard_gpt.text;

import java.util.List;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.text.parse.result.CommandParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.AIParseResultFactory;
import tn.amin.keyboard_gpt.text.transform.format.ConversionMethod;
import tn.amin.keyboard_gpt.text.parse.ParseDirective;
import tn.amin.keyboard_gpt.text.parse.result.FormatParseResultFactory;
import tn.amin.keyboard_gpt.text.parse.result.ParseResult;

public class TextParser {
    private Pattern patternBold = Pattern.compile("¿([^¿]+)¿$");
    private Pattern patternItalic = Pattern.compile("¡([^¡]+)¡$");
    private Pattern patternCrossout = Pattern.compile("~([^~]+)~$");
    private Pattern patternUnderline = Pattern.compile("ū([^~]+)ū$");
    private Pattern patternAI = Pattern.compile("\\$([^$]*)\\$$");
    private Pattern patternAICommand = Pattern.compile("©(?:([^ ©]+) *)?([^©]+)?©$");

    private List<ParseDirective> directives = List.of(
            new ParseDirective(patternBold, new FormatParseResultFactory(ConversionMethod.BOLD)),
            new ParseDirective(patternItalic, new FormatParseResultFactory(ConversionMethod.ITALIC)),
            new ParseDirective(patternCrossout, new FormatParseResultFactory(ConversionMethod.CROSSOUT)),
            new ParseDirective(patternUnderline, new FormatParseResultFactory(ConversionMethod.UNDERLINE)),
            new ParseDirective(patternAI, new AIParseResultFactory()),
            new ParseDirective(patternAICommand, new CommandParseResultFactory())
    );

    public TextParser() {
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
