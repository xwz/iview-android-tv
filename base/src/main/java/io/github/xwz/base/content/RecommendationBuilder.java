package io.github.xwz.base.content;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecommendationBuilder {

    private static final String TAG = "RecommendationBuilder";

    private final int mId;
    private final Context mContext;
    private String mTitle;
    private String mDescription;
    private PendingIntent mIntent;
    private Bitmap mImage;
    private Bitmap mBackground;
    private int mIconRes;
    private int mColor;
    private String mBackgroundPrefix;

    public RecommendationBuilder(Context context, int id) {
        mContext = context;
        mId = id;
    }

    public RecommendationBuilder setBackgroundPrefix(String prefix) {
        mBackgroundPrefix = prefix;
        return this;
    }

    public RecommendationBuilder setTitle(String title) {
        mTitle = title;
        return this;
    }

    public RecommendationBuilder setDescription(String description) {
        mDescription = description;
        return this;
    }

    public RecommendationBuilder setPendingIntent(PendingIntent intent) {
        mIntent = intent;
        return this;
    }

    public RecommendationBuilder setIcon(int res) {
        mIconRes = res;
        return this;
    }

    public RecommendationBuilder setColor(int color) {
        mColor = color;
        return this;
    }

    public RecommendationBuilder setImage(Bitmap image) {
        mImage = image;
        return this;
    }

    public RecommendationBuilder setBackground(Bitmap background) {
        mBackground = background;
        return this;
    }

    public Notification build() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mTitle)
                .setContentText(mDescription)
                .setSmallIcon(mIconRes)
                .setLargeIcon(mImage)
                .setContentIntent(mIntent)
                .setColor(mColor)
                .setExtras(createBackgroundImageBundle())
                .setPriority(0)
                .setLocalOnly(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(mImage));
        return builder.build();
    }

    private Bundle createBackgroundImageBundle() {
        Bundle extras = new Bundle();
        if (mBackground != null) {
            String background = Uri.parse(mBackgroundPrefix + Integer.toString(mId)).toString();
            extras.putString(Notification.EXTRA_BACKGROUND_IMAGE_URI, background);
            try {
                File bitmapFile = getNotificationBackground(mContext, mId);
                bitmapFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(bitmapFile);
                mBackground.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
                Log.d(TAG, "Writing background image to: " + bitmapFile);
            } catch (IOException ioe) {
                Log.d(TAG, "Exception caught writing bitmap to file!", ioe);
            }
        }
        return extras;
    }

    private static File getNotificationBackground(Context context, int notificationId) {
        String filename = "recommendation-tmp" + Integer.toString(notificationId) + ".png";
        Log.i(TAG, "getNotificationBackground: " + filename);
        return new File(context.getCacheDir(), filename);
    }

    public static class RecommendationBackgroundContentProvider extends ContentProvider {

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public String getType(Uri uri) {
            return null;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            return null;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return null;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
            Log.i(TAG, "openFile");
            int backgroundId = Integer.parseInt(uri.getLastPathSegment());
            File bitmapFile = getNotificationBackground(getContext(), backgroundId);
            return ParcelFileDescriptor.open(bitmapFile, ParcelFileDescriptor.MODE_READ_ONLY);
        }
    }
}
