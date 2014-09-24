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
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.location.LocationListener;
import android.os.Build;
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
public final class LocationPlugin extends CobaltAbstractPlugin implements LocationListener {

    // TAG
    private static final String TAG = LocationPlugin.class.getSimpleName();

    /**********************************************************
     * MEMBERS
     **********************************************************/

    private static final String LOCATION = "location";
    private static final String ERROR = "error";
    private static final String TEXT = "text";
    private static final String CODE = "code";
    private static final String DISABLED = "disabled";
    private static final String NULL = "null";

    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ALTITUDE = "altitude";
    private static final String ACCURACY = "accuracy";

    private static final String GET_LOCATION = "getLocation";
    private static final String GET_LONGITUDE = "getLongitude";
    private static final String GET_LATITUDE = "getLatitude";
    private static final String GET_ALTITUDE = "getAltitude";
    private static final String GET_ACCURACY = "getAccuracy";

    private boolean mFoundProvider = true;
    private LocationManager mLocationManager;
    private String mProvider;
    private CobaltFragment mFragment;

    private boolean getLocation = false;
    private boolean getLongitude = false;
    private boolean getLatitude = false;
    private boolean getAltitude = false;
    private boolean getAccuracy = false;


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
        mFragment = webContainer.getFragment();

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

                // no callback cause call sendMessage now
                //String callback = message.getString(Cobalt.kJSCallback);
                JSONObject resultLocation = new JSONObject();
                resultLocation.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                resultLocation.put(Cobalt.kJSPluginName, LOCATION);
                JSONObject data = new JSONObject();

                if (location != null) {
                    data.put(ERROR, false);
                    JSONObject value = new JSONObject();
                    if (action.equals(GET_LOCATION) ||
                            action.equals(GET_LONGITUDE)) {
                        getLongitude = true;
                        getLocation = true;
                        value.put(LONGITUDE, location.getLongitude());
                    }

                    if (action.equals(GET_LOCATION) ||
                            action.equals(GET_LATITUDE)) {
                        getLatitude = true;
                        getLocation = true;
                        value.put(LATITUDE, location.getLatitude());
                    }

                    if (action.equals(GET_ALTITUDE)) {
                        value.put(ALTITUDE, location.getAltitude());
                        getAltitude = true;
                    }

                    if (action.equals(GET_ACCURACY)) {
                        value.put(ACCURACY, location.getAccuracy());
                        getAccuracy = true;
                    }

                    //resultLocation.put(CALLBACK, callback);
                    data.put(Cobalt.kJSValue, value);
                    resultLocation.put(Cobalt.kJSData, data);
                    mFragment.sendMessage(resultLocation);
                }

                else {
                    data.put(ERROR, true);
                    if (mFoundProvider) {
                        data.put(CODE, NULL);
                        data.put(TEXT, "No location found, please wait");
                        Log.d(TAG, "call requestLocationUpdate");
                        mLocationManager.requestLocationUpdates(mProvider,400, 1, this);
                    }
                    else {
                        data.put(CODE, DISABLED);
                        data.put(TEXT, "Location detection has been disabled by user");
                    }
                    resultLocation.put(Cobalt.kJSData, data);
                    mFragment.sendMessage(resultLocation);
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

        mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) mProvider = LocationManager.GPS_PROVIDER;

        else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) mProvider = LocationManager.NETWORK_PROVIDER;

        else {
            mFoundProvider = false;
            return null;
        }
        Location location = mLocationManager.getLastKnownLocation(mProvider);

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        if (location != null) {
            Log.d(TAG, "location values = "+location.getLongitude() + " / "+ location.getLatitude());
            JSONObject resultLocation = new JSONObject();
            try {
                resultLocation.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                resultLocation.put(Cobalt.kJSPluginName, LOCATION);
                JSONObject data = new JSONObject();
                data.put(ERROR, false);
                JSONObject value = new JSONObject();
                if (getLocation || getLongitude)
                    value.put(LONGITUDE, location.getLongitude());
                if (getLocation || getLatitude)
                    value.put(LATITUDE, location.getLatitude());
                if (getAltitude)
                    value.put(ALTITUDE, location.getAltitude());
                if (getAccuracy)
                    value.put(ALTITUDE, location.getAccuracy());

                data.put(Cobalt.kJSValue, value);
                resultLocation.put(Cobalt.kJSData, data);
                Log.d(TAG, "send new position to web with location = "+resultLocation.toString());

                mFragment.sendMessage(resultLocation);
                mLocationManager.removeUpdates(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
