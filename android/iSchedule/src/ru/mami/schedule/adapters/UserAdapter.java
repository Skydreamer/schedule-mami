package ru.mami.schedule.adapters;

import java.util.HashMap;
import java.util.Map;

import ru.mami.schedule.utils.StringConstants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserAdapter {
    public static final String NAME = "name";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";

    private Context context;

    public UserAdapter(Context context) {
        this.context = context;
    }

    public Map<String, String> getUserAsMap() {
        Map<String, String> userInfo = new HashMap<String, String>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                StringConstants.USER_INFO_PREFERENCE, Context.MODE_PRIVATE);
        userInfo.put(StringConstants.USER_NAME,
                sharedPreferences.getString(StringConstants.USER_NAME, "-"));
        userInfo.put(StringConstants.USER_LOGIN,
                sharedPreferences.getString(StringConstants.USER_LOGIN, "-"));
        userInfo.put(StringConstants.USER_PHONE,
                sharedPreferences.getString(StringConstants.USER_PHONE, "-"));
        userInfo.put(StringConstants.USER_EMAIL,
                sharedPreferences.getString(StringConstants.USER_EMAIL, "-"));
        return userInfo;
    }

    public void saveUser(String name, String login, String email, String phone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                StringConstants.USER_INFO_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(StringConstants.USER_NAME, name);
        editor.putString(StringConstants.USER_LOGIN, login);
        editor.putString(StringConstants.USER_EMAIL, email);
        editor.putString(StringConstants.USER_PHONE, phone);
        editor.commit();
    }
}
