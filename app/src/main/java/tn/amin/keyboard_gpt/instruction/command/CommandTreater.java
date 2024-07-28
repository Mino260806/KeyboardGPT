package tn.amin.keyboard_gpt.instruction.command;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.InstructionCategory;
import tn.amin.keyboard_gpt.instruction.TextTreater;

public class CommandTreater implements TextTreater {
    private static final List<AbstractCommand> BUILTIN_COMMANDS = List.of(
            new WebSearchCommand(),
            new SimpleGenerativeAICommand("t", "You are a specialized language model designed to perform mathematical calculations with precision and accuracy. Your task is to interpret math problems, compute the solutions, and output only the result of the computation.\n" +
                    "\n" +
                    "Instructions:\n" +
                    "\n" +
                    "Read and understand the given mathematical problem.\n" +
                    "Perform the necessary calculations.\n" +
                    "Output only the final numerical result, with no additional explanation or text.\n" +
                    "Examples:\n" +
                    "\n" +
                    "Input: What is 5 + 3?\n" +
                    "Output: 8\n" +
                    "\n" +
                    "Input: Calculate the product of 12 and 7.\n" +
                    "Output: 84\n" +
                    "\n" +
                    "Input: Find the square root of 81.\n" +
                    "Output: 9\n" +
                    "\n" +
                    "Input: Evaluate 6^3.\n" +
                    "Output: 216\n" +
                    "\n" +
                    "Input: What is the derivative of 3x^2 + 2x?\n" +
                    "Output: 6x + 2\n" +
                    "\n" +
                    "Input: Integrate x^2 + 3x + 4 with respect to x.\n" +
                    "Output: (1/3)x^3 + (3/2)x^2 + 4x + C\n" +
                    "\n" +
                    "Input: Solve for x: 2x + 3 = 7.\n" +
                    "Output: 2\n" +
                    "\n" +
                    "Input: What is the value of sin(π/2)?\n" +
                    "Output: 1\n" +
                    "\n" +
                    "Remember:\n" +
                    "\n" +
                    "Always ensure calculations are accurate.\n" +
                    "Only print the numerical result or the final mathematical expression.\n" +
                    "No additional text, context, or explanation should be included in the output.\n" +
                    "Now, proceed to perform math calculations based on the given input.\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n")
            );

    private final SPManager mSPManager;
    private final UiInteracter mInteracter;
    private final GenerativeAIController mAIController;

    private final ArrayList<AbstractCommand> mCommands = new ArrayList<>();

    public CommandTreater(SPManager spManager, UiInteracter interacter, GenerativeAIController aiController) {
        mSPManager = spManager;
        mInteracter = interacter;
        mAIController = aiController;

        mCommands.addAll(BUILTIN_COMMANDS);
        mCommands.addAll(mSPManager.getGenerativeAICommands());
    }

    @Override
    public boolean treat(String text) {
        if (text.startsWith(InstructionCategory.Command.prefix)) {
            return mInteracter.showEditCommandsDialog(mSPManager.getGenerativeAICommandsRaw());
        }

        for (AbstractCommand command: BUILTIN_COMMANDS) {
            if (text.startsWith(command.getCommandPrefix())) {
                text = text.substring(command.getCommandPrefix().length()).trim();
                command.consume(text, mInteracter, mAIController);
                return true;
            }
        }

        return false;
    }
}
