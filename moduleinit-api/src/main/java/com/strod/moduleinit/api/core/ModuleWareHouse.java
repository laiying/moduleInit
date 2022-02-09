package com.strod.moduleinit.api.core;

import java.util.Map;

/**
 * Created by laiying on 20-6-16.
 */
public class ModuleWareHouse {
    public static Map<Integer, Class<? extends IModuleInit>> rootsIndex = new UniqueKeyTreeMap<>("More than one IModuleInits use same priority [%s]");
}
