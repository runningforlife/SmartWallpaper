package com.samsung.app.smartwallpaper.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.app.smartwallpaper.R;
import com.samsung.app.smartwallpaper.config.UrlConstant;
import com.samsung.app.smartwallpaper.network.ApiClient;

import java.io.File;
import java.io.InputStream;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Created by samsung on 2018/3/16.
 * Author: my2013.wang@samsung.com
 */

public class WallpaperItem {
    private static final String TAG = "WallpaperItem";
    private String mHashCode;
    private int mVoteUpCount=0;
    private Drawable mWallpaperDrawable;

    private int placeholder = R.drawable.img_placeholder;
    private boolean mHasVoteUp = false;
    private boolean mFavoriteOn = false;
    private String mPath;

    public WallpaperItem(){
    }

    public WallpaperItem(String hashcode){
        mHashCode = hashcode;
    }
    public void setHashCode(String hashCode){
        mHashCode = hashCode;
    }
    public String getHashCode(){
        return  mHashCode;
    }
    public String getUrl(){
        if(TextUtils.isEmpty(mHashCode)){
            return null;
        }
        return UrlConstant.DOWNLOAD_WALLPAPER_URL+mHashCode;
    }
    public int getVoteupCount(){
        return mVoteUpCount;
    }
    public void setVoteupCount(int cnt){
        mVoteUpCount = cnt;
    }
    public void voteUp(){
        mVoteUpCount++;
    }

    private TextView mVoteUpView;
    public void setVoteUpView(TextView textView){
        mVoteUpView = textView;
    }

    private ImageView mWallpaperView;
    public void setWallpaperView(ImageView imageView){
        mWallpaperView = imageView;
        if(mWallpaperView != null){
            mWallpaperView.setScaleType(ImageView.ScaleType.CENTER);
            mWallpaperView.setImageResource(placeholder);
        }
    }
    public void setVoteUpState(boolean voteUp){
        mHasVoteUp = voteUp;
    }
    public boolean hasVoteUp(){
        return mHasVoteUp;
    }
    public void setFavoriteOn(boolean favoriteOn){
        mFavoriteOn = favoriteOn;
    }
    public boolean isFavoriteOn(){
        return mFavoriteOn;
    }

    public void setWallpaperPath(String path){
        mPath = path;
    }
    public String getWallpaperPath(){
        return mPath;
    }

    public void setWallpaperDrawable(Drawable wallpaper){
        mWallpaperDrawable = wallpaper;
    }
    public Drawable getWallpaperDrawable(){
        return mWallpaperDrawable;
    }

    private static String WALLPAPER_FILES_DIR = Environment.getExternalStorageDirectory() + File.separator + "wallpaper_files";
    private AsyncTask<String,Void,Boolean> mLoadTask = null;
    public void loadWallpaperByHashCode(String hashcode){
        if(mLoadTask != null){
            mLoadTask.cancel(true);
        }

        if(TextUtils.isEmpty(hashcode)){
            return;
        }

        mLoadTask = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mWallpaperDrawable = null;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                String hashcode = params[0];
                Bitmap bitmap = ApiClient.getWallpaperByHashCode(hashcode);
                if(bitmap != null){
                    mWallpaperDrawable = new BitmapDrawable(bitmap);
                    return true;
                }else{
                    String relative_path = ApiClient.getWallpaperFilePathByHashCode(hashcode);
                    Log.i(TAG, "relative_path=" + relative_path);
                    if(!TextUtils.isEmpty(relative_path)) {
                        String full_path = WALLPAPER_FILES_DIR + File.separator + relative_path;
                        try {
                            bitmap = BitmapFactory.decodeFile(full_path);
                        }catch (Exception e){
                            Log.e(TAG, "error="+e.toString());
                        }
                        if(bitmap != null){
                            mWallpaperDrawable = new BitmapDrawable(bitmap);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if(mWallpaperView != null && success){
                    mWallpaperView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mWallpaperView.setImageDrawable(mWallpaperDrawable);
                    mWallpaperView.invalidate();
                }
            }
        };
        mLoadTask.executeOnExecutor(THREAD_POOL_EXECUTOR, hashcode);
    }
    public void loadWallpaperByPath(String path){
        if(mLoadTask != null){
            mLoadTask.cancel(true);
        }
        mLoadTask = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mWallpaperDrawable = null;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                String path = params[0];
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeFile(path);
                }catch (Exception e){
                    Log.e(TAG, "error="+e.toString());
                }
                if(bitmap != null){
                    mWallpaperDrawable = new BitmapDrawable(bitmap);
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if(mWallpaperView != null && success){
                    mWallpaperView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mWallpaperView.setImageDrawable(mWallpaperDrawable);
                    mWallpaperView.invalidate();
                }
            }
        };
        mLoadTask.executeOnExecutor(THREAD_POOL_EXECUTOR, path);
    }
    public void loadVoteUpCount(String hashcode){
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(String... params) {
                String hashcode = params[0];
                return ApiClient.getVoteUpCount(hashcode);
            }

            @Override
            protected void onPostExecute(Integer count) {
                super.onPostExecute(count);
                setVoteupCount(count);
                if(mVoteUpView != null){
                    mVoteUpView.setText(count+"赞");
                    mVoteUpView.invalidate();
                }
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR, hashcode);
    }
}
