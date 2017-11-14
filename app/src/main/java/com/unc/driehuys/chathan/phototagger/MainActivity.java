package com.unc.driehuys.chathan.phototagger;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private int currentPhotoSize;

    private SQLiteDatabase db;

    private String currentPhotoPath;

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_capture:
                captureImage();
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
                    final File file = new File(currentPhotoPath);
                    try {
                        Bitmap captureBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                        displayImage(captureBmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    private void displayImage(Bitmap bitmap) {
        ImageView imgView = (ImageView) findViewById(R.id.current_image);
        imgView.setImageBitmap(bitmap);

        currentPhotoSize = bitmap.getByteCount();
    }

    private void saveImage() {
        String tags = "";

        Object[] options = {
                currentPhotoPath,
                currentPhotoSize,
                tags,
        };

        db.execSQL("INSERT INTO photos VALUES (?, ?, ?)", options);
    }
}
