/**
 *
 * LocationPlugin
 * LocationPlugin
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


import android.util.Log;
import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Sébastien Famel
 */

public final class WebservicesPlugin extends CobaltAbstractPlugin {

    private static final String TAG = WebservicesPlugin.class.getSimpleName();

    private static final String CALL = "call";
    private static final String CALL_ID = "callId";


    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) {
            sInstance = new WebservicesPlugin();
        }

        sInstance.addWebContainer(webContainer);
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        CobaltFragment fragment = webContainer.getFragment();
        try {
            String action = message.getString(Cobalt.kJSAction);

            if (action.equals(CALL)) {
                int time = (int) System.currentTimeMillis();
                JSONObject callId = new JSONObject();
                callId.put(CALL_ID, time);
                WebservicesTask wsTask = new WebservicesTask(fragment);
                message.put(CALL_ID, time);
                wsTask.execute(message);
                fragment.sendCallback(message.getString(Cobalt.kJSCallback), callId);
            }

            else if (Cobalt.DEBUG) Log.d(TAG, "ERROR - can't found action in message : " + message.toString());
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.d(TAG, "ERROR - can't find a good key in : " + message.toString());
                exception.printStackTrace();
            }
        }
    }

    public static JSONObject treatData(JSONObject data, JSONObject process, CobaltFragment fragment) {
        if (process != null
            && WebservicesInterface.class.isAssignableFrom(fragment.getClass())) {
            return ((WebservicesInterface) fragment).treatData(data, process);
        }
        else return data;
    }

    public static void storeValue(String value, String key, CobaltFragment fragment) {
        if (WebservicesInterface.class.isAssignableFrom(fragment.getClass())
            && !((WebservicesInterface) fragment).storeValue(value, key))
            WebservicesData.setItem(value, key);
    }

    public static String storedValueForKey(String key, CobaltFragment fragment) {
        if (WebservicesInterface.class.isAssignableFrom(fragment.getClass())
                && ((WebservicesInterface) fragment).storedValueForKey(key) != null)
            return ((WebservicesInterface) fragment).storedValueForKey(key);
        else {
            return WebservicesData.getString(key);
        }
    }



}