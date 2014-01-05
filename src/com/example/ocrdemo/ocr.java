package com.example.ocrdemo;


import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

public class ocr {
	 public static final String DATA_PATH = Environment
             .getExternalStorageDirectory().toString() + "/tesseract/";
	String ocrconvert(String file,Bitmap bm){
		
		BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inSampleSize = 4;
	    Matrix mtx = new Matrix();
	    Bitmap bitmap = BitmapFactory.decodeFile(file, options );
		TessBaseAPI baseApi = new TessBaseAPI();
		System.out.println("1st");
		
		//InputStream in=getAssets().open("eng.traineddata");
		baseApi.init("file:///android_asset/tessdata/eng.traineddata", "eng");
		
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, 500, 500,mtx, false);
		baseApi.setImage(bitmap);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		System.out.println("2st");
		//textmanipulation a = new textmanipulation();
		//String result = a.manipulation(recognizedText);
		return recognizedText;
	}
}
