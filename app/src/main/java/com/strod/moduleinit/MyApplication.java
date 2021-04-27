package com.strod.moduleinit;

import android.app.Application;

import com.strod.moduleinit.api.ModuleInitManager;

/**
 * Created by laiying on 2021/4/27.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ModuleInitManager.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ModuleInitManager.getInstance().init(this);// 尽可能早，推荐在Application中初始化
    }
}
