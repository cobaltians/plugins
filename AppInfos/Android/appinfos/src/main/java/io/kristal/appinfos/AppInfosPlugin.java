/**
 *
 * AppInfosPlugin
 * AppInfos
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Kristal
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

package io.kristal.appinfos;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;

public class AppInfosPlugin extends CobaltAbstractPlugin {

    protected final static String TAG = AppInfosPlugin.class.getSimpleName();

    private static final String GET_APP_INFOS = "getAppInfos";
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";


    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    protected static AppInfosPlugin sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) sInstance = new AppInfosPlugin();
        sInstance.addWebContainer(webContainer);
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        try {
            String action = message.getString(Cobalt.kJSAction);

            if (action.equals(GET_APP_INFOS)){
                CobaltFragment fragment = webContainer.getFragment();
                Context ctx = webContainer.getActivity();
                try {
                    PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                    JSONObject data = new JSONObject();
                    data.put(VERSION_NAME, packageInfo.versionName);
                    data.put(VERSION_CODE, packageInfo.versionCode);
                    fragment.sendCallback(message.getString(Cobalt.kJSCallback),data);
                }
                catch (PackageManager.NameNotFoundException e) {
                    if (Cobalt.DEBUG) {
                        Log.e(TAG, "onMessage: Package Name not found " + message.toString() + ".");
                        e.printStackTrace();
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
}
