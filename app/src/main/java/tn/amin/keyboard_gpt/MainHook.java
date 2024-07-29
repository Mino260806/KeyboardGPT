package tn.amin.keyboard_gpt;

import android.app.Application;
import android.app.Instrumentation;
import android.inputmethodservice.InputMethodService;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.ai.client.generativeai.BuildConfig;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    KeyboardGPTBrain brain;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.isFirstApplication) {
            return;
        }

        if (lpparam.packageName.equals("tn.amin.keyboard_gpt")) {
            MainHook.log("Not hooking own module");
            return;
        }

        MainHook.log("KeyboardGPT version " + BuildConfig.VERSION_NAME);
        switch (lpparam.packageName) {
            case "com.google.android.inputmethod.latin":
            case "com.touchtype.swiftkey":
            case "com.syntellia.fleksy.keyboard":
            default:
                hookKeyboardWithEditText(lpparam, InstructionTrigger.EditTextClear);
                break;
            case "com.samsung.android.honeyboard":
                hookKeyboardWithEditText(lpparam, InstructionTrigger.LineBreak);
                break;
        }
    }

    private void hookKeyboardWithEditText(XC_LoadPackage.LoadPackageParam lpparam, InstructionTrigger trigger) {
        XposedHelpers.findAndHookMethod(TextView.class, "sendBeforeTextChanged",
                CharSequence.class, int.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject instanceof EditText) {
                    CharSequence text = (CharSequence) param.args[0];

//                    log("sendBeforeTextChanged \"" + text + "\"");
                    if (trigger == InstructionTrigger.LineBreak) {
                        EditText editText = (EditText) param.thisObject;
                        brain.setEditText(editText);
                    }

                    if (brain.consumeText(String.valueOf(text))) {
                        param.setResult(null);
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(TextView.class, "sendOnTextChanged",
                CharSequence.class, int.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject instanceof EditText) {
                    CharSequence text = (CharSequence) param.args[0];

//                    log("sendOnTextChanged \"" + text + "\"");
                    if (brain.consumeText(String.valueOf(text))) {
                        param.setResult(null);
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(TextView.class, "sendAfterTextChanged",
                Editable.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject instanceof EditText) {
                    Editable text = (Editable) param.args[0];

//                    log("sendAfterTextChanged \"" + text + "\"");
                    if (brain.consumeText(String.valueOf(text))) {
                        param.setResult(null);
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(InputMethodService.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                MainHook.log("InputMethodService onCreate");
                brain.onInputMethodCreate((InputMethodService) param.thisObject);
            }
        });

        XposedHelpers.findAndHookMethod(InputMethodService.class, "onDestroy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                MainHook.log("InputMethodService onDestroy");
                brain.onInputMethodDestroy((InputMethodService) param.thisObject);
            }
        });

        XposedHelpers.findAndHookMethod(Instrumentation.class, "callApplicationOnCreate",
                Application.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Application app = (Application) param.args[0];
                        brain = new KeyboardGPTBrain(app.getApplicationContext());
                    }
                });

        switch (trigger) {
            case EditTextClear:
                hookKeyboardWithClearText(lpparam);
                break;
            case LineBreak:
                hookKeyboardWithLineBreak(lpparam);
                break;
        }
    }

    private void hookKeyboardWithClearText(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.thisObject instanceof EditText) {
                            EditText editText = (EditText) param.thisObject;
                            CharSequence text = (CharSequence) param.args[0];

                            log("setText \"" + text + "\"");
                            if (brain.consumeText(String.valueOf(text))) {
                                param.setResult(null);
                            }

                            if (text == null || text.equals("")) {
                                brain.setEditText(editText);
                                if (brain.performCommand()) {
                                    param.setResult(null);
                                }
                            }
                        }
                    }
                });
    }

    private void hookKeyboardWithLineBreak(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.inputmethodservice.RemoteInputConnection", lpparam.classLoader,
                "sendKeyEvent", KeyEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        KeyEvent event = (KeyEvent) param.args[0];
                        MainHook.log("sendKeyEvent " + event);
                        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (brain.performCommand()) {
                                param.setResult(null);
                            }
                        }
                    }
                });
    }

    public static void logST() {
        XposedBridge.log(Log.getStackTraceString(new Throwable()));
    }

    public static void log(String message) {
        XposedBridge.log("(KeyboardGPT) " + message);
    }

    public static void log(Throwable t) {
        XposedBridge.log(t);
    }
}
