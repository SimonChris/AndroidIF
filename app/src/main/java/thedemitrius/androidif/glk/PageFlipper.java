package thedemitrius.androidif.glk;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class PageFlipper
{
	private Bitmap source;
	private View win;
	private boolean forward;
	
	private Activity context;
	private Timer timer;
	private Canvas g;
	
	private int increment;
	private int pos;
	
	PageFlipper(Activity context,Bitmap source,Bitmap target,View win,boolean forward)
	{
		this.source=source;
		this.win=win;
		this.forward=forward;
		this.context=context;
		
		timer=new Timer();
		g=new Canvas(target);
		
		increment=target.getWidth()/10;
		pos=target.getWidth();
		
		flip();
	}

	public void flip() 
	{
		context.runOnUiThread(new Runnable(){
			public void run(){
				boolean done;

				pos=Math.max(0, pos-increment);

				if (forward)
					done=flipForward();
				else
					done=flipBackward();

				win.postInvalidate();

				if (!done)
					timer.schedule(new FlipperTask(), 5);
				else
					timer.cancel();}
		});
	}

	private boolean flipForward()
	{
		g.drawBitmap(source, pos, 0, new Paint());
		
		return (pos==0);
	}
	
	private boolean flipBackward()
	{
		Rect rect=new Rect(pos,0,source.getWidth(),source.getHeight());
		g.drawBitmap(source, rect, new Rect(0,0,rect.width(), rect.height()), new Paint());
		
		return (pos==0);
	}
	
	class FlipperTask extends TimerTask
	{
		public void run()
		{
			flip();
		}
	}
}
