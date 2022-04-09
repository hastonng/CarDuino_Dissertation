package CarDuino.Services;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * Description:
 * This class is to store all SharedPreference for User Information
 */
public class SaveSharedPreference
{
    static  final int PREF_USER_ID = 0;
    static  final int PREF_REG_DEVICES= 0;
    static  final int PREF_USER_AGE= 0;
    static  final String PREF_USER_NAME= "username";
    static  final String PREF_FIRST_NAME= "firstname";
    static  final String PREF_LAST_NAME= "lastname";
    static  final String PREF_EMAIL= "email";
    static  final String PREF_USER_IMAGE_PATH= "imagepath";
    static  final String PREF_PREVIOUS_DEVICE = null;


    public static void setRSSI_STATE(Context ctx, boolean state)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean("RSSI_STATE", state);
        editor.apply();
    }

    public static boolean getRSSI_STATE(Context ctx)
    {
        return getSharedPreferences(ctx).getBoolean("RSSI_STATE",false);
    }

    public static void setPREF_USER_NAME(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.apply();
    }

    public static String getPREF_FIRST_NAME(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_FIRST_NAME, "");
    }

    public static void setPREF_FIRST_NAME(Context ctx, String userFName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_FIRST_NAME, userFName);
        editor.apply();
    }
    public static String getPREF_LAST_NAME(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_LAST_NAME, "");
    }

    public static void setPREF_LAST_NAME(Context ctx, String userLName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LAST_NAME, userLName);
        editor.apply();
    }

    public static String getPREF_IMAGE_PATH(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_IMAGE_PATH, "");
    }

    public static void setPREF_IMAGE_PATH(Context ctx, String imagepath)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_IMAGE_PATH, imagepath);
        editor.apply();
    }

    public static void setPREF_EMAIL(Context ctx, String userEmail)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_EMAIL, userEmail);
        editor.apply();
    }

    public static void setPREF_PREVIOUS_DEVICE(Context ctx,String deviceName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_PREVIOUS_DEVICE, deviceName);
        editor.apply();
    }

    public static String getPREF_PREVIOUS_DEVICE(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_PREVIOUS_DEVICE,"");
    }


    public static String getPREF_EMAIL(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_EMAIL, "");
    }


    public static int getPREF_USER_ID(Context ctx)
    {
        return getSharedPreferences(ctx).getInt("user_id",PREF_USER_ID);
    }


    public static void setPREF_USER_ID(Context ctx, int user_id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt("user_id",user_id);
        editor.apply();
    }

    public static int getPREF_REG_DEVICES(Context ctx)
    {
        return getSharedPreferences(ctx).getInt("user_reg_device",PREF_REG_DEVICES);
    }

    public static void setPREF_REG_DEVICES(Context ctx, int reg_device)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt("user_reg_device",reg_device);
        editor.apply();
    }

    public static int getPREF_USER_AGE(Context ctx)
    {
        return getSharedPreferences(ctx).getInt("user_age",PREF_USER_AGE);
    }

    public static void setPREF_USER_AGE(Context ctx, int user_age)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt("user_age",user_age);
        editor.apply();
    }



    static SharedPreferences getSharedPreferences(Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void clear(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, "username");
        editor.putString(PREF_FIRST_NAME, "firstname");
        editor.putString(PREF_LAST_NAME, "lastname");
        editor.putString(PREF_USER_IMAGE_PATH, "path");
        editor.putString(PREF_EMAIL, "email");
        editor.putString(PREF_PREVIOUS_DEVICE, null);
        editor.putInt("user_id",0);
        editor.putInt("user_age",0);
        editor.putInt("user_reg_device",0);
        editor.apply();
        editor.clear();
    }
}