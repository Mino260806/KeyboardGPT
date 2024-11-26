package tn.amin.keyboard_gpt.instruction;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.command.CommandTreater;
import tn.amin.keyboard_gpt.instruction.prompt.PromptTreater;

public class InstructionTreater implements TextTreater {
    private final CommandTreater mCommandTreater;
    private final PromptTreater mPromptTreater;

    public InstructionTreater(SPManager spManager, UiInteracter interacter, GenerativeAIController aiController) {
        mPromptTreater = new PromptTreater(spManager, interacter, aiController);
        mCommandTreater = new CommandTreater(spManager, interacter, aiController);
    }

    public InstructionCategory getInstructionCategory(String text) {
        if (text == null) {
            return InstructionCategory.None;
        }

        for (InstructionCategory category : InstructionCategory.values()) {
            String prefix = category.prefix;
            if (prefix != null) {
                // Check English and Chinese symbols
                // 检查英文和中文符号
                if (text.startsWith(prefix) || text.startsWith(getChineseEquivalent(prefix))) {
                    return category;
                }
            }
        }

        return InstructionCategory.None;
    }
    // A new method is added to get the corresponding Chinese symbols
    // 新增一个方法，用于获取对应的中文符号
    private String getChineseEquivalent(String prefix) {
        switch (prefix) {
            case "?":
                return "？";
            case "!":
                return "！";
            default:
                return prefix;
        }
    }
    // Modify the removeInstructionPrefix method
    // 修改 removeInstructionPrefix 方法
    public String removeInstructionPrefix(String text, InstructionCategory category) {
        if (text == null || category.prefix == null) {
            return null;
        }

        String prefix = category.prefix;
        String chinesePrefix = getChineseEquivalent(prefix);

        if (text.startsWith(prefix)) {
            return text.substring(prefix.length()).trim();
        } else if (text.startsWith(chinesePrefix)) {
            return text.substring(chinesePrefix.length()).trim();
        } else {
            return null;
        }
    }


    public boolean isInstruction(String text) {
        return getInstructionCategory(text) != InstructionCategory.None;
    }

    @Override
    public boolean treat(String text) {
        InstructionCategory category = getInstructionCategory(text);
        String instruction = removeInstructionPrefix(text, category);
        switch (category) {
            case Prompt:
                return mPromptTreater.treat(instruction);
            case Command:
                return mCommandTreater.treat(instruction);
            case None:
                MainHook.log("Aborting performCommand because text is not a valid instruction");
                break;
        }

        return false;
    }
}
