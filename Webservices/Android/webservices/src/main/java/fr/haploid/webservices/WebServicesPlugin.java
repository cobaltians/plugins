/**
 *
 * WebServicesPlugin
 * WebServices
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

package fr.haploid.webservices;

import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.plugin.CobaltPluginWebContainer;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public final class WebServicesPlugin extends CobaltAbstractPlugin {

    protected static final String TAG = WebServicesPlugin.class.getSimpleName();

    private static final String kJSCallId = "callId";
    private static final String JSActionCall = "call";

    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    protected static WebServicesPlugin sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) sInstance = new WebServicesPlugin();
        sInstance.addWebContainer(webContainer);
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        try {

            String action = message.getString(Cobalt.kJSAction);

            if (action.equals(JSActionCall)) {
                CobaltFragment fragment = webContainer.getFragment();
                long time = System.currentTimeMillis();

                try {
                    JSONObject data = new JSONObject();
                    data.put(kJSCallId, time);
                    fragment.sendCallback(message.getString(Cobalt.kJSCallback), data);

                    message.put(kJSCallId, time);
                    WebServicesTask wsTask = new WebServicesTask(fragment, message);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) wsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else wsTask.execute();
                }
                catch (JSONException exception) {
                    if (Cobalt.DEBUG) {
                        Log.e(TAG, "onMessage: missing callback key in message " + message.toString() + ".");
                        exception.printStackTrace();
                    }
                }
            }
            else if (Cobalt.DEBUG) Log.e(TAG, "onMessage: invalid action " + action + "in message " + message.toString() + ".");
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(TAG, "onMessage: missing action key in message " + message.toString() + ".");
                exception.printStackTrace();
            }
        }
    }

    public static JSONObject treatData(JSONObject data, JSONObject process, CobaltFragment fragment) {
        if (process != null
                && WebServicesInterface.class.isAssignableFrom(fragment.getClass())) {
            return ((WebServicesInterface) fragment).treatData(data, process);
        }
        else return data;
    }

    public static boolean handleError(JSONObject call, JSONObject response, CobaltFragment fragment) {
        return ! WebServicesInterface.class.isAssignableFrom(fragment.getClass()) || ((WebServicesInterface) fragment).handleError(call, response);
    }

    public static void storeValue(String key, String value, CobaltFragment fragment) {
        if (WebServicesInterface.class.isAssignableFrom(fragment.getClass())
                && ! ((WebServicesInterface) fragment).storeValue(value, key)) {
            WebServicesData.setItem(value, key);
        }
    }

    public static String storedValueForKey(String key, CobaltFragment fragment) {
        if (WebServicesInterface.class.isAssignableFrom(fragment.getClass())) {
            String storedValue = ((WebServicesInterface) fragment).storedValueForKey(key);
            if (storedValue != null) return storedValue;
        }

        return WebServicesData.getString(key);
    }
}