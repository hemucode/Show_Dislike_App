package com.hemu.dislikesshowyt;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.hemu.dislikesshowyt.models.Common;
import com.hemu.dislikesshowyt.models.InAppUpdate;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    InAppUpdate inAppUpdate;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    Button paste_btn, view_btn,button_ex;
    EditText editText;
    Button button;

    AdView adView;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check app update
        inAppUpdate = new InAppUpdate(MainActivity.this);
        inAppUpdate.checkForAppUpdate();

        editText = findViewById(R.id.url_box);

        MobileAds.initialize(this, initializationStatus -> {
        });

        adView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        inAppUpdate = new InAppUpdate(MainActivity.this);
        inAppUpdate.checkForAppUpdate();

        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW , Uri.parse("https://www.youtube.com/")));
        });

        button_ex = findViewById(R.id.button_ex);
        button_ex.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW , Uri.parse("https://www.downloadhub.cloud/2022/10/dislike-add-youtube.html")));
        });

        setAds();

        view_btn = findViewById(R.id.view_btn);
        view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url_id = editText.getText().toString();
                if (!url_id.isEmpty()){
                    String pattern = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|(?:be-nocookie|be)\\.com\\/(?:watch|[\\w]+\\?(?:feature=[\\w]+.[\\w]+\\&)?v=|v\\/|e\\/|embed\\/|live\\/|shorts\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
                    Pattern compiledPattern = Pattern.compile(pattern);
                    Matcher matcher = compiledPattern.matcher(url_id);
                    if (Common.isConnectToInternet(MainActivity.this)) {

                        if(matcher.find()){
                            if (mInterstitialAd != null) {
                                mInterstitialAd.show((Activity) v.getContext());
                                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        Intent t = new Intent(MainActivity.this, DirectViewActivity.class);
                                        t.putExtra("type", "link");
                                        t.putExtra("id", matcher.group(1));
                                        v.getContext().startActivity(t);
                                        editText.setText("");
                                        mInterstitialAd = null;
                                        setAds();
                                    }
                                });

                            } else {
                                Intent t = new Intent(MainActivity.this, DirectViewActivity.class);
                                t.putExtra("type", "link");
                                t.putExtra("id", matcher.group(1));
                                v.getContext().startActivity(t);
                                editText.setText("");
                            }
                        } else {
                            editText.setText("");
                            MainActivity.this.startActivity(new Intent(MainActivity.this, SearchActivity.class)
                                    .putExtra("title",url_id));

                        }
                    }else {
                        Toast.makeText(MainActivity.this, "Connect Internet", Toast.LENGTH_SHORT).show();

                    }


                }else {
                    //No URL provided
                    Toast.makeText(MainActivity.this, "No Value provided", Toast.LENGTH_SHORT).show();
                }
            }
        });

        paste_btn = findViewById(R.id.paste_btn);
        paste_btn.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            try {
                CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
                String youTubeUrl = textToPaste.toString();
                String pattern = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|(?:be-nocookie|be)\\.com\\/(?:watch|[\\w]+\\?(?:feature=[\\w]+.[\\w]+\\&)?v=|v\\/|e\\/|embed\\/|live\\/|shorts\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
                Pattern compiledPattern = Pattern.compile(pattern);
                Matcher matcher = compiledPattern.matcher(youTubeUrl);
                if(matcher.find()){
                    //return matcher.group();
                    editText.setText(textToPaste);
                    Toast.makeText(this, "Youtube URL", Toast.LENGTH_SHORT).show();

                } else {
                    editText.setText(textToPaste);
                    Toast.makeText(this, "Search Video", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                editText.setText("");
                Toast.makeText(this, "Copy first", Toast.LENGTH_SHORT).show();
                return;
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inAppUpdate.onActivityResult(requestCode, resultCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inAppUpdate.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inAppUpdate.onDestroy();
    }
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    public class NetworkChangeListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Common.isConnectToInternet(context)) {
                final Dialog dialog = new Dialog(context); // Context, this, etc.
                dialog.setContentView(R.layout.activity_network);
                LinearLayout linearLayout = dialog.findViewById(R.id.dismiss);
                linearLayout.setOnClickListener(v -> dialog.cancel());
                dialog.show();
            }
        }
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