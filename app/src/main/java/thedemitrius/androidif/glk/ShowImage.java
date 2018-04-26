package thedemitrius.androidif.glk;

import thedemitrius.androidif.Init;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class ShowImage extends Activity
{
	private Bitmap image; 
	private ImageView imageView;
	
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
    	
    	int imageNum=getIntent().getIntExtra("image",0);
    	if (imageNum==0){
			setResult(Activity.RESULT_CANCELED);
    		finish();
    		return;}

		image=BitmapFactory.decodeStream(getClass().getResourceAsStream(Init.storyURL+imageNum+".pic"));
    	if (image==null){
    		setResult(Activity.RESULT_CANCELED);
    		finish();
    		return;}

		imageView=new ImageView(this);
    	setContentView(imageView);
    }

    class ImageView extends View implements View.OnTouchListener
    {

		public ImageView(Context context) 
		{
			super(context);
			
			setOnTouchListener(this);
		}
		
		public void onDraw(Canvas g)
		{
			Matrix matrix=new Matrix();
			
			if (getWidth()>getHeight()){
				if (image.getWidth()<=image.getHeight()){
					matrix.setRotate(-90);
					matrix.postTranslate(0, image.getWidth());
					matrix.postScale((float)getWidth()/(float)image.getHeight(),(float)getHeight()/(float)image.getWidth());}
				else{
					matrix.setScale((float)getWidth()/(float)image.getWidth(),(float)getHeight()/(float)image.getHeight());}
			}
			else{
				if (image.getHeight()<=image.getWidth()){
					matrix.setRotate(-90);
					matrix.postScale((float)getWidth()/(float)image.getHeight(),(float)getHeight()/(float)image.getWidth());}
				else{
					matrix.setScale((float)getWidth()/(float)image.getWidth(),(float)getHeight()/(float)image.getHeight());}
			}
			
			g.drawBitmap(image, matrix, new Paint());
		}

		public boolean onTouch(View v, MotionEvent event) 
		{
			setResult(Activity.RESULT_CANCELED);
			finish();
			return (true);
		}
    }
}
