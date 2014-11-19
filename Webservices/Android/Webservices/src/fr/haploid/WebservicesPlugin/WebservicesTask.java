/**
 *
 * WebservicesTask
 * WebservicesPlugin
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
import fr.cobaltians.cobalt.fragments.CobaltFragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class WebservicesTask extends AsyncTask<Void, Void, JSONObject> {

    private static final String TAG = WebservicesTask.class.getSimpleName();

    private static final String JSPluginNameWebservices = "webservices";

    private static final String kJSCallId = "callId";
    private static final String kJSSendCacheResult = "sendCacheResult";
    private static final String kJSStorageKey = "storageKey";
    private static final String kJSProcessData = "processData";
    private static final String kJSUrl = "url";
    private static final String kJSHeaders = "headers";
    private static final String kJSParams = "params";
    private static final String kJSType = "type";
    private static final String JSTypeGET = "GET";
    private static final String kJSSaveToStorage = "saveToStorage";

    private static final String kJSSuccess = "ws_success";
    private static final String kJSStatusCode = "statusCode";
    private static final String kJSText = "text";

    private static final String JSActionOnStorageResult = "onStorageResult";
    private static final String JSActionOnStorageError = "onStorageError";
    private static final String JSTextEmpty = "EMPTY";
    private static final String JSTextNotFound = "NOT_FOUND";
    private static final String JSTextUnknownError = "UNKNOW_ERROR";

    private static final String JSOnWSResult = "onWSResult";
    private static final String JSOnWSError = "onWSError";

    private final CobaltFragment mFragment;
    private long mCallId;
    private boolean mSendCacheResult;
    private String mStorageKey;
    private JSONObject mProcessData;
    private String mUrl;
    private JSONObject mHeaders;
    private String mParams;
    private String mType;
    private boolean mSaveToStorage;

    public WebservicesTask(CobaltFragment fragment, JSONObject message) {
        mFragment = fragment;

        try {
            mCallId = message.getLong(kJSCallId);
            JSONObject data = message.getJSONObject(Cobalt.kJSData);
            mSendCacheResult = data.optBoolean(kJSSendCacheResult);
            mUrl = data.optString(kJSUrl, null);

            if (mUrl != null) {
                mHeaders = data.optJSONObject(kJSHeaders);
                mParams = data.optString(kJSParams);
                mType = data.getString(kJSType);
                mSaveToStorage = data.optBoolean(kJSSaveToStorage);
            }

            if (mSendCacheResult || mUrl != null) mProcessData = data.optJSONObject(kJSProcessData);

            if (mSendCacheResult
                || (mUrl != null && mSaveToStorage)) {
                mStorageKey = data.getString(kJSStorageKey);
            }
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(WebservicesPlugin.TAG,    TAG + ": check your Webservice call. Known issues: \n"
                                                + "\t- missing data field, \n"
                                                + "\t- url field is defined but but missing type field, \n"
                                                + "\t- sendCacheResult field is true or url field is defined and saveToStorage field is true but missing storageKey field.\n");
                exception.printStackTrace();
            }
        }
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            if (mSendCacheResult) {
                JSONObject cacheResultMessage = new JSONObject();
                cacheResultMessage.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                cacheResultMessage.put(Cobalt.kJSPluginName, JSPluginNameWebservices);

                int storageSize = WebservicesData.getCountItem();
                if (storageSize > 0) {
                    if (mStorageKey != null) {
                        String storedValue = WebservicesPlugin.storedValueForKey(mStorageKey, mFragment);

                        if (storedValue != null) {
                            try {
                                JSONObject storedData = new JSONObject(storedValue);
                                storedData = WebservicesPlugin.treatData(storedData, mProcessData, mFragment);
                                storedData.put(kJSCallId, mCallId);

                                cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageResult);
                                cacheResultMessage.put(Cobalt.kJSData, storedData);
                            }
                            catch (JSONException exception) {
                                if (Cobalt.DEBUG) {
                                    Log.e(WebservicesPlugin.TAG, TAG + " - doInBackground: value parsing failed for key " + mStorageKey + ". Value: " + storedValue);
                                    exception.printStackTrace();
                                }

                                cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                                cacheResultMessage.put(kJSText, JSTextUnknownError);
                            }
                        }
                        else {
                            if (Cobalt.DEBUG) Log.w(WebservicesPlugin.TAG, TAG + " - doInBackground: value not found in cache for key " + mStorageKey + ".");

                            cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                            cacheResultMessage.put(kJSText, JSTextNotFound);
                        }
                    }
                    else {
                        cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                        cacheResultMessage.put(kJSText, JSTextUnknownError);
                    }
                }
                else {
                    if (Cobalt.DEBUG) Log.w(WebservicesPlugin.TAG, TAG + " - doInBackground: cache is empty.");

                    cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                    cacheResultMessage.put(kJSText, JSTextEmpty);
                }
                mFragment.sendMessage(cacheResultMessage);
            }

            if (mUrl != null
                && mType != null) {
                // TODO: later, use Helper
                /*
                responseData = WebservicesHelper.downloadFromServer(data.getString(URL), data.getJSONObject(PARAMS), data.getString(TYPE));
                if (responseData != null) {
                    responseHolder.put(TEXT, responseData.getResponseData());
                    responseHolder.put(STATUS_CODE, responseData.getStatusCode());
                    responseHolder.put(WS_SUCCESS, responseData.isSuccess());
                    responseHolder.put(CALL_WS, true);
                }
                */

                Uri.Builder builder = new Uri.Builder();
                builder.encodedPath(mUrl);

                if (! mParams.equals("")
                    && mType.equalsIgnoreCase(JSTypeGET)) builder.encodedQuery(mParams);

                JSONObject response = new JSONObject();
                response.put(kJSSuccess, false);

                try {
                    URL url = new URL(builder.build().toString());

                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        // TODO: set timeouts?
                        urlConnection.setRequestMethod(mType);
                        urlConnection.setDoInput(true);

                        if (mHeaders != null) {
                            JSONArray names = mHeaders.names();

                            if (names != null) {
                                int length = names.length();

                                for (int i = 0 ; i < length ; i++) {
                                    String name = names.getString(i);
                                    urlConnection.setRequestProperty(name, mHeaders.get(name).toString());
                                }
                            }
                        }

                        if (! mParams.equals("")
                            && ! mType.equalsIgnoreCase(JSTypeGET)) {
                            urlConnection.setDoOutput(true);

                            OutputStream outputStream = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                            writer.write(mParams);
                            writer.flush();
                            writer.close();
                            outputStream.close();
                        }

                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();
                        response.put(kJSStatusCode, responseCode);

                        InputStream inputStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuffer buffer = new StringBuffer();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            buffer.append(line).append("\n");
                        }

                        if (buffer.length() != 0) response.put(kJSText, buffer.toString());

                        response.put(kJSSuccess, true);
                    }
                    catch (ProtocolException exception) {
                        if (Cobalt.DEBUG) {
                            Log.e(WebservicesPlugin.TAG, TAG + " - doInBackground: not supported request method type " + mType);
                            exception.printStackTrace();
                        }
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }

                    return response;
                }
                catch (MalformedURLException exception) {
                    if (Cobalt.DEBUG) {
                        Log.e(WebservicesPlugin.TAG, TAG + " - doInBackground: malformed url " + builder.build().toString() + ".");
                        exception.printStackTrace();
                    }
                }
            }
        }
        catch (JSONException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        super.onPostExecute(response);

        if (response != null) {
            try {
                JSONObject message = new JSONObject();
                message.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                message.put(Cobalt.kJSPluginName, JSPluginNameWebservices);

                if (response.getBoolean(kJSSuccess)) message.put(Cobalt.kJSAction, JSOnWSResult);
                else {
                    message.put(Cobalt.kJSAction, JSOnWSError);
                    int statusCode = response.optInt(kJSStatusCode, -1);
                    if (statusCode != -1) message.put(kJSStatusCode, statusCode);
                }

                JSONObject data = new JSONObject();
                data.put(kJSCallId, mCallId);

                String text = response.optString(kJSText, null);
                if (text != null) {
                    try {
                        JSONObject responseData = new JSONObject(text);
                        responseData = WebservicesPlugin.treatData(responseData, mProcessData, mFragment);
                        data.put(Cobalt.kJSData, responseData);
                    }
                    catch (JSONException exception) {
                        Log.w(Cobalt.TAG, TAG + " - onPostExecute: response could not be parsed as JSON. Response: " + text);
                        exception.printStackTrace();
                        data.put(kJSText, text);
                    }
                }

                message.put(Cobalt.kJSData, data);
                mFragment.sendMessage(message);

                if (mSaveToStorage && mStorageKey != null) {
                    WebservicesPlugin.storeValue(text, mStorageKey, mFragment);
                }
                else if (Cobalt.DEBUG) {
                    Log.w(Cobalt.TAG,   TAG + " - onPostExecute: missing storageKey field \n"
                                        + "Web service response will not be stored in cache.");
                }
            }
            catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        else if (Cobalt.DEBUG) {
            Log.d(Cobalt.TAG, TAG + " - onPostExecute: url and/or type fields were empty, Webservice has not been called.");
        }
    }
}
