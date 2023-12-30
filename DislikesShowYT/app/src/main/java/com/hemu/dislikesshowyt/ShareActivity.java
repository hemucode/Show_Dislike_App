package com.hemu.dislikesshowyt;

import static com.hemu.dislikesshowyt.models.ChannelDataService.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hemu.dislikesshowyt.models.ObjectDataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareActivity extends AppCompatActivity {

    BottomSheetDialog dialog;

    private InterstitialAd mInterstitialAd;

    AdView adView;

    String appsName, packageName;

    ImageView appOpen,shareBtn,infoBtn,closeBtn;

    TextView title, view_text,like_text,dislike_text,view_text1,like_text1,dislike_text1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        setAds();

        appsName = getApplication().getString(R.string.app_name);
        packageName = getApplication().getPackageName();




        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }

        dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_dialog,null,false);

        dialog.setContentView(view);
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        MobileAds.initialize(this, initializationStatus -> {
        });
        adView = view.findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        view_text = view.findViewById(R.id.view_text);
        like_text = view.findViewById(R.id.like_text);
        dislike_text = view.findViewById(R.id.dislike_text);

        view_text1 = view.findViewById(R.id.view_text1);
        like_text1 = view.findViewById(R.id.like_text1);
        dislike_text1 = view.findViewById(R.id.dislike_text1);

        title = view.findViewById(R.id.title_x);

        appOpen = view.findViewById(R.id.appOpen);

        appOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ShareActivity.this, MainActivity.class));
            }
        });



        shareBtn = view.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent share = new Intent("android.intent.action.SEND");
                share.setType("text/plain");
                share.putExtra("android.intent.extra.SUBJECT", ShareActivity.this.appsName);
                String APP_Download_URL = "https://play.google.com/store/apps/details?id=" + ShareActivity.this.packageName;
                share.putExtra("android.intent.extra.TEXT", ShareActivity.this.appsName + " - Hey check out new Dislike Show app at: \n\n" + APP_Download_URL);
                ShareActivity.this.startActivity(Intent.createChooser(share, "Share Dislike Show"));
            }
        });



        infoBtn = view.findViewById(R.id.infoBtn);

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ShareActivity.this);
                builder1.setMessage(R.string.information);
                builder1.setCancelable(true);
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });


        closeBtn = view.findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            String pattern = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|(?:be-nocookie|be)\\.com\\/(?:watch|[\\w]+\\?(?:feature=[\\w]+.[\\w]+\\&)?v=|v\\/|e\\/|embed\\/|live\\/|shorts\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(sharedText);
            if(matcher.find()) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(ShareActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            dataLoadApi(matcher.group(1));
                            apiRequest(matcher.group(1));
                            mInterstitialAd = null;
                            setAds();
                        }
                    });

                } else {
                    dataLoadApi(matcher.group(1));
                    apiRequest(matcher.group(1));
                }

            }else {
                Toast.makeText(this, "YouTube Link Only", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ShareActivity.this, MainActivity.class));
            }
            //Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
        }
    }

    private void apiRequest(String id) {
        ObjectDataService service = new ObjectDataService(ShareActivity.this);
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

    private void dataLoadApi(String id) {
        SharedPreferences getShared = getSharedPreferences("appdata", MODE_PRIVATE);
        String APIKEY1 = getShared.getString("APIKEY1","noValue");
        String APIKEY2 = getShared.getString("APIKEY2","noValue");
        String APIKEY_UES = getShared.getString("APIKEY_UES","noValue");
        String YTSearchURL = getShared.getString("YTDetailsUrl","noValue");
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
            APP_YTSearchURL = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=MY_VIDEO_ID&fields=items(id%2Csnippet)&key=MY_APIKEY";
        }else {
            APP_YTSearchURL = YTSearchURL;
        }

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


        String url = APP_YTSearchURL.replace("MY_VIDEO_ID", id).replace("MY_APIKEY",MY_APIKEY);
        Log.d(TAG, "1onErrorResponses: " + url);
        ObjectDataService service = new ObjectDataService(ShareActivity.this);

        service.getChannelData(url, new ObjectDataService.OnDataResponse() {
            @Override
            public Void onError(String error) {
                return null;
            }

            @Override
            public void onResponse(JSONObject response) {
                // Log.d(TAG, "1onErrorResponses: " + response);
                try {
                    JSONArray jsonArray = response.getJSONArray("items");
                    JSONObject channelData = jsonArray.getJSONObject(0);
                    JSONObject jsonObject2 = new JSONObject(channelData.getString("snippet"));

                    title.setText(jsonObject2.getString("title"));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }



    public void setAds(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.InterstitialAd), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });
    }
}