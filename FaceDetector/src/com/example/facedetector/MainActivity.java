package com.example.facedetector;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	int REQUEST_CAMERA = 1111;
	int OPEN_IMAGE = 2222;
	
	Button btnGallery, btnCamera;
	String imagePath;
	
	Uri imageUri = Uri.parse("");
	static Bitmap image;
	boolean captureMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnGallery = (Button) findViewById(R.id.btnGallery);
		btnGallery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 사진첩 열기
				Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				Intent intent = new Intent(Intent.ACTION_PICK, uri);
				startActivityForResult(intent, OPEN_IMAGE);
			}
		});
		btnCamera = (Button) findViewById(R.id.btnCamera);
		btnCamera.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
				imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

				// 카메라 호출
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				MainActivity.this.startActivityForResult(intent, REQUEST_CAMERA);
			}
		});
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK)
		{
			try
			{
				captureMode = true;
				imagePath = imageUri.getPath();
				image = getScaledBitmap(this, imagePath);
				
				ExifInterface exif = new ExifInterface(imagePath);
				int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				int exifDegree = exifOrientationToDegrees(exifOrientation);
								
				image = rotate(image, exifDegree);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			Intent intent = new Intent(this, SelectFace.class);
			intent.putExtra("imageUri", imageUri.toString());
			intent.putExtra("captureMode", captureMode);
			startActivity(intent);
		}
			
		else if(requestCode == OPEN_IMAGE && resultCode == RESULT_OK)
		{
			try
			{
				captureMode = false;
				Uri imageUri = data.getData();
			
				imageUri = convertContentToFileUri(this, imageUri); // content uri -> file uri
				imagePath = imageUri.getPath();
				image = getScaledBitmap(this, imagePath);
	
				ExifInterface exif = new ExifInterface(imagePath);
				int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				int exifDegree = exifOrientationToDegrees(exifOrientation);

				image = rotate(image, exifDegree);
			}
			
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Intent intent = new Intent(this, SelectFace.class);
			intent.putExtra("imageUri", imageUri.toString());
			intent.putExtra("captureMode", captureMode);
			startActivity(intent);
		}
	}
	
	// bitmap 회전 각도 얻기
	public int exifOrientationToDegrees(int exifOrientation)
	{
		if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
		{
			return 90;
		}
		else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
		{
			return 180;
		}
		else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
		{
			return 270;
		}
		return 0;
	}
		
	// bitmap 회전각도에 따라 회전
	public Bitmap rotate(Bitmap bitmap, int degrees)
	{
		if(degrees != 0 && bitmap != null)
		{
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
			if(bitmap != converted)
			{
				bitmap.recycle();
				bitmap = converted;
			}
		}
		return bitmap;
	}

	// bitmap 사이즈 조절 (메모리 부족 방지)
	@SuppressWarnings("deprecation")
	public static Bitmap getScaledBitmap(Activity a, String path) {
		Display display = a.getWindowManager().getDefaultDisplay();
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;

		int inSampleSize = 1;
		if (srcHeight > destHeight || srcWidth > destWidth) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round((float)srcHeight / (float)destHeight);
			} else {
				inSampleSize = Math.round((float)srcWidth / (float)destWidth);
			}
		}

		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}
	
	// content uri -> file uri
	public Uri convertContentToFileUri(Context ctx, Uri uri) throws Exception {
		Cursor cursor = null;
		try {
			cursor = ctx.getContentResolver().query(uri, null, null, null, null);
			cursor.moveToNext();
			return Uri.fromFile(new File(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))));
		}
		finally {
			if(cursor != null)
				cursor.close();
		}
	}
}