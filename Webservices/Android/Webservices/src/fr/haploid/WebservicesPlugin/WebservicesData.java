/**
 *
 * WebservicesPlugin
 * WebservicesData
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Haploid
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package fr.haploid.WebservicesPlugin;

import fr.cobaltians.cobalt.Cobalt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

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

    public static void setItem(String key, String value) {
        if (sApplicationContext == null) initAppContext();

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setItem(String key, boolean value) {
        if (sApplicationContext == null) initAppContext();

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setItem(String key, int value) {
        if (sApplicationContext == null) initAppContext();

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setItem(String key, long value) {
        if (sApplicationContext == null) initAppContext();

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defaut) {
        if (sApplicationContext == null) initAppContext();

        return sSharedPreferences.getBoolean(key, defaut);
    }

    public static String getString(String key) {
        if (sApplicationContext == null) initAppContext();

        return sSharedPreferences.getString(key, null);
    }

    public static int getInt(String key) {
        if (sApplicationContext == null) initAppContext();

        return sSharedPreferences.getInt(key, -1);
    }

    public static long getLong(String key) {
        if (sApplicationContext == null) initAppContext();

        return sSharedPreferences.getLong(key, -1);
    }

    public static void removeItem(String key) {
        if (sApplicationContext == null) initAppContext();

        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static int getCountItem() {
        if (sApplicationContext == null) initAppContext();

        Map<String, ?> map = sSharedPreferences.getAll();
        return map.size();
    }
}
