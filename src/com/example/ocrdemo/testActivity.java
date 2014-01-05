package com.example.ocrdemo;

import java.io.File;
import java.io.IOException;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class testActivity extends Activity {

private static final int TAKE_PICTURE_REQUEST = 1;

@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		takePicture();
	}

private void takePicture() {
	Log.d("Start", "Activity started!");
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(intent, TAKE_PICTURE_REQUEST);
}
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	Log.d("Start", "onActivity started");
    if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
        String picturePath = data.getStringExtra(
                CameraManager.EXTRA_PICTURE_FILE_PATH);
        try {
			processPictureWhenReady(picturePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    super.onActivityResult(requestCode, resultCode, data);
}

private void processPictureWhenReady(final String picturePath) throws IOException {
    final File pictureFile = new File(picturePath);

    if (pictureFile.exists()) {
        // The picture is ready; process it.
    	//Log.d("Picture Taken!",picturePath);
    	Log.d("Start", "OCR Started!");
    	boolean _taken = true;
    	String _path = picturePath;
    	//String _datapath = "file:///android_asset/eng.traineddata";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        	
        Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
        //_image.setImageBitmap(bitmap);
        	
        //_field.setVisibility( View.GONE );
    	
    	
    	
    	ExifInterface exif = new ExifInterface(_path);
    	int exifOrientation = exif.getAttributeInt(
    	        ExifInterface.TAG_ORIENTATION,
    	        ExifInterface.ORIENTATION_NORMAL);

    	int rotate = 0;

    	switch (exifOrientation) {
    	case ExifInterface.ORIENTATION_ROTATE_90:
    	    rotate = 90;
    	    break;
    	case ExifInterface.ORIENTATION_ROTATE_180:
    	    rotate = 180;
    	    break;
    	case ExifInterface.ORIENTATION_ROTATE_270:
    	    rotate = 270;
    	    break;
    	}

    	if (rotate != 0) {
    	    int w = bitmap.getWidth();
    	    int h = bitmap.getHeight();

    	    // Setting pre rotate
    	    Matrix mtx = new Matrix();
    	    mtx.preRotate(rotate);

    	    // Rotating Bitmap & convert to ARGB_8888, required by tess
    	    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
    	}
    	bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    	
    	Log.d("Start", "OCR Ended");
    	
    	TessBaseAPI baseApi = new TessBaseAPI();
    	// DATA_PATH = Path to the storage
    	// lang = for which the language data exists, usually "eng"
    	baseApi.init("file:///android_asset/tessdata/eng.traineddata", "eng");
    	// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
    	baseApi.setImage(bitmap);
    	String recognizedText = baseApi.getUTF8Text();
    	baseApi.end();
    	
    	//TextView tv = ( TextView ) findViewById(R.id.editText1);
		//tv.setText("ERROR:YES");
    	Card card1 = new Card(getBaseContext());
		card1.setText("ERROR:YES");
		View card1View = card1.toView();
    	
    	Log.d("Start", recognizedText);
    	
    } else {
        // The file does not exist yet. Before starting the file observer, you
        // can update your UI to let the user know that the application is
        // waiting for the picture (for example, by displaying the thumbnail
        // image and a progress indicator).

        final File parentDirectory = pictureFile.getParentFile();
        FileObserver observer = new FileObserver(parentDirectory.getPath()) {
            // Protect against additional pending events after CLOSE_WRITE is
            // handled.
            private boolean isFileWritten;

            @Override
            public void onEvent(int event, String path) {
                if (!isFileWritten) {
                    // For safety, make sure that the file that was created in
                    // the directory is actually the one that we're expecting.
                    File affectedFile = new File(parentDirectory, path);
                    isFileWritten = (event == FileObserver.CLOSE_WRITE
                            && affectedFile.equals(pictureFile));

                    if (isFileWritten) {
                        stopWatching();

                        // Now that the file is ready, recursively call
                        // processPictureWhenReady again (on the UI thread).
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
									processPictureWhenReady(picturePath);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                            }
                        });
                    }
                }
            }
        };
        observer.startWatching();
    }
}
}
