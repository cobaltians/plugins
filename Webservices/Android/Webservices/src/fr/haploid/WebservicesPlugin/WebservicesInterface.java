package fr.haploid.WebservicesPlugin;

import org.json.JSONObject;

/**
 * Created by sebastienfamel on 03/11/2014.
 */
public interface WebservicesInterface {

    public JSONObject treatData(JSONObject data, JSONObject process);
    public boolean storeValue(String value, String key);
    public String storedValueForKey(String key);
}
