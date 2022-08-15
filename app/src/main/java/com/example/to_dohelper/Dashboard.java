package com.example.to_dohelper;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dohelper.DBHelper.NotesDBHelper;

import java.util.List;
import java.util.Stack;

public class Dashboard extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    Adapter adapter;
    List<Note> notes;
    public static Stack<PendingIntent> pendingIntentStack = new Stack<>();
    public static Stack<Long> idStack = new Stack<>();
    public static int listSize;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NotesDBHelper db = new NotesDBHelper(Dashboard.this);
        notes = db.getNotes();
        recyclerView = findViewById(R.id.NotesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(Dashboard.this));
        adapter = new Adapter(Dashboard.this,notes);
        recyclerView.setAdapter(adapter);
        listSize = adapter.getItemCount();
    }

//    Accessing Menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_notes,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add){
            Intent intent = new Intent(Dashboard.this, NoteOperations.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}