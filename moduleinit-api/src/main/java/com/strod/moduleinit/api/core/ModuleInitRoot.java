package com.strod.moduleinit.api.core;

import java.util.Map;

/**
 * Created by laiying on 20-6-16.
 */
public interface ModuleInitRoot {
//    void loadInto(List<String> module);
    void loadInto(Map<Integer, Class<? extends IModuleInit>> module);
}
