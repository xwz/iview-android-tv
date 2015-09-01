package io.github.xwz.abciview.content;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import io.github.xwz.abciview.R;

public class RecommendationBuilder {

    private final Context mContext;
    private String mTitle;
    private String mDescription;
    private PendingIntent mIntent;
    private Bitmap mImage;
    private Bitmap mBackground;

    public RecommendationBuilder(Context context) {
        mContext = context;
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


    public RecommendationBuilder setImage(Bitmap image) {
        mImage = image;
        return this;
    }

    public RecommendationBuilder setBackground(Bitmap url) {
        mBackground = url;
        return this;
    }

    public Notification build() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mTitle)
                .setContentText(mDescription)
                .setSmallIcon(R.mipmap.icon)
                .setLargeIcon(mImage)
                .setContentIntent(mIntent)
                .setColor(mContext.getResources().getColor(R.color.brand_color))
                .setPriority(0)
                .setLocalOnly(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(mBackground));
        return builder.build();
    }
}
