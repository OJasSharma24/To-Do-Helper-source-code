package com.example.to_dohelper.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.to_dohelper.DBCredentials.DBParameters;
import com.example.to_dohelper.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesDBHelper extends SQLiteOpenHelper {
    public NotesDBHelper(Context context) {
        //Database Creation
        super(context, DBParameters.DB_NAME, null, DBParameters.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Table Creation
        String create_query = "CREATE TABLE "+DBParameters.TABLE_NAME+"("+DBParameters.KEY_ID+" INTEGER PRIMARY KEY  , "+DBParameters.KEY_TITLE+" TEXT, "+DBParameters.KEY_CONTENT+" TEXT,"+DBParameters.KEY_TIME+" TEXT)";
        sqLiteDatabase.execSQL(create_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(oldVersion >= newVersion){
            return;
        }
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+DBParameters.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    //Method for adding new notes
    public long addNote(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TITLE,note.getTitle());
        values.put(DBParameters.KEY_CONTENT,note.getContent());
        return db.insert(DBParameters.TABLE_NAME, null, values);
    }

    //Method for getting a single note
    public Note getNote(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+DBParameters.TABLE_NAME+" WHERE "+DBParameters.KEY_ID+"=?",new String[]{String.valueOf(id)});
        if(cursor!=null){
            cursor.moveToFirst();
        }
        assert cursor != null;
        Note note = new Note(cursor.getLong(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
        cursor.close();
        return note;
    }

    //Method for getting a list of notes
    public List<Note> getNotes(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Note> listOfNotes = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM "+DBParameters.TABLE_NAME+" ORDER BY "+DBParameters.KEY_ID+" DESC",null);
        if(cursor.moveToFirst()){
            do {
                Note note = new Note();
                note.setId(cursor.getLong(0));
                note.setTitle(cursor.getString(1));
                note.setContent(cursor.getString(2));

                listOfNotes.add(note);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return listOfNotes;
    }

    //Method for updating note
    public long updateNote(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TITLE,note.getTitle());
        values.put(DBParameters.KEY_CONTENT,note.getContent());
        return db.update(DBParameters.TABLE_NAME, values, DBParameters.KEY_ID+"=?",new String[]{String.valueOf(note.getId())});
    }

    //Method for deleting a note
    public void deleteNote(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBParameters.TABLE_NAME, DBParameters.KEY_ID+"=?",new String[]{String.valueOf(id)});
        db.close();
    }

    //Method for deleting an alarm
    public long deleteAlarm(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TIME, " ");
        return db.update(DBParameters.TABLE_NAME, values, DBParameters.KEY_ID+"=? AND "+DBParameters.KEY_TITLE+"=?",new String[]{String.valueOf(note.getId()), note.getTitle()});

    }

    //Method for inserting an alarm
    public long setReminderTime(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TIME, note.getTime());
        return db.update(DBParameters.TABLE_NAME, values, DBParameters.KEY_ID+"=? AND "+DBParameters.KEY_TITLE+"=?",new String[]{String.valueOf(note.getId()), note.getTitle()});
    }
}
