package fr.haploid.WebservicesPlugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.cobaltians.cobalt.Cobalt;

import java.util.Map;

/**
 * Created by sebastienfamel on 30/10/2014.
 */
public class WebservicesData {
    private static Context sApplicationContext;
    private static SharedPreferences sSharedPreferences;

    private static void initAppContext() {
        sApplicationContext = Cobalt.getAppContext();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
    }

    /*******************
    * GETTERS/SETTERS  *
    *******************/

    public static void setItem(String value, String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setItem(boolean value, String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setItem(int value, String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setItem(long value, String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defaut) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        return sSharedPreferences.getBoolean(key, defaut);
    }

    public static String getString(String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        return sSharedPreferences.getString(key, null);
    }

    public static int getInt(String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        return sSharedPreferences.getInt(key, -1);
    }

    public static long getLong(String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        return sSharedPreferences.getLong(key, -1);
    }

    public static void removeItem(String key) {
        if (sApplicationContext == null) {
            initAppContext();
        }
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static int getCountItem() {
        if (sApplicationContext == null) {
            initAppContext();
        }
        Map<String, ?> map = sSharedPreferences.getAll();
        return map.size();
    }
}
