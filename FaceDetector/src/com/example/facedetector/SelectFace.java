package com.example.facedetector;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SelectFace extends Activity {
	
	int i, numFaces;
	float eyesDistance[];
	RectF rect[]; 
	PointF midPoint = new PointF(0, 0);
	FrameLayout fl;
	LinearLayout [] ll; // 버튼을 넣을 레이아웃 배열
	Button btn[];
	Bitmap image;
	Uri imageUri;
	boolean captureMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectface);
		
		Intent intent = getIntent();
		imageUri = Uri.parse(intent.getStringExtra("imageUri"));
		captureMode = intent.getExtras().getBoolean("captureMode");
		
		fl = (FrameLayout)findViewById(R.id.view);
		DrawButtons drawButtons = new DrawButtons(this);
		fl.addView(drawButtons);
	}
	
	@SuppressLint("NewApi")
	public void addBtns()
	{
		btn = new Button[numFaces];
		for(i=0; i<numFaces; i++)
		{	
			ll = new LinearLayout[numFaces];
			ll[i] = new LinearLayout(this);
			ll[i].setOrientation(LinearLayout.VERTICAL);
			
			btn[i] = new Button(this);
			btn[i].setText("face" + (i+1));
			//btn[i].setAlpha((float) 0.8);
			btn[i].setLayoutParams(new LayoutParams((int) (eyesDistance[i]*1.5)*2, (int) (eyesDistance[i]*2.0)*2));
			
			btn[i].setX(rect[i].left);
			btn[i].setY(rect[i].top);
			
			final int x = (int)(rect[i].left);
			final int y = (int)(rect[i].top);
			final int x2 = (int)(rect[i].right);
			final int y2 = (int)(rect[i].bottom);
			
			// 얼굴이 이미지의 가장자리에 있을 때, 검출 된 얼굴이 한개라면 미검출로 판단, 두개 이상이라면 버튼 표시 안함.
			if(x<0 || y<0 || x2>image.getWidth() || y2>image.getHeight())
			{
				if(numFaces == 1)
				{
					Toast.makeText(getBaseContext(), "검출 된 얼굴이 없습니다", Toast.LENGTH_SHORT).show();
					finish();
				}
				else if(numFaces > 1)
					continue;
			}
			
			ll[i].addView(btn[i]);
			fl.addView(ll[i]);
			
			btn[i].setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Button b = (Button)v;
					String text = b.getText().toString();
					Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	public class DrawButtons extends View
	{
		
		public static final int MAX_FACES_COUNT = 100;
		int x, y;
		
		public DrawButtons(Context context) {
			super(context);
		}
		
		@SuppressLint("DrawAllocation")
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			image = MainActivity.image.copy(Bitmap.Config.RGB_565, true);
			cleanBitmap(MainActivity.image);
			
			float imageWidth = image.getWidth();
			float imageHeight = image.getHeight();
			
			Display display = ((WindowManager) SelectFace.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			@SuppressWarnings("deprecation")
			int displayWidth = display.getWidth();
			@SuppressWarnings("deprecation")
			int displayHeight = display.getHeight();
			
			//높이를 화면과 맞췄을때 폭이 화면보다 작거나 같을 경우 높이를 화면의 크기와 맞춘다.
			if ((displayHeight/imageHeight)*imageWidth <= displayWidth)
			{
				image = Bitmap.createScaledBitmap(image, (int)((displayHeight/imageHeight)*imageWidth), displayHeight, false);
			}
			//높이를 화면과 맞췄을때 폭이 화면보다 커질 경우, 혹은 사진의 높이와 폭이 같을때 폭을 화면의 크기와 맞춘다.
			else if ((displayHeight/imageHeight)*imageWidth > displayWidth || imageWidth == imageHeight)
			{
				image = Bitmap.createScaledBitmap(image, displayWidth, (int)((displayWidth/imageWidth)*imageHeight), false);
			}

			canvas.drawBitmap(image, 0, 0, null);
			FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES_COUNT];
			FaceDetector detector = new FaceDetector(
				image.getWidth(),
				image.getHeight(),
				faces.length);
			
			numFaces = detector.findFaces(image, faces);
			rect = new RectF[numFaces];
			eyesDistance = new float[numFaces];
			
			if (numFaces > 0) {
			    for (int i = 0; i < numFaces; i++) {
			    	Face face = faces[i];
			        face.getMidPoint(midPoint);
			        
			        eyesDistance[i] = face.eyesDistance();
			        
			        rect[i] = new RectF();
			        rect[i].left = (float)(midPoint.x - (eyesDistance[i]*1.5)) ;
			        rect[i].top = (float)(midPoint.y - (eyesDistance[i]*2.0)) ;
			        rect[i].right = (float)(midPoint.x + (eyesDistance[i]*1.5)) ;
			        rect[i].bottom = (float)(midPoint.y + (eyesDistance[i]*2.0)) ;
			    }
			    addBtns();
			}
			else if(numFaces == 0)
			{
				Toast.makeText(getBaseContext(), "검출 된 얼굴이 없습니다", Toast.LENGTH_SHORT).show();
				finish();
			}
				
			if(captureMode == true)
			{
				File file = new File(imageUri.getPath());
				// 임시 파일 삭제
				// 싸이메라, 픽스아트 등의 카메라 앱은 쓰레기가 남음
				if(file.exists())
				{
					file.delete();
				}
			}
		}
		
		public void cleanBitmap(Bitmap bitmap) {
			
	        bitmap.recycle();
	        bitmap = null;
	    }
	}
}
