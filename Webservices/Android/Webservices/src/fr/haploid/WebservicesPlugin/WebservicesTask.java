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
    private static final String FILTER_DATA = "filterData";
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
        boolean wsSucces = false;

        try {
            long callId = jsonObject.getLong(CALL_ID);

            responseHolder.put(CALL_ID, callId );
            responseHolder.put(WS_SUCCESS, false);
            responseHolder.put(TEXT, "" );

            JSONObject data = jsonObject.getJSONObject(Cobalt.kJSData);
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
                            resultWs.put(Cobalt.kJSAction, ON_STORAGE_RESULT);
                            resultWs.put(Cobalt.kJSData, dataFromStorage);
                            Log.d(TAG, "send to the web from storage = " + resultWs.toString());
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
                Uri.Builder builder = new Uri.Builder();
                builder.encodedPath(data.getString(URL));

                JSONObject params = data.getJSONObject(PARAMS);
                JSONArray namesOfParams = params.names();

                for (int i=0 ; i<namesOfParams.length() ; i++) {
                    String param = namesOfParams.getString(i);
                    builder.appendQueryParameter(param, params.get(param).toString());
                }
                java.net.URL url = new URL(builder.build().toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(data.getString(TYPE));
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                responseHolder.put(STATUS_CODE, responseCode);

                if (responseCode == 200) {
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return responseHolder;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        return responseHolder;
                    }

                    responseHolder.put(TEXT, buffer.toString() );
                    responseHolder.put(WS_SUCCESS, true);
                }
            }
            else {
                Log.d(TAG, "no key URL in data also do something dude");
            }
            return responseHolder;

        }
        catch (JSONException e) {
            e.printStackTrace();
            return responseHolder;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return responseHolder;
        } catch (IOException e) {
            e.printStackTrace();
            return responseHolder;
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        JSONObject resultWs = new JSONObject();

        try {
            resultWs.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
            resultWs.put(Cobalt.kJSPluginName, WEBSERVICES);

            // si WS renvoie un succes ou non
            if (jsonObject.getBoolean(WS_SUCCESS)) {
                resultWs.put(Cobalt.kJSAction, ON_WS_RESULT);
            }
            else {
                resultWs.put(Cobalt.kJSAction, ON_WS_ERROR);
                int statusCode = jsonObject.optInt(STATUS_CODE, -1);
                if (statusCode != -1) {
                    resultWs.put(STATUS_CODE, statusCode);
                }
            }

            // construction de l'objet data
            JSONObject data = new JSONObject();
            data.put(CALL_ID, jsonObject.getLong(CALL_ID));

            // jsonification possible ou non
            try {
                JSONObject responseData = new JSONObject(jsonObject.getString(TEXT));
                responseData = WebservicesPlugin.treatData(responseData, jsonObject.optJSONObject(PROCESS_DATA), mFragment);
                data.put(Cobalt.kJSData, responseData);
            }catch (JSONException e) {
                data.put(TEXT, jsonObject.get(TEXT));
            }

            resultWs.put(Cobalt.kJSData, data);

            // Storage
            if (jsonObject.getBoolean(SAVE_TO_STORAGE)) {
                WebservicesPlugin.storeValue(data.toString(), jsonObject.getString(STORAGE_KEY), mFragment);
            }

            // Envoi au web
            mFragment.sendMessage(resultWs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
