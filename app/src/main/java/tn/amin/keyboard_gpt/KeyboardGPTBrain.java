package tn.amin.keyboard_gpt;

import android.content.Context;

import tn.amin.keyboard_gpt.instruction.command.AbstractCommand;
import tn.amin.keyboard_gpt.instruction.command.CommandManager;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.instruction.command.WebSearchCommand;
import tn.amin.keyboard_gpt.listener.DialogDismissListener;
import tn.amin.keyboard_gpt.listener.GenerativeAIListener;
import tn.amin.keyboard_gpt.listener.InputEventListener;
import tn.amin.keyboard_gpt.llm.GenerativeAIController;
import tn.amin.keyboard_gpt.text.TextParser;
import tn.amin.keyboard_gpt.text.parse.result.AIParseResult;
import tn.amin.keyboard_gpt.text.parse.result.CommandParseResult;
import tn.amin.keyboard_gpt.text.parse.result.FormatParseResult;
import tn.amin.keyboard_gpt.text.parse.result.ParseResult;
import tn.amin.keyboard_gpt.text.parse.result.SettingsParseResult;
import tn.amin.keyboard_gpt.text.transform.format.TextUnicodeConverter;
import tn.amin.keyboard_gpt.ui.IMSController;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class KeyboardGPTBrain implements InputEventListener, GenerativeAIListener, DialogDismissListener {
    private final static String STR_GENERATING_CONTENT = "<Generating Content...>";
    private boolean justPrepared = true;

    private final GenerativeAIController mAIController;
    private final CommandManager mCommandManager;
//    private final InstructionTreater mInstructionTreater;
    private final TextParser mTextParser;

    public KeyboardGPTBrain(Context context) {
        IMSController.getInstance().addListener(this);
        UiInteractor.getInstance().registerOnDismissListener(this);

        mAIController = new GenerativeAIController();
        mAIController.addListener(this);
        mTextParser = new TextParser();
        mCommandManager = new CommandManager();
    }

    @Override
    public void onTextUpdate(String text, int cursor) {
//        MainHook.log("[IMSController] User typed \"" + text + "\"");
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
            generateResponse(aiParseResult.prompt, null);
        } else if (parseResult instanceof CommandParseResult) {
            CommandParseResult commandParseResult = (CommandParseResult) parseResult;
            if (commandParseResult.command.isEmpty()) {
                UiInteractor.getInstance().showEditCommandsDialog();
            } else {
                AbstractCommand command = mCommandManager.get(commandParseResult.command);
                if (command instanceof GenerativeAICommand) {
                    GenerativeAICommand genAICommand = (GenerativeAICommand) command;
                    generateResponse(commandParseResult.prompt, genAICommand.getTweakMessage());
                } else if (command instanceof WebSearchCommand){
                    String url = "https://www.google.com/search?q=" + commandParseResult.prompt;
                    UiInteractor.getInstance().showWebSearchDialog("Web Search", url);
                }
            }
        } else if (parseResult instanceof SettingsParseResult) {
            UiInteractor.getInstance().showSettingsDialog();
        }
    }

    private void generateResponse(String prompt, String systemMessage) {
        if (prompt.isEmpty() || mAIController.needModelClient()) {
            if (UiInteractor.getInstance().showChoseModelDialog()) {
                UiInteractor.getInstance().toastLong("Chose and configure your language model");
            }
            return;
        }

        if (mAIController.needApiKey()) {
            if (UiInteractor.getInstance().showChoseModelDialog()) {
                UiInteractor.getInstance().toastLong(mAIController.getLanguageModel().label +
                        " is Missing API Key");
            }
            return;
        }

        new Thread(() -> mAIController.generateResponse(prompt, systemMessage)).start();
    }

    @Override
    public void onAIPrepare() {
        MainHook.log("[Brain] ONPREPARE");
        IMSController.getInstance().flush();
        IMSController.getInstance().commit(STR_GENERATING_CONTENT);

        IMSController.getInstance().stopNotifyInput();
        IMSController.getInstance().startInputLock();

        justPrepared = true;
    }

    private void clearGeneratingContent() {
        if (justPrepared) {
            justPrepared = false;

            IMSController.getInstance().flush();
            IMSController.getInstance().delete(STR_GENERATING_CONTENT.length());
        }
    }

    @Override
    public void onAINext(String chunk) {
        MainHook.log("[Brain] ONNEXT");
        IMSController.getInstance().endInputLock();
        clearGeneratingContent();
        IMSController.getInstance().flush();
        IMSController.getInstance().commit(chunk);
        IMSController.getInstance().startInputLock();
    }

    @Override
    public void onAIError(Throwable t) {
        MainHook.log("[Brain] ONERROR");
        IMSController.getInstance().endInputLock();
        clearGeneratingContent();
        IMSController.getInstance().startNotifyInput();
    }

    @Override
    public void onAIComplete() {
        MainHook.log("[Brain] ONCOMPLETE");
        IMSController.getInstance().endInputLock();
        clearGeneratingContent();
        IMSController.getInstance().startNotifyInput();
    }

    @Override
    public void onDismiss(boolean isPrompt, boolean isCommand) {
        if (isPrompt) {
            MainHook.log("Selected " + mAIController.getLanguageModel());
            UiInteractor.getInstance().post(() -> {
                UiInteractor.getInstance().toastShort("Selected " + mAIController.getLanguageModel()
                        + " (" + mAIController.getModelClient().getSubModel() + ")");
            });
        } else if (isCommand) {
            UiInteractor.getInstance().post(() -> {
                UiInteractor.getInstance().toastShort("New Commands Saved");
            });
        }
    }
}
