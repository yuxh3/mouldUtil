package application;

import android.app.Application;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public class BaseApplication extends Application {
    private static BaseApplication application = new BaseApplication();

    private BaseApplication(){};

    public static BaseApplication getInstance(){
        return application;
    }
}
