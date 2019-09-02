package thedemitrius.androidif.glk;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class TextBufferWindowGestureListener implements GLKConstants
{
	private GLK glk;
	private TextBufferWindow win;
	private long timeout;
	private boolean keyboardLocked;
	private TreeMap<Integer, Rect> hyperlinks;
	private boolean hyperlinksActive;

	private MotionEvent pointer1;
	private MotionEvent pointer2;
	
	private Timer longPressTimer;
	private LongPress longPress;
	
	public TextBufferWindowGestureListener(GLK glk,TextBufferWindow win)
	{
		this.glk=glk;
		this.win=win;
		timeout=0;
		
		hyperlinks=new TreeMap<Integer, Rect>();
		hyperlinksActive=false;
		
		longPressTimer=new Timer();
	}
	
	private boolean onScroll(MotionEvent e1, MotionEvent e2)
	{
		if (e1==null||e2==null||(System.currentTimeMillis()-timeout)>250){

			assert e1 != null;
			assert e2 != null;
			float distanceX=e1.getX()-e2.getX();
			float distanceY=e1.getY()-e2.getY();
			
			if (Math.abs(distanceX)>Math.abs(distanceY)){
				//if (Math.abs(distanceX)>ViewConfiguration.getTouchSlop()){
					timeout=System.currentTimeMillis();
					if (distanceX>0)
						win.pageForward();
					else
						win.pageBack();}
				//else
				//	return (false);}

			else{
				//if (Math.abs(distanceY)>ViewConfiguration.getTouchSlop()){
					timeout=System.currentTimeMillis();
					if (!win.helpUp()){
						keyboardLocked=(distanceY>0);
						glk.toggleKeyboard(keyboardLocked);//}
				}
				//else
				//	return (false);
			}
		}

		return (true);
	}

	private boolean onSingleTapConfirmed(MotionEvent e)
	{
		if (e==null)
			return (false);

		if (hyperlinksActive&&!hyperlinks.isEmpty()){
			int lastLink= hyperlinks.lastKey();
			for (int i=0;i<=lastLink;i++){
				if (hyperlinks.containsKey(i)){
					Rect rect=hyperlinks.get(i);
					if (e.getX()>=rect.left&&e.getY()>=rect.top&&e.getX()<=rect.right&&e.getY()<=rect.bottom){
						hyperlinksActive=false;
						glk.resume(EVTYPE_HYPERLINK, win.getId(), i, 0);
						return (true);}
				}
			}
		}


		if (e.getX()<(win.getWidth()/5))
			win.pageBack();
		else
			win.pageForward();
		return (true);
	}
	
	private boolean onGrowOrShrink(MotionEvent e1, MotionEvent e2)
	{
		if (e1==null||e2==null||(System.currentTimeMillis()-timeout)<=250)
			return (false);
			
		double distance1=Math.sqrt(Math.pow(e1.getX(0)-e1.getX(1), 2)+Math.pow(e1.getY(0)-e1.getY(1),2));
		double distance2=Math.sqrt(Math.pow(e2.getX(0)-e2.getX(1), 2)+Math.pow(e2.getY(0)-e2.getY(1),2));
			
		//if (Math.abs(distance1-distance2)<=ViewConfiguration.getTouchSlop())
		//	return (false);
		
		if (distance1>distance2)
			glk.shrinkFont();
		else
			glk.growFont();
		
		return (true);
	}
	
	boolean getKeyboardLocked()
	{
		return keyboardLocked;
	}
	
	void setKeyboardLocked(boolean keyboardLocked)
	{
		this.keyboardLocked=keyboardLocked;
	}
	
	void clearHyperlinks()
	{
		hyperlinks.clear();
	}
	
	boolean getHyperlinksActive()
	{
		return (hyperlinksActive);
	}
	
	void toggleHyperlinks(boolean hyperlinksActive)
	{
		this.hyperlinksActive=hyperlinksActive;
	}
	
	void addHyperlink(int id,Rect loc)
	{
		if (hyperlinks.containsKey(id)){
			Rect rect=hyperlinks.get(id);
			rect.bottom=loc.bottom;}
		else{ 
			hyperlinks.put(id, loc);}
	}
	
	boolean handleTouchEvent(MotionEvent e)
	{
		int count=e.getPointerCount();
		
		if (count<=0||count>2){
			pointer1=null;
			pointer2=null;
			return (false);}

		int action=e.getActionMasked();
			
		switch(action){
		case MotionEvent.ACTION_UP:
			longPress.cancel();
			if (pointer1!=null)
				onSingleTapConfirmed(e);
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL:
			longPress.cancel();
			pointer1=null;
			pointer2=null;
			return (true);
		case MotionEvent.ACTION_DOWN:
			pointer2=null;
			pointer1=MotionEvent.obtain(e);
			longPress=new LongPress();
			longPressTimer.schedule(longPress, ViewConfiguration.getLongPressTimeout());
			return (true);
		case MotionEvent.ACTION_POINTER_DOWN:
			pointer2=MotionEvent.obtain(e);
			longPress.cancel();
			return (true);
		case MotionEvent.ACTION_MOVE:
			if (pointer2!=null){
				if (onGrowOrShrink(pointer2,e))
					pointer2=MotionEvent.obtain(e);}
			else
				if (pointer1!=null)
					if (onScroll(pointer1,e))
						pointer1=null;
			return (true);
		default:
			return (false);}
	}
	
	class LongPress extends TimerTask
	{
		public void run() 
		{
			if (!win.helpUp()&&pointer1!=null&&pointer2==null)
				win.post(new Runnable(){
					public void run(){
						glk.openMenu();}});
			pointer1=null;
			pointer2=null;
		}
	}
	
	public void finalize()
	{
		longPressTimer.cancel();
	}
}	
