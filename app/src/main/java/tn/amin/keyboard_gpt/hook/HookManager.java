package tn.amin.keyboard_gpt.hook;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HookManager {
    private Map<Method, XC_MethodHook.Unhook> unhookMap = new HashMap<>();

    public void hook(Class<?> clazz, String methodName, Class<?>[] paramTypes, XC_MethodHook callback) {
        Method method = XposedHelpers.findMethodBestMatch(clazz, methodName, paramTypes);
        if (!unhookMap.containsKey(method)) {
            unhookMap.put(method, XposedBridge.hookMethod(method, callback));
        }
    }

    public void hookAll(Class<?> clazz, String methodName, XC_MethodHook callback) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (!unhookMap.containsKey(method)) {
                    unhookMap.put(method, XposedBridge.hookMethod(method, callback));
                }
            }
        }
    }

    public void unhook(Predicate<Method> clearPredicate) {
        for (Method method: unhookMap.keySet()) {
            if (clearPredicate.test(method)) {
                unhookMap.remove(method).unhook();
            }
        }
    }
}
