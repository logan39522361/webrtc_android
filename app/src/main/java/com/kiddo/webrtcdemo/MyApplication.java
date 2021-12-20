package com.kiddo.webrtcdemo;

import com.kiddo.webrtcdemo.core.exception.MyExceptHandler;

public class MyApplication extends android.app.Application {

    private static MyApplication app;
    private static String pkName;

    public static MyApplication getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        pkName = this.getPackageName();//包名

        MyExceptHandler handler = MyExceptHandler.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    public static String getPkName() {
        return pkName;
    }
}

