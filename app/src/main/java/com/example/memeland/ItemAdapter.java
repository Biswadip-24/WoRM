package com.example.memeland;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import static android.Manifest.permission.*;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ExampleViewHolder> {
    private ArrayList<Row_item> mRow_items;
    private Context mContext;
    private Activity mActivity;
    int liked = R.drawable.liked_heart;
    int not_liked = R.drawable.heart;
    private static HashSet<Integer> Button_liked = new HashSet<>();
    private static HashSet<Integer> Button_Download = new HashSet<>();
    private Row_item currentItem;


    public static class ExampleViewHolder extends RecyclerView.ViewHolder {

        public ImageView mMeme;
        public ImageButton mShare;
        public ImageButton mLike;
        public ImageButton mDownload;
        public boolean download_shared;


        public ExampleViewHolder(View itemView) {
            super(itemView);
            mMeme = itemView.findViewById(R.id.memeImage);
            mShare = itemView.findViewById(R.id.imageButton3);
            mLike = itemView.findViewById(R.id.imageButton2);
            mDownload = itemView.findViewById(R.id.downloadButton);
            download_shared = false;
        }

    }

    public ItemAdapter(ArrayList<Row_item> RowList, Context mc, Activity mA) {
        mRow_items = RowList;
        mContext = mc;
        mActivity = mA;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_layout, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ExampleViewHolder holder, final int position) {


        try {
            currentItem = mRow_items.get(position);
            Glide.with(mContext).load(currentItem.getUrl()).into(holder.mMeme);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                mActivity.requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 1);
            }


            final BitmapDrawable[] drawable = new BitmapDrawable[1];
            final Bitmap[] bitmap = new Bitmap[1];
            final Uri[] bmpUri = {null};

            Glide.with(mActivity)
                    .load(currentItem.getUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                            try {
                                drawable[0] = (BitmapDrawable) resource;
                                bitmap[0] = drawable[0].getBitmap();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return false;

                        }
                    })
                    .into(holder.mMeme);

            holder.mDownload.setOnClickListener(view -> {
                if (!Button_Download.contains(position)) {
                    Toast.makeText(holder.itemView.getContext(), "Image Saved Succesfully", Toast.LENGTH_SHORT).show();

                    FileOutputStream fos = null;
                    File file = mContext.getExternalFilesDir(null);

                    if (file == null) throw new AssertionError();

                    File dir = new File(file.getAbsolutePath() + "/MemeLand");

                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    String s ="%d"+ currentItem.getUrl().substring(currentItem.getUrl().lastIndexOf('.'));

                    String filename = String.format(s, System.currentTimeMillis());
                    File outFile = new File(dir, filename);
                    try {
                        fos = new FileOutputStream(outFile);
                        bitmap[0].compress(Bitmap.CompressFormat.PNG, 100, fos);
                        bmpUri[0] = Uri.fromFile(outFile);
                        fos.flush();
                        fos.close();

                        Button_Download.add(position);

                        MediaScannerConnection.scanFile(mContext,
                                new String[]{outFile.toString()},
                                new String[]{outFile.getName()}, null);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Image Already Saved", Toast.LENGTH_SHORT).show();
                }
            });

            Log.e("meme", "position" + position);

            if (!Button_liked.contains(position)) {
                holder.mLike.setBackgroundResource(not_liked);
            } else holder.mLike.setBackgroundResource(liked);

            holder.mLike.setOnClickListener(view -> {
                if (Button_liked.contains(position)) {
                    holder.mLike.setBackgroundResource(not_liked);
                    Button_liked.remove(position);
                } else {
                    holder.mLike.setBackgroundResource(liked);
                    Button_liked.add(position);
                }
            });

            holder.mShare.setOnClickListener(view -> {
                //shareMeme();
                if (!Button_Download.contains(position)) {
                    FileOutputStream fos = null;
                    File file = mContext.getExternalFilesDir(null);

                    assert file != null;

                    File dir = new File(file.getAbsolutePath() + "/MemeLand");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    String filename = String.format("%d.png", System.currentTimeMillis());
                    File outFile = new File(dir, filename);
                    try {
                        fos = new FileOutputStream(outFile);
                        bitmap[0].compress(Bitmap.CompressFormat.PNG, 100, fos);
                        bmpUri[0] = Uri.fromFile(outFile);
                        shareMeme(bmpUri[0]);
                        fos.flush();
                        fos.close();


                        Button_Download.add(position);

                        MediaScannerConnection.scanFile(mContext,
                                new String[]{outFile.toString()},
                                new String[]{outFile.getName()}, null);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    shareMeme(bmpUri[0]);
                }
            });
        } catch (Exception e) {
        }
    }


    public void shareMeme(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        sendIntent.setType("image/png");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share this meme using..");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(shareIntent);
    }

    @Override
    public int getItemCount() {
        return mRow_items.size();
    }
}