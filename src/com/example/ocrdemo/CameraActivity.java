package com.example.ocrdemo;
import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.glass.app.Card;

import junit.framework.Assert;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraActivity extends Activity implements OnClickListener {

	private static final String TAG = CameraActivity.class.getSimpleName();

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private static final String KEY_FILE_URI = "com.hellocamera.camera_file_uri";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	private Uri mFileUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// restore the saved camera URI if the activity was killed during the
		// camera intent
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_FILE_URI)) {
				mFileUri = Uri
						.parse(savedInstanceState.getString(KEY_FILE_URI));
			} else {
				mFileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a
																	// file to
																	// save the
																	// image
			}
		} else {
			mFileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file
																// to save the
																// image
		}

		Button btnTakePicture = (Button) findViewById(R.id.btn_take_picture);
		btnTakePicture.setOnClickListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putString(KEY_FILE_URI, mFileUri.toString());
	}

	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(int type) {
		File file = getOutputMediaFile(type);
		Assert.assertNotNull("getOutputMediaFileUri file", file);
		return Uri.fromFile(file);
	}

	/** Create a File for saving an image or video */
	private File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		String state = Environment.getExternalStorageState();
		Assert.assertTrue("external media is mounted",
				TextUtils.equals(state, Environment.MEDIA_MOUNTED));

		File mediaStorageDir;
		if (Build.VERSION.SDK_INT > 8) {
			mediaStorageDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		} else {
			mediaStorageDir = new File(
					Environment.getExternalStorageDirectory(), "Pictures");
		}

		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory()) {
				Log.v(TAG, "directory is ok");
			} else {
				Assert.fail("failed to create directory");
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			Assert.fail("Invalid media type");
			return null;
		}
		Assert.assertNotNull("media file is not null", mediaFile);
		Log.v(TAG, "will store file at " + mediaFile.toString());
		return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_take_picture:
			Assert.assertNotNull("file uri not null before firing intent",
					mFileUri);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// this is the file that the camera app will write to
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// In most cases, data should be null because the file location
				// was passed as an extra to the original intent that started
				// the camera
				// not all phones comply with the expected behavior and instead
				// pass back some data.
				// If no file is specified in the original intent, a thumbnail
				// is passed back
				// here as data.
				if (data != null) {
					if (data.getData() != null) {
						Log.v(TAG, "intent data: " + data.getData().toString());
					}
					if (data.getAction() != null) {
						Log.v(TAG, "intent action: "
								+ data.getAction().toString());
					}
					if (data.getExtras() != null) {
						Log.v(TAG, "intent extras: "
								+ data.getExtras().toString());
					}
				}

				// this is for image preview ( to be commented in end) 
				ImageView imageView = (ImageView) findViewById(R.id.image_view);
				File murl = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				String mediaFile = murl.toString();
				Assert.assertNotNull("file uri in onActivityResult", mFileUri);
				Log.v(TAG, "stored file name is " + mFileUri.toString());
				File file = getFileFromUri();
				if (file != null) {

					// do it if you want to set size
					Bitmap bm = decodeSampledBitmapFromFile(file, 500, 500);
					imageView.setImageBitmap(bm);
					ocr result = new ocr();
					String final_string = result.ocrconvert(mediaFile.toString(),bm);
					TextView tv = ( TextView ) findViewById(R.id.editText1);
					tv.setText(final_string);
					//Card card1 = new Card(getBaseContext());
					//card1.setText("ERROR:YES");
					//card1.setFootnote("I'm the footer!");
					// Don't call this if you're using TimelineManager
					//View card1View = card1.toView();
					
				}
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}
		}
	}

	/**
	 * return a file based on the mFileUri that always has the format
	 * file://xyz/xyz
	 */
	private File getFileFromUri() {
		if (mFileUri != null) {
			try {
				URI uri;
				if (mFileUri.toString().startsWith("file://")) {
					// normal path
					uri = URI.create(mFileUri.toString());
				} else {
					// support path
					uri = URI.create("file://" + mFileUri.toString());
				}
				File file = new File(uri);
				if (file != null) {
					if (file.canRead()) {
						return file;
					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Prevent bitmap out of memory exceptions by scaling the image
	 */
	public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth,
			int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	}

	/** Calculate the scaling factor */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

}
