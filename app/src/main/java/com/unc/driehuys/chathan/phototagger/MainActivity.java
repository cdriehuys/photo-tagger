package com.unc.driehuys.chathan.phototagger;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;

    public void onClick(View view) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("phototagger.db", MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS photos (path TEXT, size INTEGER, tags TEXT);");
    }
}
