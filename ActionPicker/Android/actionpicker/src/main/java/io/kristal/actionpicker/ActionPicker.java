package io.kristal.actionpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.plugin.CobaltPluginWebContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActionPicker extends CobaltAbstractPlugin {

    private String mCallback;
    private CobaltFragment mFragment;
    protected static ActionPicker sInstance;

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {

        mFragment = webContainer.getFragment();

        if(message.optString("action").equals("getAction")) {
            JSONObject data = message.optJSONObject(Cobalt.kJSData);
            mCallback = message.optString(Cobalt.kJSCallback);
            String cancel = data.optString("cancel");

            JSONArray actionsJSONArray = data.optJSONArray("actions");
            if (actionsJSONArray != null) {
               ArrayList<String> actions = new ArrayList<>();
               for (int i = 0; i < actionsJSONArray.length(); i++){
               try {
                   String action = actionsJSONArray.getString(i);
                   actions.add(action);
               }
               catch(JSONException e) { e.printStackTrace(); }
               }
               showUIPicker(cancel, actions, mCallback, webContainer);
               }
        }

    }

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) {
            sInstance = new ActionPicker();
        }

        sInstance.addWebContainer(webContainer);

        return sInstance;
    }

    private void showUIPicker(String cancel, final ArrayList<String> actions, final String callback, CobaltPluginWebContainer webCont) {
        CharSequence[] items = actions.toArray(new CharSequence[actions.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(webCont.getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                JSONObject data = new JSONObject();
                try {
                    data.put("index", item);
                    mFragment.sendCallback(callback, data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}