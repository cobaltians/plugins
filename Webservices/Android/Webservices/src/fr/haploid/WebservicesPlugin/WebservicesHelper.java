/**
 *
 * WebservicesPlugin
 * WebservicesHelper
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

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebservicesHelper {

    protected static synchronized WebservicesResponseData downloadFromServer(String url, JSONObject params, String type) {

        int responseCode = 9999;

        StringBuffer buffer = new StringBuffer();

        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(url);

        JSONArray namesOfParams = params.names();

        for (int i = 0; i < namesOfParams.length(); i++) {
            try {
                String param = namesOfParams.getString(i);
                builder.appendQueryParameter(param, params.get(param).toString());
            }
            catch (JSONException e) {
                return new WebservicesResponseData("JSONException "+e.toString(), responseCode, false);
            }
        }

        URL urlBuild;

        try {
            urlBuild = new URL(builder.build().toString());
        }
        catch (MalformedURLException e) {
            return new WebservicesResponseData("MalformedURLException "+e.toString(), responseCode, false);
        }

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) urlBuild.openConnection();
            try {
                urlConnection.setRequestMethod(type);
            }
            catch (ProtocolException e) {
                return new WebservicesResponseData("ProtocolException "+e.toString(), responseCode, false);
            }

            urlConnection.connect();
            responseCode = urlConnection.getResponseCode();
            if (responseCode < 400) {
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return new WebservicesResponseData(null, responseCode, false);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return new WebservicesResponseData(null, responseCode, false);
                }
            }
        }
        catch (IOException e) {
            return new WebservicesResponseData("IOException "+e.toString(), responseCode, false);
        }
        return new WebservicesResponseData(buffer.toString(), responseCode, true);
    }
}
