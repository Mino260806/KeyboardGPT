package tn.amin.keyboard_gpt.text.transform.format;

import java.nio.charset.StandardCharsets;

public abstract class ConversionMethod {
    public static final ConversionMethod BOLD = new ShiftConversionMethod( 0x1D5EE);
    public static final ConversionMethod ITALIC = new ShiftConversionMethod( 0x1D622);
    public static final ConversionMethod CROSSOUT = new AddConversionMethod( 0x336);
    public static final ConversionMethod UNDERLINE = new AddConversionMethod( 0x35F).withFilter(Character::isLetterOrDigit);

    private CharacterFilter filter = null;

    public ConversionMethod withFilter(CharacterFilter filter) {
        this.filter = filter;
        return this;
    }

    public abstract String doConvert(char c);

    public final String convert(char c) {
        if (filter == null || filter.filterCharacter(c)) {
            return doConvert(c);
        }
        return String.valueOf(c);
    }

    static boolean isAscii(char c) {
        return (StandardCharsets.US_ASCII.newEncoder().canEncode(c)) &&
                Character.isLetter(c);
    }
}