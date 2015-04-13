/**
 *
 * WebServices
 * WebServicesTask
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

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class WebServicesTask extends AsyncTask<Void, Void, JSONObject> {

    private static final String TAG = WebServicesTask.class.getSimpleName();

    private static final String JSPluginNameWebservices = "webservices";

    public static final String kJSCallId = "callId";
    public static final String kJSSendCacheResult = "sendCacheResult";
    public static final String kJSStorageKey = "storageKey";
    public static final String kJSProcessData = "processData";
    public static final String kJSUrl = "url";
    public static final String kJSHeaders = "headers";
    public static final String kJSParams = "params";
	public static final String kJSTimeout = "timeout";
    public static final String kJSType = "type";
    private static final String JSTypeDELETE = "DELETE";
    private static final String JSTypeGET = "GET";
    private static final String JSTypePOST = "POST";
    private static final String JSTypePUT = "PUT";
    public static final String kJSSaveToStorage = "saveToStorage";

    private static final String kJSSuccess = "ws_success";
    public static final String kJSStatusCode = "statusCode";
    public static final String kJSText = "text";

    private static final String JSActionOnStorageResult = "onStorageResult";
    private static final String JSActionOnStorageError = "onStorageError";
    private static final String JSTextEmpty = "EMPTY";
    private static final String JSTextNotFound = "NOT_FOUND";
    private static final String JSTextUnknownError = "UNKNOW_ERROR";

    public static final String JSOnWSResult = "onWSResult";
    public static final String JSOnWSError = "onWSError";

    private final CobaltFragment mFragment;
    private JSONObject mCall;
    private long mCallId;
    private boolean mSendCacheResult;
    private String mStorageKey;
    private JSONObject mProcessData;
    private String mUrl;
    private JSONObject mHeaders;
    private String mParams;
	private int mTimeout;
    private String mType;
    private boolean mSaveToStorage;

    public WebServicesTask(CobaltFragment fragment, JSONObject call) {
        mFragment = fragment;
        mCall = call;

        try {
            mCallId = call.getLong(kJSCallId);
            JSONObject data = call.getJSONObject(Cobalt.kJSData);
            mSendCacheResult = data.optBoolean(kJSSendCacheResult);
            mUrl = data.optString(kJSUrl, null);

            if (mUrl != null) {
                mHeaders = data.optJSONObject(kJSHeaders);
                mParams = data.optString(kJSParams);
				mTimeout = data.optInt(kJSTimeout, -1);
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
                Log.e(WebServicesPlugin.TAG,    TAG + ": check your Webservice call. Known issues: \n"
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

                JSONObject storedData = new JSONObject();
                storedData.put(kJSCallId, mCallId);
                cacheResultMessage.put(Cobalt.kJSData, storedData);

                int storageSize = WebServicesData.getCountItem();
                if (storageSize > 0) {
                    if (mStorageKey != null) {
                        String storedValue = WebServicesPlugin.storedValueForKey(mStorageKey, mFragment);

                        if (storedValue != null) {
                            try {
                                storedData.put(Cobalt.kJSData, WebServicesPlugin.treatData(new JSONObject(storedValue), mProcessData, mFragment));

                                cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageResult);
                                //cacheResultMessage.put(Cobalt.kJSData, storedData);
                            }
                            catch (JSONException exception) {
                                if (Cobalt.DEBUG) {
                                    Log.e(WebServicesPlugin.TAG, TAG + " - doInBackground: value parsing failed for key " + mStorageKey + ". Value: " + storedValue);
                                    exception.printStackTrace();
                                }

                                cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                                storedData.put(kJSText, JSTextUnknownError);
                            }
                        }
                        else {
                            if (Cobalt.DEBUG) Log.w(WebServicesPlugin.TAG, TAG + " - doInBackground: value not found in cache for key " + mStorageKey + ".");

                            cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                            storedData.put(kJSText, JSTextNotFound);
                        }
                    }
                    else {
                        cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                        storedData.put(kJSText, JSTextUnknownError);

                    }
                }
                else {
                    if (Cobalt.DEBUG) Log.w(WebServicesPlugin.TAG, TAG + " - doInBackground: cache is empty.");

                    cacheResultMessage.put(Cobalt.kJSAction, JSActionOnStorageError);
                    storedData.put(kJSText, JSTextEmpty);
                }
                cacheResultMessage.put(Cobalt.kJSData, storedData);

                mFragment.sendMessage(cacheResultMessage);
            }

            if (mUrl != null
                && mType != null) {
                // TODO: later, use Helper
                /*
                responseData = WebServicesHelper.downloadFromServer(data.getString(URL), data.getJSONObject(PARAMS), data.getString(TYPE));
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
						if (mTimeout > 0) urlConnection.setConnectTimeout(mTimeout);
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
                        if (responseCode != -1) {
                            response.put(kJSStatusCode, responseCode);
                            if (responseCode < 400) response.put(kJSSuccess, true);
                        }

                        try {
                            InputStream inputStream;
                            if (responseCode >= 400 && responseCode < 600) inputStream = urlConnection.getErrorStream();
                            else inputStream = urlConnection.getInputStream();
                            
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuffer buffer = new StringBuffer();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                buffer.append(line).append("\n");
                            }
                            if (buffer.length() != 0) response.put(kJSText, buffer.toString());
                        }
                        catch (IOException exception) {
                            if (Cobalt.DEBUG) {
                                Log.i(WebServicesPlugin.TAG, TAG + " - doInBackground: no DATA returned by server.");
                                exception.printStackTrace();
                            }
                        }
                    }
                    catch (ProtocolException exception) {
                        if (Cobalt.DEBUG) {
                            Log.e(WebServicesPlugin.TAG, TAG + " - doInBackground: not supported request method type " + mType);
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
                        Log.e(WebServicesPlugin.TAG, TAG + " - doInBackground: malformed url " + builder.build().toString() + ".");
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
                else message.put(Cobalt.kJSAction, JSOnWSError);

                JSONObject data = new JSONObject();
                data.put(kJSCallId, mCallId);

                int statusCode = response.optInt(kJSStatusCode, -1);
                if (statusCode != -1) data.put(kJSStatusCode, statusCode);
                String text = response.optString(kJSText, null);
                if (text != null) {
                    try {
                        JSONObject responseData = new JSONObject(text);
                        responseData = WebServicesPlugin.treatData(responseData, mProcessData, mFragment);
                        data.put(Cobalt.kJSData, responseData);
                    }
                    catch (JSONException exception) {
                        Log.w(Cobalt.TAG, TAG + " - onPostExecute: response could not be parsed as JSON. Response: " + text);
                        exception.printStackTrace();
                        data.put(kJSText, text);
                    }
                }

                message.put(Cobalt.kJSData, data);

                if (response.getBoolean(kJSSuccess) || WebServicesPlugin.handleError(mCall, message, mFragment)) mFragment.sendMessage(message);

                if (mSaveToStorage
                    && mStorageKey != null) {
                    WebServicesPlugin.storeValue(text, mStorageKey, mFragment);
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
