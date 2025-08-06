package tn.amin.keyboard_gpt;

import android.app.Application;
import android.app.Instrumentation;
import android.inputmethodservice.InputMethodService;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private KeyboardGPTBrain brain;

    private InstructionIntercept intercept;

    private InstructionTrigger trigger;

    private String editTextClassLiteral = EditText.class.getName();

    private Class<?> editTextClass = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.isFirstApplication) {
            return;
        }

        if (lpparam.packageName.equals("tn.amin.keyboard_gpt")) {
            MainHook.log("Not hooking own module");
            return;
        }

        MainHook.log("Loading KeyboardGPT for package " + lpparam.packageName);
        switch (lpparam.packageName) {
            case "com.samsung.android.honeyboard":
                intercept = InstructionIntercept.EditTextListener;
                trigger = InstructionTrigger.LineBreak;
                hookKeyboard(lpparam);
                break;
//            case "com.baidu.input":
//                intercept = InstructionIntercept.TextViewSetText;
//                trigger = InstructionTrigger.DownUpKeyEvents;
//                editTextClass = "com.baidu.input.ime.searchservice.editor.SearchEditor";
//                hookKeyboard(lpparam);
//                break;
            case "com.google.android.inputmethod.latin":
            case "com.touchtype.swiftkey":
            case "com.syntellia.fleksy.keyboard":
            default:
                intercept = InstructionIntercept.EditTextListener;
                trigger = InstructionTrigger.EditTextClear;
                hookKeyboard(lpparam);
                break;
        }
    }

    private void hookKeyboard(XC_LoadPackage.LoadPackageParam lpparam) {
        editTextClass = XposedHelpers.findClass(editTextClassLiteral, lpparam.classLoader);

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

        switch (intercept) {
            case EditTextListener:
                hookInterceptEditTextListener(lpparam);
                break;
            case TextViewSetText:
                hookInterceptTextViewSetText(lpparam);
                break;
        }

        switch (trigger) {
            case EditTextClear:
                hookTriggerClearText(lpparam);
                break;
            case LineBreak:
                hookTriggerLineBreak(lpparam);
                break;
            case DownUpKeyEvents:
                hookTriggerDownUpKeyEvent(lpparam);
                break;
        }
    }

    private void hookInterceptTextViewSetText(XC_LoadPackage.LoadPackageParam lpparm) {
        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (editTextClass.isInstance(param.thisObject)) {
//                    log("setText " + param.args[0]);
                    CharSequence text = (CharSequence) param.args[0];

                    if (!trigger.providesEditText) {
                        TextView editText = (TextView) param.thisObject;
                        brain.setEditText(editText);
                    }

                    brain.consumeText(String.valueOf(text));
                }
            }
        });
    }

    private void hookInterceptEditTextListener(XC_LoadPackage.LoadPackageParam lpparm) {
        XposedHelpers.findAndHookMethod(TextView.class, "sendBeforeTextChanged",
                CharSequence.class, int.class, int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (editTextClass.isInstance(param.thisObject)) {
                            CharSequence text = (CharSequence) param.args[0];

//                    log("sendBeforeTextChanged \"" + text + "\"");
                            if (!trigger.providesEditText) {
                                TextView editText = (TextView) param.thisObject;
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
                        if (editTextClass.isInstance(param.thisObject)) {
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
                        if (editTextClass.isInstance(param.thisObject)) {
                            Editable text = (Editable) param.args[0];

//                    log("sendAfterTextChanged \"" + text + "\"");
                            if (brain.consumeText(String.valueOf(text))) {
                                param.setResult(null);
                            }
                        }
                    }
                });
    }

    private void hookTriggerClearText(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (editTextClass.isInstance(param.thisObject)) {
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

    private void hookTriggerLineBreak(XC_LoadPackage.LoadPackageParam lpparam) {
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

    private void hookTriggerDownUpKeyEvent(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(InputMethodService.class, "sendDownUpKeyEvents", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int eventCode = (int) param.args[0];
                MainHook.log("sendDownUpKeyEvents " + eventCode);
                if (eventCode == KeyEvent.KEYCODE_ENTER) {
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
