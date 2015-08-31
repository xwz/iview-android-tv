package io.github.xwz.abciview.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.content.UpdateRecommendationsService;

public class MainActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent recommendationIntent = new Intent(this, UpdateRecommendationsService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0, recommendationIntent, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, alarmIntent);
    }
}