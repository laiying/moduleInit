package com.strod.modulea;

import android.app.Application;
import android.util.Log;

import com.strod.moduleinit.annotation.ModuleInit;
import com.strod.moduleinit.api.core.IModuleInit;

/**
 * Created by laiying on 2021/4/27.
 */
@ModuleInit(priority = 1)
public class AModuleApplication implements IModuleInit {
    @Override
    public void init(Application application) {
        Log.i("AModuleApplication", "init()...");
    }

    @Override
    public void initDelay(Application application) {
        Log.i("AModuleApplication", "initDelay()...");
    }
}
