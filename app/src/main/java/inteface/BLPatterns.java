package inteface;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public interface BLPatterns {
    String MOBILE = "^1\\d{10}$";
    String PASSWORD = "^(?![0-9]+$)(?![a-zA-Z]+$)[\\\\da-zA-Z]{8,16}$";
}
