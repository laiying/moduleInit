package com.strod.moduleb;

import android.app.Application;
import android.util.Log;

import com.strod.moduleinit.annotation.ModuleInit;
import com.strod.moduleinit.api.core.IModuleInit;

/**
 * Created by laiying on 2021/4/27.
 */
@ModuleInit
public class BModuleApplication implements IModuleInit {
    @Override
    public void init(Application application) {
        Log.i("BModuleApplication", "init()...");
    }

    @Override
    public void initDelay(Application application) {
        Log.i("BModuleApplication", "initDelay()...");
    }
}
