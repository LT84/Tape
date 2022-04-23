package com.project.tape.SecondaryClasses;


import static com.project.tape.Fragments.FragmentGeneral.art;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.project.tape.ItemClasses.Song;
import com.project.tape.R;
import com.project.tape.Services.NotificationActionService;


public class CreateNotification {

    public static final String CHANNEL_ID = "channel";

    public static final String ACTION_PREVIOUS = "actionPrevious";
    public static final String ACTION_PLAY = "actionPlay";
    public static final String ACTION_NEXT = "actionNext";

    public static Notification notification;

    public static void createNotification(Context context, Song song, int playButton, int position, int size) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cover);

            //Previous btn
            PendingIntent pendingIntentPrevious;
            int drw_previous;
            Intent intentPrevious = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PREVIOUS);
            pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                    intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_previous = R.drawable.ic_previous_song;

            //Play btn
            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                    intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            //Next btn
            PendingIntent pendingIntentNext;
            int drw_next;
            Intent intentNext = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_NEXT);
            pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                    intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_next = R.drawable.ic_next_song;

            //Create notification
            if (art != null) {
                notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.tape_icon)
                        .setContentTitle(song.getTitle())
                        .setContentText(song.getArtist())
                        .setLargeIcon(BitmapFactory.decodeByteArray(art, 0, art.length, null))
                        .setOnlyAlertOnce(true)
                        .setShowWhen(false)
                        .addAction(drw_previous, "Previous", pendingIntentPrevious)
                        .addAction(playButton, "Play", pendingIntentPlay)
                        .addAction(drw_next, "Next", pendingIntentNext)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOngoing(true)
                        .build();
            } else {
                notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.tape_icon)
                        .setContentTitle(song.getTitle())
                        .setContentText(song.getArtist())
                        .setLargeIcon(icon)
                        .setOnlyAlertOnce(true)
                        .setShowWhen(false)
                        .addAction(drw_previous, "Previous", pendingIntentPrevious)
                        .addAction(playButton, "Play", pendingIntentPlay)
                        .addAction(drw_next, "Next", pendingIntentNext)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOngoing(true)
                        .build();
            }
            notificationManagerCompat.notify(1, notification);
        }
    }


}
