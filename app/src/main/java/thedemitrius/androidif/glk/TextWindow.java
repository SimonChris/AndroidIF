package thedemitrius.androidif.glk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import thedemitrius.androidif.util.DataTransforms;

public abstract class TextWindow extends Window
{
	protected int avgCharWidth;
	protected int avgCharHeight;
	
	protected WindowStream stream;
	
	protected int curStyle;
	
	protected Bitmap backStore;
	protected boolean paintNeeded;

	
	TextWindow(Context context,int id,int method, int size, int rock, boolean key, WindowStream stream)
	{
		super(context,id, method, size, rock, key);
		
		this.stream=stream;
		stream.setWindow(this);
		
		updateCharSizes();
		
		if ((method&WINMETHOD_FIXED)==WINMETHOD_FIXED){
			if ((method&WINMETHOD_ABOVE)==WINMETHOD_ABOVE){
				this.size=(this.size*avgCharHeight)+(avgCharHeight/3);}
			else{
				this.size*=avgCharWidth;}
		}

		curStyle=STYLE_NORMAL;
		
		backStore=null;
		paintNeeded=false;
	}
	
	WindowStream getStream()
	{
		return (stream);
	}
	
	abstract void putString(String s);
	
	void getSize(byte[] array1,int widthPtr,byte[] array2,int heightPtr)
	{
		if (widthPtr>=0)
			DataTransforms.putInt(array1, widthPtr, (getWidth()/avgCharWidth));
		
		if (heightPtr>=0)
			DataTransforms.putInt(array2, heightPtr, (getHeight()/avgCharHeight));
	}
	
	abstract void setStyle(int style);
	
	void setEchoStream(Stream echo)
	{
		stream.setEchoStream(echo);
	}
	
	void setSize(int size)
	{
		this.size=size;
		
		if ((method&WINMETHOD_FIXED)==WINMETHOD_FIXED){
			if ((method&WINMETHOD_ABOVE)==WINMETHOD_ABOVE){
				this.size=(this.size*avgCharHeight)+(avgCharHeight/3);}
			else{
				this.size*=avgCharWidth;}
		}
	}

	void updateCharSizes()
	{
		avgCharWidth=stream.getWidth('A',STYLE_NORMAL);
		avgCharHeight=stream.getHeight(STYLE_NORMAL);
	}
	
	protected void checkBackStore()
	{
		if (backStore==null){
			backStore=Bitmap.createBitmap(Math.max(getMeasuredWidth(), 1),
                    Math.max(getMeasuredHeight(), 1), Bitmap.Config.ARGB_8888);}

		else{
			if (getWidth() > 0 && getHeight() > 0 && (backStore.getWidth()!=getWidth()||backStore.getHeight()!=getHeight())){
				Bitmap newBackStore=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
				Canvas g=new Canvas(newBackStore);
				g.drawColor(stream.getBackColor(STYLE_NORMAL));
				g.drawBitmap(backStore,0,0,new Paint());
				backStore.recycle();
				backStore=newBackStore;
				paintNeeded=true;}
		}
	}
	
	public void onDraw(Canvas g)
	{
		if (paintNeeded){
			checkBackStore();
			g.drawBitmap(backStore,0,0,new Paint());}
	}

	void setHyperlink(int linkVal)
	{}
}


