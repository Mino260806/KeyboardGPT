package tn.amin.keyboard_gpt.text.transform.format;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;


public class TextUnicodeConverter {
    private static final HashMap<String, CharUnicodeConverter> mSpecialSymbols = new HashMap<>();
    public static List<Character> importedDelimiters = null;

    public static String convert(String text, ConversionMethod conversionMethod) {
        // Decompose accents
        text = Normalizer.normalize(text, Normalizer.Form.NFD);

        StringBuilder formattedTextBuilder = new StringBuilder();

        CharUnicodeConverter converter = new CharUnicodeConverter(conversionMethod);
        String formattedText;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String formattedChar = converter.convert(c);
            formattedTextBuilder.append(formattedChar);
        }

        formattedText = converter.finishingTouches(formattedTextBuilder);

        return formattedText;
    }

    static class CharUnicodeConverter {
        private ConversionMethod mConversionMethod;
        private CharacterFilter mCharacterFilter = CharacterFilter.noCharacterFilter;

        private boolean mReverse = false;

        CharUnicodeConverter(ConversionMethod method) {
            mConversionMethod = method;
        }

        public CharUnicodeConverter withFilter(CharacterFilter characterFilter) {
            mCharacterFilter = characterFilter;
            return this;
        }

        public CharUnicodeConverter reverse(boolean reverse) {
            mReverse = reverse;
            return this;
        }

        public String convert(char c) {
            // Ignore line feed and carriage return
            if (c == '\n' || c == '\r')
                return String.valueOf(c);
            // CharacterFilter gets executed first
            if (!mCharacterFilter.filterCharacter(c))
                return String.valueOf(c);
            // If CharacterFilter returns NULL_CHAR, (ignores the c), proceed to the universal
            // conversion methods.

            return mConversionMethod.convert(c);
        }

        public String finishingTouches(StringBuilder formattedString) {
            if (mReverse)
                formattedString.reverse();
            return formattedString.toString();
        }
    }

    static {
        mSpecialSymbols.put("*", new CharUnicodeConverter(new ShiftConversionMethod( 0x1D5EE)));
        mSpecialSymbols.put("!", new CharUnicodeConverter(new ShiftConversionMethod( 0x1D622)));
//        mSpecialSymbols.put("~", new CharUnicodeConverter(new AddConversionMethod( 0x336)));
//        mSpecialSymbols.put("_", new CharUnicodeConverter(new AddConversionMethod( 0x35F)).withFilter(c -> {
//            // Non alphanumeric characters do not use the same "y" level for underline
//            // so the underlining line becomes misaligned and ugly. We are obliged to disable
//            // for all non alphanumeric chars as a result. Note also that the "y" level for underlining
//            // numbers is not same as letters, but it's not very important.
//            if (Character.isLetterOrDigit(c))
//                return CharacterFilter.NULL_CHAR; // proceed to normal conversion
//            return c; // keep c as it is
//        }));

//        ArrayList<Character> delimiters = new ArrayList<>();
//        ExternalFormattingReader.readAndExtend(new File(StorageConstants.moduleFiles, "formatting"), (result) -> {
//            mSpecialSymbols.put(result.delimiter.toString(),
//                    new CharUnicodeConverter(new ReplaceConversionMethod(new CharacterTable(result.charMap)))
//                            .reverse(result.options.reverse));
//            delimiters.add(result.delimiter);
//        });
//
//        importedDelimiters = delimiters;
    }

    private static boolean isAscii(char c) {
        return (StandardCharsets.US_ASCII.newEncoder().canEncode(c)) &&
                Character.isLetter(c);
    }
}