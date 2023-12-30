package com.hemu.dislikesshowyt;

import static com.hemu.dislikesshowyt.models.ChannelDataService.TAG;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.hemu.dislikesshowyt.models.ChannelDataService;
import com.hemu.dislikesshowyt.models.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LaunchingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);
        Handler handler = new Handler();

        handler.postDelayed(() -> {
            LaunchingActivity.this.startActivity(new Intent(LaunchingActivity.this, MainActivity.class));
            finish();
        },800);

        if (Common.isConnectToInternet(LaunchingActivity.this)) {
            new webScript().execute();
        }

    }

    private class webScript extends AsyncTask<Void , Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            ChannelDataService service = new ChannelDataService(LaunchingActivity.this);
            service.getChannelData( getString(R.string.dataUrl), new ChannelDataService.OnDataResponse() {
                @Override
                public Void onError(String error) {
                    Log.d(TAG, "1onErrorResponse: " + error);
                    return null;
                }


                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG, "1onLon: " + response);
                    try {
                        JSONObject Data = response.getJSONObject(0);
                        SharedPreferences sharedPreferences = getSharedPreferences("appdata", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("APIKEY1",Data.getString("APIKEY1"));
                        editor.putString("APIKEY2",Data.getString("APIKEY2"));
                        editor.putString("DislikeAPI",Data.getString("DislikeAPI"));
                        editor.putString("YTDetailsUrl",Data.getString("YTDetailsUrl"));
                        editor.putString("YTSearchURL",Data.getString("YTSearchURL"));
                        editor.apply();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }

            });

            return null;
        }

    }

}