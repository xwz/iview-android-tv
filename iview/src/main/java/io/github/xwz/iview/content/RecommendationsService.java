package io.github.xwz.iview.content;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import io.github.xwz.base.content.RecommendationBuilder;
import io.github.xwz.iview.R;
import io.github.xwz.iview.activities.DetailsActivity;
import io.github.xwz.iview.api.EpisodeModel;

public class RecommendationsService extends IntentService {
    private static final String TAG = "UpdateRecommendations";

    private static final String RECOMMENDATION_TAG = "io.github.xwz.iview.RECOMMENDATION_TAG";
    private static final String BACKGROUND_URI_PREFIX = "content://io.github.xwz.iview.recommendation/";

    public RecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Updating recommendation cards");
        EpisodeModel episode = (EpisodeModel) intent.getSerializableExtra(ContentManager.CONTENT_ID);
        int id = intent.getIntExtra(ContentManager.CONTENT_TAG, 0);
        if (episode != null) {
            try {
                Log.d(TAG, "Will recommend: " + episode);
                buildNotification(episode, id);
            } catch (IOException e) {
                Log.e(TAG, "Error:" + e.getMessage());
            }
        }
    }

    private void buildNotification(EpisodeModel ep, final int id) throws IOException {
        Point size = new Point(getResources().getDimensionPixelSize(R.dimen.card_width),
                getResources().getDimensionPixelSize(R.dimen.card_height));
        Bitmap image = Picasso.with(this).load(ep.getThumbnail()).resize(size.x, size.y).get();
        Bitmap background = Picasso.with(this).load(ep.getThumbnail()).get();

        final RecommendationBuilder builder = new RecommendationBuilder(this, id);
        builder.setBackgroundPrefix(BACKGROUND_URI_PREFIX);
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder
                .setBackgroundPrefix(BACKGROUND_URI_PREFIX)
                .setIcon(R.mipmap.icon)
                .setColor(getResources().getColor(R.color.brand_color))
                .setTitle(ep.getSeriesTitle())
                .setDescription(ep.getTitle())
                .setImage(image)
                .setBackground(background)
                .setPendingIntent(buildPendingIntent(ep))
                .build();
        manager.notify(RECOMMENDATION_TAG, id, notification);
        Log.d(TAG, "Recommending: " + ep);
    }

    private PendingIntent buildPendingIntent(EpisodeModel ep) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(ContentManager.CONTENT_ID, ep);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DetailsActivity.class);
        stackBuilder.addNextIntent(intent);
        intent.setAction(ep.getHref());

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
