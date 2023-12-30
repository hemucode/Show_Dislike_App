package com.hemu.dislikesshowyt;

import static com.hemu.dislikesshowyt.models.ChannelDataService.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hemu.dislikesshowyt.models.Channel;
import com.hemu.dislikesshowyt.models.ObjectDataService;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Objects;

public class ViewActivity extends AppCompatActivity {
    WebView web;

    boolean isFullScreen = false;
    ImageView imageView,thumbnail,playBtn;
    TextView title,channel_name,view_text,like_text,dislike_text,view_text1,like_text1,dislike_text1,des,tags;

    AdView adView;
    String VideoId;

    ConstraintLayout imageBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Bundle extras = getIntent().getExtras();

        web = findViewById(R.id.webView);


        title = findViewById(R.id.title);
        channel_name = findViewById(R.id.channelTitle);


        view_text = findViewById(R.id.view_text);
        like_text = findViewById(R.id.like_text);
        dislike_text = findViewById(R.id.dislike_text);

        view_text1 = findViewById(R.id.view_text1);
        like_text1 = findViewById(R.id.like_text1);
        dislike_text1 = findViewById(R.id.dislike_text1);

        imageView = findViewById(R.id.rateImage);
        des = findViewById(R.id.des);

        thumbnail = findViewById(R.id.thumbnail);

        adView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        playBtn = findViewById(R.id.playBtn);
        imageBox = findViewById(R.id.imageBox);

        tags = findViewById(R.id.tags);
        tags.setVisibility(View.GONE);

        if (extras == null) return;
        String type = extras.getString("type");




        if (Objects.equals(type, "search")){
            Channel channel = (Channel) getIntent().getSerializableExtra("channel");
            assert channel != null;

            Picasso.get().load(channel.getThumbnail()).into(thumbnail);
            web.loadUrl("https://www.youtube.com/embed/"+channel.getVideoId());
            VideoId = channel.getVideoId();
            channel_name.setText(channel.getChannelTitle());
            title.setText(channel.getTitle());
            apiRequest(channel.getVideoId());
            des.setText(channel.getDescription());

        }


        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);

        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Toast.makeText(ViewActivity.this, "error", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String Script = "var css = document.createElement('style');" +
                        "var head = document.head;" +
                        "css.innerText = `" +
                        ".ytp-show-cards-title," +
                        ".ytp-pause-overlay," +
                        ".branding-img," +
                        ".ytp-large-play-button," +
                        ".ytp-youtube-button," +
                        ".ytp-menuitem:nth-child(1)," +
                        ".ytp-small-redirect," +
                        ".ytp-menuitem:nth-child(4)" +
                        "{display:none !important;}`;" +
                        "head.appendChild(css);" +
                        "document.querySelector('.ytp-play-button').click();" +
                        "css.type = 'text/css';"+
                        "let ytpFullscreenButton = document.querySelector('.ytp-fullscreen-button');" +
                        "ytpFullscreenButton.addEventListener('click', function() { Android.showToast(`toast`); });"+
                        "if(document.querySelector('.ytp-error-content-wrap-reason')){Android.showToast(`error`);}else{Android.showToast(`noError`);}";

                web.evaluateJavascript(Script,null);

            }
        });


        web.addJavascriptInterface(new WebAppInterface(this), "Android");


    }
    private void apiRequest(String id) {
        ObjectDataService service = new ObjectDataService(ViewActivity.this);
        SharedPreferences getShared = getSharedPreferences("appdata", MODE_PRIVATE);
        String DislikeAPI = getShared.getString("DislikeAPI","noValue");
        String DislikeAPI_url;
        if(DislikeAPI.equals("noValue")){
            DislikeAPI_url = getString(R.string.DislikeAPI);
        }else {
            DislikeAPI_url = DislikeAPI;
        }
        String url = DislikeAPI_url+ id;
        service.getChannelData(url, new ObjectDataService.OnDataResponse() {
            @Override
            public Void onError(String error) {
                return null;
            }

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "1onErrorResponses: " + response);
              try {

                  view_text.setText(response.get("viewCount").toString());
                  like_text.setText(response.get("likes").toString());
                  dislike_text.setText(response.get("dislikes").toString());

                  view_text1.setText(formatValue(Float.valueOf(response.get("viewCount").toString())));
                  like_text1.setText(formatValue(Float.valueOf(response.get("likes").toString())));
                  dislike_text1.setText(formatValue(Float.valueOf(response.get("dislikes").toString())));



                  String rating = response.get("rating").toString().substring(0,1);

                  if (rating.equals("0")){
                      imageView.setImageResource(R.drawable.rate6);
                  }else if (rating.equals("1")){
                      imageView.setImageResource(R.drawable.rate5);
                  }else if (rating.equals("2")){
                      imageView.setImageResource(R.drawable.rate4);
                  }else if (rating.equals("3")){
                      imageView.setImageResource(R.drawable.rate3);
                  }else if (rating.equals("4")){
                      imageView.setImageResource(R.drawable.rate2);
                  }else if (rating.equals("5")){
                      imageView.setImageResource(R.drawable.rate1);
                  }else {
                      imageView.setImageResource(R.drawable.rate6);
                  }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }

    public String formatValue(float value) {
        String arr[] = {"", "K", "M", "B", "T", "P", "E"};
        int index = 0;
        while ((value / 1000) >= 1) {
            value = value / 1000;
            index++;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return String.format("%s%s", decimalFormat.format(value), arr[index]);
    }

    public class WebAppInterface {
        Context mContext;
        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void showToast(String toast) {

            if (toast.equals("toast")){
                ViewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        setFullScreen();
                    }
                });
            }

            if (toast.equals("noError")){
                ViewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playBtn.setVisibility(View.VISIBLE);
                        playBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageBox.setVisibility(View.GONE);
                                web.setVisibility(View.VISIBLE);
                            }
                        });


                    }
                });
            }

            if (toast.equals("error")){
                ViewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playBtn.setVisibility(View.VISIBLE);
                        playBtn.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW , Uri.parse("https://www.youtube.com/watch?v="+VideoId))));
                    }
                });
            }
        }
    }

    public void setFullScreen(){
        if (isFullScreen){

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) web.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
            web.setLayoutParams(params);

            isFullScreen = false;
        }else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) web.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = params.MATCH_PARENT;


            web.setLayoutParams(params);


            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);



            isFullScreen = true;

        }
    }
}