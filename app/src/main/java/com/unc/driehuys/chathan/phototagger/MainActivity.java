package com.unc.driehuys.chathan.phototagger;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private int currentPhotoSize;

    private SQLiteDatabase db;

    private String currentPhotoPath;

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_capture:
                captureImage();
                break;
            case R.id.btn_load:
                loadImage();
                break;
            case R.id.btn_save:
                saveImage();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    displayImage(currentPhotoPath);

                    EditText tagsInput = (EditText) findViewById(R.id.input_tags);
                    tagsInput.setText("");

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("phototagger.db", MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS photos (path TEXT, size INTEGER, tags TEXT);");
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.unc.driehuys.chathan.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Store the image file's path
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void displayImage(String path) {
        File file = new File(path);
        Bitmap bitmap;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ImageView imgView = (ImageView) findViewById(R.id.current_image);
        imgView.setImageBitmap(bitmap);

        currentPhotoSize = bitmap.getByteCount();

        EditText sizeInput = (EditText) findViewById(R.id.input_size);
        sizeInput.setText(String.valueOf(currentPhotoSize));
    }

    private void loadImage() {
        EditText tagsInput = (EditText) findViewById(R.id.input_tags);
        EditText sizeInput = (EditText) findViewById(R.id.input_size);

        String tags = tagsInput.getText().toString();
        String size = sizeInput.getText().toString();

        Cursor c;

        String sizeQuery = "";
        String tagsQuery = "";

        if (!size.equals("")) {
            int sizeNum = Integer.parseInt(size, 10);
            int maxSize = (int) Math.round(sizeNum * 1.25);
            int minSize = (int) Math.round(sizeNum * 0.75);

            sizeQuery = String.format("size > %d AND size < %d", minSize, maxSize);
        }

        if (!tags.equals("")) {
            String[] tagList = tags.split(";");

            boolean isFirst = true;

            for (String tag : tagList) {
                if (!isFirst) {
                    tagsQuery += " AND ";
                }

                tagsQuery += "tags LIKE '%" + tag + "%'";

                isFirst = false;
            }
        }

        if (!sizeQuery.equals("") && !tagsQuery.equals("")) {
            c = db.rawQuery(
                    "SELECT * FROM photos WHERE " + sizeQuery + " AND " + tagsQuery + " LIMIT 1",
                    new String[] {});
        } else if (!sizeQuery.equals("")) {
            c = db.rawQuery(
                    "SELECT * FROM photos WHERE " + sizeQuery + " LIMIT 1",
                    new String[] {});
        } else if (!tagsQuery.equals("")) {
            c = db.rawQuery(
                    "SELECT * FROM photos WHERE " + tagsQuery + " LIMIT 1",
                    new String[] {});
        } else {
            c = db.rawQuery(
                    "SELECT * FROM photos LIMIT 1",
                    new String[] {});
        }

        if (c.moveToFirst()) {
            currentPhotoPath = c.getString(c.getColumnIndexOrThrow("path"));
            String newTags = c.getString(c.getColumnIndexOrThrow("tags"));

            displayImage(currentPhotoPath);

            tagsInput.setText(newTags);
        } else {
            Toast.makeText(this, "No Results", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        EditText tagsInput = (EditText) findViewById(R.id.input_tags);
        String tags = tagsInput.getText().toString();

        Cursor c = db.rawQuery("SELECT path FROM photos WHERE path = ?", new String[] {currentPhotoPath});

        if (c.getCount() == 0) {
            db.execSQL(
                    "INSERT INTO photos VALUES (?, ?, ?)",
                    new Object[] {
                            currentPhotoPath,
                            currentPhotoSize,
                            tags,
                    });

            Log.v(LOG_TAG, "Inserted new photo: " + currentPhotoPath);
        } else {
            db.execSQL(
                    "UPDATE photos SET tags = ?, size = ? WHERE path = ?",
                    new Object[] {
                            tags,
                            currentPhotoSize,
                            currentPhotoPath,
                    });

            Log.v(LOG_TAG, "Updated photo at: " + currentPhotoPath);
        }

        c.close();
    }
}
