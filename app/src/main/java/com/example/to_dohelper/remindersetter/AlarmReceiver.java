package com.example.to_dohelper.remindersetter;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.to_dohelper.DBHelper.NotesDBHelper;
import com.example.to_dohelper.Dashboard;
import com.example.to_dohelper.Note;
import com.example.to_dohelper.NoteOperations;
import com.example.to_dohelper.R;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotesDBHelper db = new NotesDBHelper(context);
        Note note = db.getNote(NoteOperations.id);
        Intent notificationIntent = new Intent(context, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) NoteOperations.id, notificationIntent, 0);

//        Notification setup
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "todohelper")
                .setSmallIcon(R.drawable.todo)
                .setContentTitle(note.getTitle())
                .setContentText(note.getContent())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify((int) NoteOperations.id, builder.build());

//        Removing Alarm from the database and from the stack
        db.deleteAlarm(note);
        Dashboard.pendingIntentStack.pop();
        Dashboard.idStack.pop();

    }
}
