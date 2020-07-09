package com.rasyidabdulhalim.aquaza.utils;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class BindingAdapter {

    @android.databinding.BindingAdapter({"imageUrl"})
    public static void loadImageFromInt(ImageView view, int url) {
        Glide.with(view.getContext())
                .load(url)
                .thumbnail(0.05f)
                .into(view);
    }

    @android.databinding.BindingAdapter({"imageUrl"})
    public static void loadImageFromString(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .thumbnail(0.05f)
                .into(view);
    }

    @android.databinding.BindingAdapter({"imageUri"})
    public static void loadImageFromUri(ImageView view, Uri uri) {
        Glide.with(view.getContext())
                .load(uri)
                .thumbnail(0.05f)
                .into(view);
    }


}
