package io.github.xwz.abciview.content;

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
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.activities.DetailsActivity;
import io.github.xwz.abciview.models.EpisodeModel;

public class UpdateRecommendationsService extends IntentService {
    private static final String TAG = "UpdateRecommendations";

    private static final String RECOMMENDATION_TAG = "io.github.xwz.abciview.RECOMMENDATION_TAG";

    public UpdateRecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Updating recommendation cards");
        List<EpisodeModel> shows = ContentManager.getInstance().getRecommendations();
        if (shows.size() == 0) {
            return;
        }
        int id = 0;
        for (EpisodeModel ep : shows) {
            try {
                buildNotification(ep, id++);
            } catch (IOException e) {
                Log.e(TAG, "Error:" + e.getMessage());
            }
        }
    }

    private void buildNotification(EpisodeModel ep, final int id) throws IOException {
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification_view);

        Point size = new Point(getResources().getDimensionPixelSize(R.dimen.card_width),
                getResources().getDimensionPixelSize(R.dimen.card_height));
        Bitmap image = Picasso.with(this).load(ep.getThumbnail()).resize(size.x, size.y).get();

        final RecommendationBuilder builder = new RecommendationBuilder(this);
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder
                .setTitle(ep.getSeriesTitle())
                .setDescription(ep.getTitle())
                .setImage(image)
                .setBackground(ep.getThumbnail())
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
