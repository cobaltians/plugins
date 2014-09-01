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

package fr.haploid.LocationPlugin;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationListener;
import android.os.Bundle;
import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.plugin.CobaltPluginWebContainer;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sébastien Famel
 */
public final class LocationPlugin extends CobaltAbstractPlugin{

    // TAG
    private static final String TAG = LocationPlugin.class.getSimpleName();

    /**********************************************************
     * MEMBERS
     **********************************************************/

    private static final String LOCATION = "location";
    private static final String ERROR = "error";
    private static final String TEXT = "text";

    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ALTITUDE = "altitude";
    private static final String ACCURACY = "accuracy";

    private static final String GET_LOCATION = "getLocation";
    private static final String GET_LONGITUDE = "getLongitude";
    private static final String GET_LATITUDE = "getLatitude";
    private static final String GET_ALTITUDE = "getAltitude";
    private static final String GET_ACCURACY = "getAccuracy";

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) {
            sInstance = new LocationPlugin();
        }

        sInstance.addWebContainer(webContainer);

        return sInstance;
    }


    /***********************************************************************************************************
     * OVERRIDEN METHODS
     ***********************************************************************************************************/

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        CobaltFragment fragment = webContainer.getFragment();

        Location location = getLocation(webContainer);

        boolean catchAction = false;
        
        try {
            String action = message.getString(Cobalt.kJSAction);

            if (action.equals(GET_LOCATION) ||
                    action.equals(GET_LONGITUDE) ||
                    action.equals(GET_LATITUDE) ||
                    action.equals(GET_ALTITUDE) ||
                    action.equals(GET_ACCURACY)) {

            	catchAction = true;
            	
                String callback = message.getString(Cobalt.kJSCallback);
                JSONObject resultLocation = new JSONObject();

                if (location != null) {
                    if (action.equals(GET_LOCATION) ||
                            action.equals(GET_LONGITUDE))
                        resultLocation.put(LONGITUDE, location.getLongitude());

                    if (action.equals(GET_LOCATION) ||
                            action.equals(GET_LATITUDE))
                        resultLocation.put(LATITUDE, location.getLatitude());

                    if (action.equals(GET_ALTITUDE))
                        resultLocation.put(ALTITUDE, location.getAltitude());

                    if (action.equals(GET_ACCURACY))
                        resultLocation.put(ACCURACY, location.getAccuracy());

                    fragment.sendCallback(callback, resultLocation);
                }

                else {
                    resultLocation.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                    resultLocation.put(Cobalt.kJSPluginName, LOCATION);
                    JSONObject data = new JSONObject();
                    data.put(ERROR, true);
                    data.put(TEXT, "location is NULL");
                    resultLocation.put(Cobalt.kJSData, data);
                    fragment.sendMessage(resultLocation);
                    if (Cobalt.DEBUG) Log.d(TAG, "location is NULL");
                }

                if (!catchAction) 
                	if (Cobalt.DEBUG) Log.d(TAG, "ERROR - can't found action in message : " + message.toString());
            }
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) {
            	if (Cobalt.DEBUG) Log.d(TAG, "ERROR - can't find a good key in : " + message.toString());
            	exception.printStackTrace();
            }
        }
    }

    private Location getLocation(CobaltPluginWebContainer webContainer) {
        Activity activity = webContainer.getActivity();

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        String provider = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) provider = LocationManager.GPS_PROVIDER;

        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) provider = LocationManager.NETWORK_PROVIDER;

        else if (Cobalt.DEBUG) Log.d(TAG, " ERROR - can't found LocationManager provider");

        return locationManager.getLastKnownLocation(provider);
    }
}
