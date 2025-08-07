package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.UiInteractor;

public abstract class AbstractCommand {
    abstract public String getCommandPrefix();
}
