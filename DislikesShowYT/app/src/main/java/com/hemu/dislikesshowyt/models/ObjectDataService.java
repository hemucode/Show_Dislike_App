package com.hemu.dislikesshowyt.models;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class ObjectDataService {
    Context ctx;
    public static final String TAG = "TAG";

    public ObjectDataService(Context ctx) {
        this.ctx = ctx;
    }

    public interface OnDataResponse{
        Void onError(String error);
        void onResponse(JSONObject response);
    }

    public void getChannelData(String url, ObjectDataService.OnDataResponse onDataResponse){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onDataResponse.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onDataResponse.onError(error.getLocalizedMessage());
            }
        });

        queue.add(jsonObjectRequest);

    }

}
