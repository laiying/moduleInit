package com.strod.moduleinit.api.core;

import android.app.Application;

/**
 * Created by laiying on 20-6-11.
 */
public interface IModuleInit {
    /**
     * Application中onCreate()方法调用,使各业务模块初始化
     * @param application
     */
    void init(Application application);

    /**
     * MainActivity中onCreate()方法调用加载,延时
     * @param application
     */
    void initDelay(Application application);
}
