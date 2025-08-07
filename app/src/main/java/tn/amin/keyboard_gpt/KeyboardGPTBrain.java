package tn.amin.keyboard_gpt;

import android.content.Context;

import tn.amin.keyboard_gpt.listener.GenerativeAIListener;
import tn.amin.keyboard_gpt.listener.InputEventListener;
import tn.amin.keyboard_gpt.text.TextParser;
import tn.amin.keyboard_gpt.text.parse.result.AIParseResult;
import tn.amin.keyboard_gpt.text.parse.result.FormatParseResult;
import tn.amin.keyboard_gpt.text.parse.result.ParseResult;
import tn.amin.keyboard_gpt.text.transform.format.TextUnicodeConverter;

public class KeyboardGPTBrain implements InputEventListener, GenerativeAIListener {
    private final GenerativeAIController mAIController;
//    private final InstructionTreater mInstructionTreater;
    private final TextParser mTextParser;

    public KeyboardGPTBrain(Context context) {
        IMSController.getInstance().addListener(this);

        mAIController = new GenerativeAIController();
        mAIController.addListener(this);
//        mInstructionTreater = new InstructionTreater(mSPManager, mInteracter, mAIController);
        mTextParser = new TextParser();
    }

//    public boolean consumeText(String text) {
//        return mInstructionTreater.isInstruction(text);
//    }
//
//    public boolean performCommand() {
//        return mInstructionTreater.treat("");
//    }

    @Override
    public void onTextUpdate(String text, int cursor) {
        MainHook.log("[IMSController] User typed \"" + text + "\"");

        IMSController imsController = UiInteractor.getInstance().getIMSController();
        ParseResult result = mTextParser.parse(text, cursor);
        if (result != null) {
            MainHook.log("indexEnd(" + result.indexEnd + "), cursor (" + cursor + ")");
            if (result.indexEnd == cursor) {
                int deleteCount = result.indexEnd - result.indexStart;

                imsController.stopNotifyInput();
                imsController.delete(deleteCount);
                imsController.startNotifyInput();

                processParsedText(text, result);
            }
        }
    }

    public void processParsedText(String text, ParseResult parseResult) {
        IMSController imsController = UiInteractor.getInstance().getIMSController();
        if (parseResult instanceof FormatParseResult) {
            FormatParseResult formatParseResult = (FormatParseResult) parseResult;
            String newText = TextUnicodeConverter.convert(formatParseResult.target, formatParseResult.conversionMethod);

            imsController.stopNotifyInput();
            imsController.commit(newText);
            imsController.startNotifyInput();
        } else if (parseResult instanceof AIParseResult) {
            AIParseResult aiParseResult = (AIParseResult) parseResult;
            if (aiParseResult.prompt.isEmpty()) {
                UiInteractor.getInstance().showChoseModelDialog();
            } else {
                new Thread(() ->
                        mAIController.generateResponse(aiParseResult.prompt)).start();
            }
        }
    }

    @Override
    public void onAIPrepare() {
        MainHook.log("[Brain] ONPREPARE");
        IMSController.getInstance().stopNotifyInput();
    }

    @Override
    public void onAINext(String chunk) {
        MainHook.log("[Brain] ONNEXT");
        IMSController.getInstance().flush();
        IMSController.getInstance().commit(chunk);
    }

    @Override
    public void onAIError(Throwable t) {
        MainHook.log("[Brain] ONERROR");
        IMSController.getInstance().startNotifyInput();
    }

    @Override
    public void onAIComplete() {
        MainHook.log("[Brain] ONCOMPLETE");
        IMSController.getInstance().startNotifyInput();
    }
}
