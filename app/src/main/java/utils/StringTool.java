package utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import application.BaseApplication;
import common.AppConst;
import inteface.BLPatterns;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public class StringTool {
    public static boolean isNotNull(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isNotNull(Object obj) {
        if (obj == null || obj.toString().trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isNull(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNull(Object obj) {
        if (obj == null || obj.toString().trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取当前时间 毫秒级
     *
     * @return
     */
    public static java.sql.Date getCurSQLDate() {
        java.util.Date utilDate = new Date();
        return new java.sql.Date(utilDate.getTime());
    }

    /**
     * 将数字类型日期格式化
     *
     * @param timestampString
     * @return
     */
    public static String timeStampDate(String timestampString, String format) {

        try {
            Long timestamp = Long.parseLong(timestampString) * 1000;
            String date = new java.text.SimpleDateFormat(format)
                    .format(new java.util.Date(timestamp));
            return date;
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * 如果小数点后的值大于零(至少是#.10)，则保留一位小数，否则不保留小数部分
     *
     * @param value
     * @return
     */
    public static String formatFloat(float value) {
        if ((int) (value + 0.9f) > (int) value) {
            return new DecimalFormat("#0.0").format(value);
        }
        return (int) value + "";
    }

    /**
     * 检验是否符合正则规则
     *
     * @param str   字符串内容
     * @param regex 正则表达式规则
     * @return
     */
    public static boolean checkRegex(String str, String regex) {
        boolean flag = false;
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            flag = matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 手机号验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        return Pattern.matches(BLPatterns.MOBILE, str);
    }

    /**
     * 电话号码验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isPhone(String str) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$"); // 验证带区号的
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$"); // 验证没有区号的
        if (str.length() > 9) {
            m = p1.matcher(str);
            b = m.matches();
        } else {
            m = p2.matcher(str);
            b = m.matches();
        }
        return b;
    }

    /**
     * 账号长度校验
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean checkUserNameLgh(String str) {
        boolean b = false;
        if (str.length() <= 40 && str.length() >= 4) {
            b = true;
        } else {
            b = false;
        }
        return b;
    }

    public static String getURLDomain(String url) {
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static boolean isEmpty(Object obj) {
        boolean f = false;
        if (obj == null) {
            f = true;
        } else if (obj instanceof TextView) {
            TextView tv = (TextView) obj;
            if (TextUtils.isEmpty(tv.getText())) {
                f = true;
            }
        } else if (obj instanceof EditText) {
            EditText et = (EditText) obj;
            if (TextUtils.isEmpty(et.getText())) {
                f = true;
            }
        } else if (obj instanceof String) {
            String s = (String) obj;
            if (s.trim().equals("")) {
                f = true;
            }
        }
        return f;
    }

    /**
     * long类型时间格式转换
     */
    public static String convertToTime(long time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return df.format(date);
    }

    /**
     * 将中文标点替换为英文标点
     */
    public static String StringFilter(String str) throws PatternSyntaxException {
        if (isNotNull(str)) {
            str = str.replaceAll("【", "[").replaceAll("】", "]").replaceAll("！", "!").replaceAll("，", ",").replaceAll("。", ".").replaceAll("（", "(").replaceAll("）", ")").replaceAll("：", ":");
            String regEx = "[『』]"; // 清除掉特殊字符
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            return m.replaceAll("").trim();
        }
        return null;
    }

    /**
     * 给文字增加颜色
     *
     * @param txt
     * @param color 1为黄，2为绿
     * @return
     */
    public static SpannableString setTXTSpn(String txt, int color) {
        SpannableString sp = new SpannableString(txt);
        ForegroundColorSpan bgSp;
        bgSp = new ForegroundColorSpan(BaseApplication.getInstance().getResources().getColor(color));
        sp.setSpan(bgSp, 0, txt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    public static boolean isEmail(String strEmail) {
        Pattern pattern = Pattern
                .compile("^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher mc = pattern.matcher(strEmail);
        return mc.matches();
    }

    /**
     * url编码
     *
     * @param url 需要编码的字符串
     * @return
     */
    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, AppConst.CHARSETNAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 针对TextView显示中文中出现的排版错乱问题，通过调用此方法得以解决
     *
     * @param str
     * @return 返回全部为全角字符的字符串
     */
    public static String toDBC(String str) {
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 统计时拼接的字符串，记录首页的广告顺序标记，如：n-n-n，或者n-n
     *
     * @param type1
     * @param type2
     * @param type3
     * @return
     */

    public static String concatString(String type1, String type2, String type3) {
        StringBuffer buffer = new StringBuffer(type1);
        if (!TextUtils.isEmpty(type2)) {
            buffer = buffer.append("-").append(type2);
        }
        if (!TextUtils.isEmpty(type3)) {
            buffer = buffer.append("-").append(type3);
        }
        return buffer.toString();
    }

    /**
     * 流转换为String
     *
     * @param is
     * @return
     */
    public static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        if (is != null) {
            is.close();
        }
        if (in != null) {
            in.close();
        }
        return buffer.toString();
    }

    public static CharSequence priceSpannable(String price, String format, boolean isFormatStart) {
        try {
            if (TextUtils.isEmpty(price)) {
                return "";
            }

            String[] splitStr = price.split("\\.");
            if (splitStr.length != 2) {
                //size大于2,原样返回
                return price;
            }
            int leftIndex = price.indexOf(splitStr[0]);
            int leftLength = splitStr[0].length();
            int totalLength = price.length();
            int formatLength = format.length();
            //flag为包含start，不包含end
            SpannableStringBuilder builder = new SpannableStringBuilder();

            SpannableString spannableFormat = new SpannableString(format);
            Context context = BaseApplication.getInstance();
            spannableFormat.setSpan(new AbsoluteSizeSpan(DensityTool.dip2px(context, 12)), 0, formatLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            SpannableString spannablePrice = new SpannableString(price);
            spannablePrice.setSpan(new AbsoluteSizeSpan(DensityTool.dip2px(context, 18)), leftIndex, leftLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannablePrice.setSpan(new AbsoluteSizeSpan(DensityTool.dip2px(context, 12)), leftLength, totalLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            if (isFormatStart) {
                builder.append(spannableFormat);
                builder.append(spannablePrice);
                return builder;
            } else {
                builder.append(spannablePrice);
                builder.append(spannableFormat);
                return builder;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 指定字符串模板调大小(例如数字,汉字)
    public static SpannableString getDiffTextSizeString(String text, int size,
                                                        String patternStr) {
        SpannableString ret = null;
        if (text != null) {
            SpannableString s = new SpannableString(text);
            Pattern p = Pattern.compile(patternStr);
            Matcher m = p.matcher(s);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                s.setSpan(new AbsoluteSizeSpan(DensityTool.dip2px(BaseApplication.getInstance(), size), true), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ret = s;
        }
        return ret;
    }

}
