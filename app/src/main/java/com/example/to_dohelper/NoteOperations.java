package com.example.to_dohelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.to_dohelper.DBHelper.NotesDBHelper;
import com.example.to_dohelper.databinding.ActivityNoteOperationsBinding;
import com.example.to_dohelper.remindersetter.AlarmReceiver;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

public class NoteOperations extends AppCompatActivity {
//    Variable Declaration
    private ActivityNoteOperationsBinding binding;
    private MaterialTimePicker picker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    public static long id;
    private NotesDBHelper db = new NotesDBHelper(NoteOperations.this);
    private Note note;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteOperationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("New Note");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        createNotificationChannel();


//        Setting alarm buttons disabled by default
        binding.setReminder.setEnabled(false);
        binding.setReminder.setBackgroundColor(getResources().getColor(R.color.grey));
        binding.setTime.setEnabled(false);
        binding.setTime.setColorFilter(getResources().getColor(R.color.grey));
        binding.deleteAlarm.setEnabled(false);
        binding.deleteAlarm.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        id = intent.getLongExtra("ID",0);

//        Checking if the activity is opened with the data stored in a database
        if(id > 0){
            note = db.getNote(id);
            getSupportActionBar().setTitle(note.getTitle());
            binding.noteTitle.setText(note.getTitle());
            binding.noteContent.setText(note.getContent());

//            Displaying alarm time on TextView, if present in the database
            if(note.getTime() != null && binding.alarmTime.length() == 1){
                binding.alarmTime.setText(note.getTime());
            }
//            Enabling set time button
            binding.setTime.setEnabled(true);
            binding.setTime.setColorFilter(getResources().getColor(R.color.teal_700));
        }
//        Enabling delete alarm button
        if(binding.alarmTime.length() > 1){
            binding.deleteAlarm.setEnabled(true);
            binding.deleteAlarm.setVisibility(View.VISIBLE);
        }

        binding.setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
//                  Enabling set remainder button
                if(binding.alarmTime.getText()!= " "){
                    binding.setReminder.setEnabled(true);
                    binding.setReminder.setBackgroundColor(getResources().getColor(R.color.teal_700));

                }
            }
        });

        binding.setReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimePicker();
            }
        });

        binding.deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
                if(binding.alarmTime.getText() != " "){
                    long i = db.deleteAlarm(note);
                    if(i > 0){
                        Toast.makeText(NoteOperations.this,"Alarm Canceled", Toast.LENGTH_SHORT).show();
                        Intent dashboardIntent = new Intent(NoteOperations.this, Dashboard.class);
                        startActivity(dashboardIntent);
                    }

                }
            }
        });

         binding.noteTitle.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

             }

             @Override
             public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                 if(charSequence.length() != 0){
                     getSupportActionBar().setTitle(charSequence);
                 }
             }

             @Override
             public void afterTextChanged(Editable editable) {

             }
         });

    }
//    Method to cancel the alarm
    public void cancelAlarm() {

        Intent intent = new Intent(this, AlarmReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(this,(int)id,intent,0);

        if(binding.alarmTime.getText() != " "){
            if(alarmManager == null){
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            }
            alarmManager.cancel(pendingIntent);
        }

    }
//    Method to set the time picker
    private void setTimePicker() {

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

//        Code for checking is there is any alarm already exist.

        if (Dashboard.pendingIntentStack.size() < 1) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, 0);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Dashboard.pendingIntentStack.push(pendingIntent);
            Dashboard.idStack.push(id);
            note.setTime(binding.alarmTime.getText().toString());
            long i = db.setReminderTime(note);
            if(i > 0){
                Toast.makeText(NoteOperations.this,"Alarm set Successfully", Toast.LENGTH_SHORT).show();
                Intent dashboardIntent = new Intent(NoteOperations.this, Dashboard.class);
                startActivity(dashboardIntent);
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteOperations.this);
            builder.setMessage("There is already one alarm set for one note. If you want to set alarm for this note press Ok")
                    .setCancelable(false)
//                    Code to override an alarm
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cancelAlarm();
                            NotesDBHelper tempDB = new NotesDBHelper(NoteOperations.this);
                            Note tempNote = tempDB.getNote(Dashboard.idStack.get(0));
                            tempDB.deleteAlarm(tempNote);
                            Dashboard.idStack.pop();
                            Dashboard.pendingIntentStack.pop();
                            setTimePicker();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.cancel();

                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

//    Method to show the time picker
    private void showTimePicker() {

        picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Reminder Time")
                .build();

        picker.show(getSupportFragmentManager(), "todohelper");

        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(picker.getHour() > 12){
                    binding.alarmTime.setText(
                            String.format("%02d",(picker.getHour()-12))+" : "+String.format("%02d",picker.getMinute())+" PM"
                    );
                }
                else if(picker.getHour() == 12){
                    binding.alarmTime.setText("12 : "+picker.getMinute()+" PM");
                }
                else if(picker.getHour() == 0){
                    binding.alarmTime.setText("12 : "+picker.getMinute()+" AM");
                }
                else {
                    binding.alarmTime.setText(picker.getHour()+" : "+picker.getMinute()+" AM");
                }

                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,picker.getHour());
                calendar.set(Calendar.MINUTE, picker.getMinute());
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);
            }
        });
    }



    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "notesReminderChannel";
            String description = "Channel for Note Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("todohelper", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_notes,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        Code for deleting note
        if (item.getItemId() == R.id.delete) {
            db.deleteNote(id);
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
        }
//        Code for saving new note or changes in stored note
        if (item.getItemId() == R.id.save) {
            if(id > 0 && binding.noteTitle.getText().length() != 0){
                note = new Note(id,binding.noteTitle.getText().toString(),binding.noteContent.getText().toString());
                note.setTitle(binding.noteTitle.getText().toString());
                note.setContent(binding.noteContent.getText().toString());
                long ID = db.updateNote(note);
                if(ID == note.getId()){
                    Toast.makeText(this,"Note Updated.",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"Unable to Update Note.",Toast.LENGTH_SHORT).show();
                }
                startActivity(new Intent(getApplicationContext(), Dashboard.class));

            } else if(binding.noteTitle.getText().length() != 0) {
                note = new Note(binding.noteTitle.getText().toString(),binding.noteContent.getText().toString());
                db.addNote(note);
                Toast.makeText(this, "Note is Saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NoteOperations.this, Dashboard.class);
                startActivity(intent);
            }else {
                binding.noteTitle.setError("Title Can not be Blank");
            }

        }
        return super.onOptionsItemSelected(item);
    }
}