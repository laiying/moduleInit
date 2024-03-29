package com.strod.moduleinit.api;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.strod.moduleinit.annotation.ModuleInit;
import com.strod.moduleinit.api.core.IModuleInit;
import com.strod.moduleinit.api.core.ModuleInitRoot;
import com.strod.moduleinit.api.core.ModuleWareHouse;
import com.strod.moduleinit.api.exception.ModuleHandlerException;
import com.strod.moduleinit.api.utils.ClassUtils;
import com.strod.moduleinit.api.utils.ConfigConstants;
import com.strod.moduleinit.api.utils.PackageUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by laiying on 20-6-17.
 */
public class ModuleInitManager {

    private static final String TAG = "ModuleInit";

    private static final ModuleInitManager ourInstance = new ModuleInitManager();

    public static ModuleInitManager getInstance() {
        return ourInstance;
    }

    private ModuleInitManager() {
    }

    private volatile static boolean hasInit = false;
    private volatile static boolean debuggable = false;
    private static boolean registerByPlugin;

    public static synchronized void openDebug() {
        debuggable = true;
    }

    public static boolean debuggable() {
        return debuggable;
    }

    public synchronized void init(Application application){
        if (!hasInit) { //确保只初始化一次
            Log.i(TAG, "ModuleInit init start.");
            hasInit = _init(application);
            Log.i(TAG, "ModuleInit init over.");
        }
    }

    private boolean _init(Application application){
        load(application);
        return true;
    }

    private void load(Application context) throws ModuleHandlerException {
        try {
            long startInit = System.currentTimeMillis();

            loadRouterMap();
            if (registerByPlugin) {
                Log.i(TAG, "Load router map by moduleinit-auto-register plugin.");
            } else {
                Set<String> routerMap;

                // It will rebuild router map every times when debuggable.
                if (debuggable() || PackageUtils.isNewVersion(context)) {
                    Log.i(TAG, "Run with debug mode or new install, rebuild module map.");
                    // These class was generated by arouter-compiler.
                    routerMap = ClassUtils.getFileNameByPackageName(context, ConfigConstants.MODULE_INIT_ROOT_PAKCAGE);
                    if (!routerMap.isEmpty()) {
                        context.getSharedPreferences(ConfigConstants.MODULE_INIT_SP_CACHE_KEY, Context.MODE_PRIVATE).edit().putStringSet(ConfigConstants.MODULE_INIT_SP_KEY_MAP, routerMap).apply();
                    }

                    PackageUtils.updateVersion(context);    // Save new version name when router map update finishes.
                } else {
                    Log.i(TAG, "Load router map from cache.");
                    routerMap = new HashSet<>(context.getSharedPreferences(ConfigConstants.MODULE_INIT_SP_CACHE_KEY, Context.MODE_PRIVATE).getStringSet(ConfigConstants.MODULE_INIT_SP_KEY_MAP, new HashSet<String>()));
                }

                Log.i(TAG, "Find router map finished, map size = " + routerMap.size() + ", cost " + (System.currentTimeMillis() - startInit) + " ms.");
                startInit = System.currentTimeMillis();

                for (String className : routerMap) {
                    if (className.startsWith(ConfigConstants.MODULE_INIT_ROOT_PAKCAGE + ConfigConstants.DOT + ConfigConstants.SDK_NAME + ConfigConstants.SEPARATOR + ConfigConstants.SUFFIX_ROOT)) {
                        // This one of root elements, load root.
                        ((ModuleInitRoot) (Class.forName(className).getConstructor().newInstance())).loadInto(ModuleWareHouse.rootsIndex);
                    }
                }

                Log.i(TAG, "Load root element finished, cost " + (System.currentTimeMillis() - startInit) + " ms.");

            }

            if (ModuleWareHouse.rootsIndex.size() == 0) {
                Log.e(TAG, "No mapping files were found, check your configuration please!");
            }else {
                //
                invokeInit(context);
            }

            if (debuggable()) {
                Log.d(TAG, String.format(Locale.getDefault(), "ModuleInit has already been loaded, GroupIndex[%d]", ModuleWareHouse.rootsIndex.size()));
            }

        } catch (Exception e) {
            throw new ModuleHandlerException(TAG + "Module init exception! [" + e.getMessage() + "]");
        }
    }

    private void invokeInit(@Nullable Application application){
        if (ModuleWareHouse.rootsIndex.isEmpty()){
            return;
        }

        for (Map.Entry<Integer, Class<? extends IModuleInit>> entry : ModuleWareHouse.rootsIndex.entrySet()) {
            Class<? extends IModuleInit> moduleInitClass = entry.getValue();
            try {
                IModuleInit iModuleInit = moduleInitClass.getConstructor().newInstance();
                //调用初始化方法
                iModuleInit.init(application);
            } catch (Exception ex) {
                throw new RuntimeException("Configurable init interceptor error! name = [" + moduleInitClass.getName() + "], reason = [" + ex.getMessage() + "]");
            }
        }
    }

    public void invokeInitDelay(@Nullable Application application){
        if (ModuleWareHouse.rootsIndex.isEmpty()){
            return;
        }

        for (Map.Entry<Integer, Class<? extends IModuleInit>> entry : ModuleWareHouse.rootsIndex.entrySet()) {
            Class<? extends IModuleInit> moduleInitClass = entry.getValue();
            try {
                IModuleInit iModuleInit = moduleInitClass.getConstructor().newInstance();
                //调用初始化方法
                iModuleInit.initDelay(application);
            } catch (Exception ex) {
                throw new RuntimeException("Configurable init interceptor error! name = [" + moduleInitClass.getName() + "], reason = [" + ex.getMessage() + "]");
            }
        }
    }

    /**
     * moduleinit-auto-register plugin will generate code inside this method
     * call this method to register all Routers, Interceptors and Providers
     */
    private static void loadRouterMap() {
        registerByPlugin = false;
        //auto generate register code by gradle plugin: moduleinit-auto-register
        // looks like below:
        // registerRouteRoot(new ARouter..Root..modulejava());
        // registerRouteRoot(new ARouter..Root..modulekotlin());
    }

    /**
     * mark already registered by moduleinit-auto-register plugin
     */
    private static void markRegisteredByPlugin() {
        if (!registerByPlugin) {
            registerByPlugin = true;
        }
    }


    /**
     * register by class name
     * Sacrificing a bit of efficiency to solve
     * the problem that the main dex file size is too large
     * @param className class name
     */
    private static void register(String className) {
        if (!TextUtils.isEmpty(className)) {
            try {
                Log.i(TAG, "register className: " + className);
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.getConstructor().newInstance();
                if (obj instanceof ModuleInitRoot) {
                    registerRouteRoot((ModuleInitRoot) obj);
                }  else {
                    Log.i(TAG, "register failed, class name: " + className
                            + " should implements one of ModuleInitRoot.");
                }
            } catch (Exception e) {
                Log.e(TAG,"register class error:" + className);
            }
        }
    }

    /**
     * method for moduleinit-auto-register plugin to register Routers
     * @param moduleInitRoot ModuleInitRoot implementation class in the package: com.strod.moduleinit.api.core.ModuleInitRoot
     */
    private static void registerRouteRoot(ModuleInitRoot moduleInitRoot) {
        markRegisteredByPlugin();
        if (moduleInitRoot != null) {
            moduleInitRoot.loadInto(ModuleWareHouse.rootsIndex);
        }
    }
}
