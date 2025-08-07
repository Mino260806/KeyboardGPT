package tn.amin.keyboard_gpt.hook;

import java.util.function.Consumer;

import de.robv.android.xposed.XC_MethodHook;

public class MethodHook extends XC_MethodHook {
    private final Consumer<MethodHookParam> before;

    private final Consumer<MethodHookParam> after;

    public MethodHook(Consumer<MethodHookParam> before, Consumer<MethodHookParam> after) {
        this.before = before;
        this.after = after;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if (before != null) {
            before.accept(param);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        if (after != null) {
            after.accept(param);
        }
    }

    public static XC_MethodHook after(Consumer<MethodHookParam> after) {
        return new MethodHook(null, after);
    }

    public static XC_MethodHook before(Consumer<MethodHookParam> before) {
        return new MethodHook(before, null);
    }
}
