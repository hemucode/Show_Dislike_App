package com.hemu.dislikesshowyt.Adopters;


import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.hemu.dislikesshowyt.R;
import com.hemu.dislikesshowyt.ViewActivity;
import com.hemu.dislikesshowyt.models.Channel;
import com.squareup.picasso.Picasso;


import java.util.List;


public class ChannelAdopters extends RecyclerView.Adapter<ChannelAdopters.ViewHolder> {
    List<Channel> channels;
    private InterstitialAd mInterstitialAd;
    private Context mContext;


    public ChannelAdopters(Context mContext, List<Channel> channels) {
        this.channels = channels;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ChannelAdopters.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.activity_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelAdopters.ViewHolder holder, int position) {
        int positions = position;
        Channel ChannelDataItem = channels.get(position);
        if (holder.title!= null) {
            holder.title.setText(ChannelDataItem.getTitle());
        }
        if (holder.channelTitle!= null) {
            holder.channelTitle.setText(ChannelDataItem.getChannelTitle());
        }
        if (holder.imageView!= null) {
            Picasso.get().load(ChannelDataItem.getThumbnail()).into(holder.imageView);
        }

        if (holder.item_view!= null){
            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show((Activity) v.getContext());
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Intent t = new Intent(v.getContext(), ViewActivity.class);
                                t.putExtra("type", "search");
                                t.putExtra("channel", channels.get(positions));
                                v.getContext().startActivity(t);
                                mInterstitialAd = null;
                                setAds();
                            }
                        });

                    } else {
                        Intent t = new Intent(v.getContext(), ViewActivity.class);
                        t.putExtra("type", "search");
                        t.putExtra("channel", channels.get(positions));
                        v.getContext().startActivity(t);
                    }
                }
            });
        }

        setAds();
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title,channelTitle;

        ConstraintLayout item_view;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.channelThumbnail);
            title = itemView.findViewById(R.id.title);
            channelTitle = itemView.findViewById(R.id.channelTitle);
            item_view = itemView.findViewById(R.id.item_view);

        }
    }

    public void setAds(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(mContext,mContext.getString(R.string.InterstitialAd), adRequest,
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
