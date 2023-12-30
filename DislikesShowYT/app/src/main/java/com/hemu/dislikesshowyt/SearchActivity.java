package com.hemu.dislikesshowyt;

import static com.hemu.dislikesshowyt.models.ChannelDataService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.hemu.dislikesshowyt.Adopters.ChannelAdopters;
import com.hemu.dislikesshowyt.models.Channel;
import com.hemu.dislikesshowyt.models.ObjectDataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    RecyclerView newsChannelList;
    ChannelAdopters newsChannelAdopters;
    List<Channel> newsChannels;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        progressBar = findViewById(R.id.progressBar);
        newsChannelList = findViewById(R.id.recyclerView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("title");
            SharedPreferences getShared = getSharedPreferences("appdata", MODE_PRIVATE);
            String APIKEY1 = getShared.getString("APIKEY1","noValue");
            String APIKEY2 = getShared.getString("APIKEY2","noValue");
            String APIKEY_UES = getShared.getString("APIKEY_UES","noValue");
            String YTSearchURL = getShared.getString("YTSearchURL","noValue");
            String APP_APIKEY1,APP_APIKEY2,MY_APIKEY,APP_YTSearchURL;

            if (APIKEY1.equals("noValue")){
                APP_APIKEY1 = getString(R.string.APIKEY1);
            }else {
                APP_APIKEY1 = APIKEY1;
            }

            if (APIKEY2.equals("noValue")){
                APP_APIKEY2 = getString(R.string.APIKEY2);
            }else {
                APP_APIKEY2 = APIKEY2;
            }

            if (YTSearchURL.equals("noValue")){
                APP_YTSearchURL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=20&q=MY_KEYWORD&type=video&key=MY_APIKEY";
            }else {
                APP_YTSearchURL = YTSearchURL;
            }


            assert title != null;
            SharedPreferences sharedPreferences = getSharedPreferences("appdata", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (APIKEY_UES.equals("noValue")){
                editor.putString("APIKEY_UES",APP_APIKEY1);
                editor.apply();
                MY_APIKEY = APP_APIKEY1;
                Log.d(TAG, "1onErrorResponses: " + "0");

            }else if (APIKEY_UES.equals(APP_APIKEY1)){
                MY_APIKEY = APP_APIKEY2;
                editor.putString("APIKEY_UES",APP_APIKEY2);
                editor.apply();
                Log.d(TAG, "1onErrorResponses: " + "1");
            }else if (APIKEY_UES.equals(APP_APIKEY2)){
                MY_APIKEY = APP_APIKEY1;
                editor.putString("APIKEY_UES",APP_APIKEY1);
                editor.apply();
                Log.d(TAG, "1onErrorResponses: " + "2");
            }else {
                MY_APIKEY = APP_APIKEY1;
                Log.d(TAG, "1onErrorResponses: " + "3");
            }


            String url = APP_YTSearchURL.replace("MY_KEYWORD", title).replace("MY_APIKEY",MY_APIKEY);
            //Log.d(TAG, "1onErrorResponses: " + url);
            ObjectDataService service = new ObjectDataService(SearchActivity.this);

            newsChannelList.setLayoutManager(new GridLayoutManager(this,1, LinearLayoutManager.VERTICAL, false));
            newsChannels = new ArrayList<>();
            newsChannelAdopters = new ChannelAdopters(this, newsChannels){
                @Override
                public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                    progressBar.setVisibility(View.GONE);
                    super.onBindViewHolder(holder, position);
                }

            };

            newsChannelList.setAdapter(newsChannelAdopters);
            service.getChannelData(url, new ObjectDataService.OnDataResponse() {
                @Override
                public Void onError(String error) {
                    return null;
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("items");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject channelData = jsonArray.getJSONObject(i);
                            JSONObject jsonObject1 = new JSONObject(channelData.getString("id"));
                            JSONObject jsonObject2 = new JSONObject(channelData.getString("snippet"));
                            Channel c = new Channel();
                            c.setId(i);
                            c.setVideoId(jsonObject1.getString("videoId"));
                            c.setTitle(jsonObject2.getString("title"));
                            c.setDescription(jsonObject2.getString("description"));
                            c.setChannelTitle(jsonObject2.getString("channelTitle"));
                            c.setThumbnail("https://i.ytimg.com/vi/"+jsonObject1.getString("videoId")+"/hqdefault.jpg");

                            //Log.d(TAG, "1onErrorResponses: " + jsonObject1.getString("videoId"));
                            newsChannels.add(c);
                            newsChannelAdopters.notifyDataSetChanged();

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });



        }
    }
}