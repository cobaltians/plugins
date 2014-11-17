package fr.haploid.WebservicesPlugin;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sebastienfamel on 04/11/2014.
 */
public class WebservicesTask extends AsyncTask<JSONObject, Void, JSONObject> {

    private static final String TAG = WebservicesTask.class.getSimpleName();

    private CobaltFragment mFragment;

    private static final String CALL_ID = "callId";
    private static final String CALL_WS = "callWS";
    private static final String ON_STORAGE_RESULT = "onStorageResult";
    private static final String ON_STORAGE_ERROR = "onStorageError";
    private static final String ON_WS_ERROR = "onWSError";
    private static final String ON_WS_RESULT = "onWSResult";
    private static final String PARAMS = "params";
    private static final String PROCESS_DATA = "processData";
    private static final String SAVE_TO_STORAGE = "saveToStorage";
    private static final String SEND_CACHE_RESULT = "sendCacheResult";
    private static final String STATUS_CODE = "statusCode";
    private static final String STORAGE_KEY = "storageKey";
    private static final String TEXT = "text";
    private static final String TYPE = "type";
    private static final String URL = "url";
    private static final String WEBSERVICES = "webservices";
    private static final String WS_SUCCESS = "ws_success";

    /**************
    * Constructor
    **************/
    public WebservicesTask (CobaltFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected JSONObject doInBackground(JSONObject... jsonObjects) {
        if (jsonObjects.length == 0) {
            return null;
        }
        JSONObject jsonObject = jsonObjects[0];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        JSONObject responseHolder = new JSONObject();


        WebservicesResponseData responseData = null;

        try {
            long callId = jsonObject.getLong(CALL_ID);

            responseHolder.put(CALL_ID, callId );
            responseHolder.put(WS_SUCCESS, false);
            responseHolder.put(CALL_WS, false);
            responseHolder.put(TEXT, "" );

            JSONObject data = jsonObject.getJSONObject(Cobalt.kJSData);
            Log.d(TAG, "data on start = "+data.toString());
            boolean sendCacheResult = data.optBoolean(SEND_CACHE_RESULT, false);

            if (sendCacheResult) {
                JSONObject resultWs = new JSONObject();
                resultWs.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                resultWs.put(Cobalt.kJSPluginName, WEBSERVICES);
                try {
                    int storageSize = WebservicesData.getCountItem();
                    if (storageSize > 0) {

                        String storedByKey = WebservicesPlugin.storedValueForKey(data.getString(STORAGE_KEY), mFragment);

                        if (storedByKey != null) {
                            JSONObject dataFromStorage = new JSONObject(storedByKey);
                            dataFromStorage = WebservicesPlugin.treatData(dataFromStorage, data.optJSONObject(PROCESS_DATA), mFragment);
                            dataFromStorage.put(CALL_ID, callId);
                            resultWs.put(Cobalt.kJSAction, ON_STORAGE_RESULT);
                            resultWs.put(Cobalt.kJSData, dataFromStorage);
                        }
                        else {
                            resultWs.put(Cobalt.kJSAction, ON_STORAGE_ERROR);
                            resultWs.put(TEXT, "NOT_FOUND");
                        }
                    }
                    else {
                        resultWs.put(Cobalt.kJSAction, ON_STORAGE_ERROR);
                        resultWs.put(TEXT, "EMPTY");
                    }
                }
                catch (JSONException e) {
                    resultWs.put(Cobalt.kJSAction, ON_STORAGE_ERROR);
                    resultWs.put(TEXT, "UNKNOW_ERROR");
                }
                mFragment.sendMessage(resultWs);
            }

            if (data.has(URL)) {
                // Note: it's optional from Web with default value = false
                boolean saveToStorage = data.getBoolean(SAVE_TO_STORAGE);
                if (saveToStorage) {
                    responseHolder.put(STORAGE_KEY, data.getString(STORAGE_KEY));
                }

                responseHolder.put(SAVE_TO_STORAGE, saveToStorage);

                responseData = WebservicesHelper.downloadFromServer(data.getString(URL), data.getJSONObject(PARAMS), data.getString(TYPE));
                if (responseData != null) {
                    responseHolder.put(TEXT, responseData.getResponseData());
                    responseHolder.put(STATUS_CODE, responseData.getStatusCode());
                    responseHolder.put(WS_SUCCESS, responseData.isSuccess());
                    responseHolder.put(CALL_WS, true);
                }
            }
            return responseHolder;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return responseHolder;
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        JSONObject resultWs = new JSONObject();
        try {
            if (jsonObject.getBoolean(CALL_WS)) {
                resultWs.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                resultWs.put(Cobalt.kJSPluginName, WEBSERVICES);

                if (jsonObject.getBoolean(WS_SUCCESS)) {
                    resultWs.put(Cobalt.kJSAction, ON_WS_RESULT);
                }
                else {
                    resultWs.put(Cobalt.kJSAction, ON_WS_ERROR);
                }

                int statusCode = jsonObject.optInt(STATUS_CODE, -1);
                if (statusCode != -1) {
                    resultWs.put(STATUS_CODE, statusCode);
                }

                JSONObject data = new JSONObject();
                data.put(CALL_ID, jsonObject.getLong(CALL_ID));

                try {
                    JSONObject responseData = new JSONObject(jsonObject.getString(TEXT));
                    responseData = WebservicesPlugin.treatData(responseData, jsonObject.optJSONObject(PROCESS_DATA), mFragment);
                    data.put(Cobalt.kJSData, responseData);
                }
                catch (JSONException e) {
                    data.put(TEXT, jsonObject.get(TEXT));
                }

                resultWs.put(Cobalt.kJSData, data);

                if (jsonObject.optBoolean(SAVE_TO_STORAGE)) {
                    WebservicesPlugin.storeValue(data.toString(), jsonObject.getString(STORAGE_KEY), mFragment);
                }

                mFragment.sendMessage(resultWs);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
