package tn.amin.keyboard_gpt.command;

import tn.amin.keyboard_gpt.UiInteracter;

public abstract class AbstractCommand {
    abstract public String getCommandPrefix();

    abstract public void consume(String text, UiInteracter interacter);
}
