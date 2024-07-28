package tn.amin.keyboard_gpt.instruction.command;

public class SimpleGenerativeAICommand extends GenerativeAICommand {
    private final String mPrefix;
    private final String mTweakMessage;

    public SimpleGenerativeAICommand(String prefix, String tweakMessage) {
        mPrefix = prefix;
        mTweakMessage = tweakMessage;
    }

    @Override
    public String getCommandPrefix() {
        return mPrefix;
    }

    @Override
    public String getTweakMessage() {
        return mTweakMessage;
    }
}
