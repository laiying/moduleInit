# moduleInit
### 背景
- Android多模块架构，模块内有些配置等需要在Application的onCreate（）方法进行初始化，例如集中在app模块的Application或者基础业务模块BaseApplication中进行调用。
这样带来一个问题，模块一多，需要调用一大堆各个模块的初始化配置，而且耦合性比较高。
### 常规优化
- 很自然能想到的优化点是:抽出一个IModuleInit接口，里面有init(Application app)和initDelay(Application app)接口方法。
在各个模块分别写一个类实现这个方法，最后在app模块的Application或者基础业务模块BaseApplication中的onCreate（）利用反射对实现IModuleInit接口的类进行调用。
例如
```/**基本业务模块*/
    private static final String BaseInit = "com.strod.library.config.BaseModuleApplication";
    //app主业务模块
    private static final String AppInit = "com.strod.cloud.config.AppModuleApplication";
    /**用户账号业务模块*/
    private static final String AccountInit = "com.strod.user.config.UserModuleApplication";

    public static String[] initModuleNames = {BaseInit, AppInit,AccountInit};
    
    /**
     * 初始化组件
     * @param application
     */
    public void initModule(@Nullable Application application) {
        for (String moduleInitName : initModuleNames) {
            try {
                Class<?> clazz = Class.forName(moduleInitName);
                IModuleApplication init = (IModuleApplication) clazz.newInstance();
                //调用初始化方法
                init.init(application);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
       
    }

    /**
     * 延时初始化组件
     * @param application
     */
    public void initModuleDelay(@Nullable Application application) {
        for (String moduleInitName : initModuleNames) {
            try {
                Class<?> clazz = Class.forName(moduleInitName);
                IModuleApplication init = (IModuleApplication) clazz.newInstance();
                //调用初始化方法
                init.initDelay(application);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    ```
- 但是还有一个缺点，反射的类名还需要手动配置。
### 本库优化
- 本库为的就是优化手动配置的缺点，采用注解方式，利用apt编译时把配置生成，然后在ModuleInitManager类的init（）方法中，开启线程扫描class文件，对apt编译生成的类中的配置收集，然后再反射对实现IModuleInit接口的类进行调用。
整个过程简化到只需要在实现IModuleInit接口的类加入@ModuleInit注解，以及在Application中的onCreate()方法调用ModuleInitManager.getInstance().init(this)就完成。
### 本库终极优化
- 最后还对扫描class文件进行了优化，使用com.strod:moduleinit-plugin:1.0.0 gradle插件，插件进行自动注册可以缩短初始化时间解决应用加固导致无法直接访问 dex 文件，原理还是ASM织入class文件字节码实现。更多详情查看源码
