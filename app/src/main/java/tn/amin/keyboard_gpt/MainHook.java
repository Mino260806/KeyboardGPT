package tn.amin.keyboard_gpt;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.util.Log;
import android.view.inputmethod.EditorInfo;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import tn.amin.keyboard_gpt.hook.HookManager;
import tn.amin.keyboard_gpt.hook.MethodHook;
import tn.amin.keyboard_gpt.ui.IMSController;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class MainHook implements IXposedHookLoadPackage {
    private static Context applicationContext = null;

    private KeyboardGPTBrain brain;

    private HookManager hookManager;

    private Class<?> inputConnectionClass = null;

    private Class<?> inputMethodServiceClass = null;

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
        hookKeyboard(lpparam);
    }

    private void ensureInitialized(Context applicationContext) {
        if (MainHook.applicationContext == null) {
            MainHook.applicationContext = applicationContext;
            SPManager.init(applicationContext);
            UiInteractor.init(applicationContext);

            brain = new KeyboardGPTBrain(applicationContext);
        }
    }

    private void hookKeyboard(XC_LoadPackage.LoadPackageParam lpparam) {
        hookManager = new HookManager();

        XposedHelpers.findAndHookMethod(InputMethodService.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                MainHook.log("InputMethodService onCreate");
                InputMethodService ims = (InputMethodService) param.thisObject;

                UiInteractor.getInstance().onInputMethodCreate(ims);

                inputMethodServiceClass = ims.getClass();
                MainHook.log("InputMethodService : " + inputMethodServiceClass.getName());

                hookMethodService();
            }
        });

        XposedHelpers.findAndHookMethod(InputMethodService.class, "onDestroy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                MainHook.log("InputMethodService onDestroy");
                InputMethodService ims = (InputMethodService) param.thisObject;
                UiInteractor.getInstance().onInputMethodDestroy(ims);
            }
        });

        XposedHelpers.findAndHookMethod(InputMethodService.class, "onFinishInput", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                MainHook.log("InputMethodService onFinishInput");
            }
        });

        XposedHelpers.findAndHookMethod(Instrumentation.class, "callApplicationOnCreate",
                Application.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Application app = (Application) param.args[0];
                        ensureInitialized(app.getApplicationContext());
                    }
                });
    }

    private void hookMethodService() {
        hookManager.hook(inputMethodServiceClass, "onUpdateSelection",
                new Class<?>[]{ int.class, int.class, int.class, int.class, int.class, int.class },
                MethodHook.after(param -> {
                    String packageName = UiInteractor.getInstance().getIMS().getCurrentInputEditorInfo().packageName;
                    if (BuildConfig.APPLICATION_ID.equals(packageName)) {
                        return;
                    }
                    IMSController.getInstance().onUpdateSelection(
                            (int) param.args[0],
                            (int) param.args[1],
                            (int) param.args[2],
                            (int) param.args[3],
                            (int) param.args[4],
                            (int) param.args[5]
                    );
                }));

        hookManager.hook(inputMethodServiceClass, "onStartInput",
                new Class<?>[] { EditorInfo.class, boolean.class } , MethodHook.after(param -> {
                hookManager.unhook(m -> m.getClass().equals(inputConnectionClass));

                MainHook.log("InputMethodService onStartInput");
                InputMethodService ims = (InputMethodService) param.thisObject;

                inputMethodServiceClass = ims.getClass();
                inputConnectionClass = ims.getCurrentInputConnection().getClass();
                MainHook.log("InputMethodService InputConnection : " + inputConnectionClass.getName());

                hookInputConnection();
        }));
        MainHook.log("Done hooking InputMethodService : " + inputMethodServiceClass.getName());
    }

    @SuppressLint("ObsoleteSdkInt")
    private void hookInputConnection() {
        XC_MethodHook conditionalGate = MethodHook.before(param -> {
            if (IMSController.getInstance().isInputLocked()) {
                param.setResult(false);
            }
        });

        hookManager.hook(inputConnectionClass, "commitText",
                new Class<?>[] { CharSequence.class, int.class }, conditionalGate);
        hookManager.hook(inputConnectionClass, "commitCorrection",
                new Class<?>[] { android.view.inputmethod.CorrectionInfo.class }, conditionalGate);
        hookManager.hook(inputConnectionClass, "commitCompletion",
                new Class<?>[] { android.view.inputmethod.CompletionInfo.class }, conditionalGate);
        hookManager.hook(inputConnectionClass, "setComposingText",
                new Class<?>[] { CharSequence.class, int.class }, conditionalGate);
        hookManager.hook(inputConnectionClass, "finishComposingText",
                new Class<?>[] {}, conditionalGate);
        hookManager.hook(inputConnectionClass, "deleteSurroundingText",
                new Class<?>[] { int.class, int.class }, conditionalGate);

        if (Build.VERSION.SDK_INT >= 24) {
            hookManager.hook(inputConnectionClass, "deleteSurroundingTextInCodePoints",
                    new Class<?>[] { int.class, int.class }, conditionalGate);
        }
        if (Build.VERSION.SDK_INT >= 33) {
            hookManager.hook(inputConnectionClass, "commitText",
                    new Class<?>[] { CharSequence.class, int.class,
                            android.view.inputmethod.TextAttribute.class }, conditionalGate);
        }
        if (Build.VERSION.SDK_INT >= 34) {
            hookManager.hook(inputConnectionClass, "replaceText",
                    new Class<?>[] { int.class, int.class, CharSequence.class, int.class,
                            android.view.inputmethod.TextAttribute.class }, conditionalGate);
        }

        MainHook.log("Done hooking InputConnection : " + inputConnectionClass.getName());
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    public static void logST() {
        log(Log.getStackTraceString(new Throwable()));
    }

    public static void log(String message) {
        if (!SPManager.isReady() || SPManager.getInstance().getEnableLogs()) {
            XposedBridge.log("(KeyboardGPT) " + message);
        }
    }

    public static void log(Throwable t) {
        XposedBridge.log(t);

        UiInteractor.getInstance().post(() ->
                UiInteractor.getInstance().toastLong(t.getClass().getSimpleName() + " : " + t.getMessage()));
    }
}
