package tn.amin.keyboard_gpt.text.transform.format;

public class AddConversionMethod extends ConversionMethod {
    private int mOffset;
    public AddConversionMethod(int offset) {
        mOffset = offset;
    }

    @Override
    public String doConvert(char c) {
        if (Character.isSpaceChar(c))
            return String.valueOf(c);
        return new StringBuilder()
                .append(c)
                .append((char)mOffset)
                .toString();
    }
}