package sichris.androidDemo.glk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

class TextGridWindow extends TextWindow
{
	private int x,y;
	
	private boolean cleared;

	TextGridWindow(Context context,int id, int method, int size, int rock, boolean key, WindowStream stream) 
	{
		super(context,id, method, size, rock, key, stream);
		
		x=0;
		y=0;
		cleared=true;
	}
	
	void putString(String s)
	{
		checkBackStore();
		Canvas g=new Canvas(backStore);
		Paint font=stream.getFont(curStyle);
		
		int pos=0;
		int nextLF;
		
		while ((nextLF=s.indexOf('\n',pos))!=-1){
			String drawnText=s.substring(pos,nextLF);
			if (!cleared){
				g.clipRect(new Rect((x*avgCharWidth), (y*avgCharHeight+(avgCharHeight/5)), drawnText.length()*avgCharWidth, avgCharHeight+(avgCharHeight/5)));
				g.drawColor(stream.getBackColor(STYLE_NORMAL));
				g.clipRect(new Rect(0, 0, getWidth(), getHeight()));}
			g.drawText(drawnText,(x*avgCharWidth),((y+1)*avgCharHeight), font);
			x=0;
			y++;
			pos=nextLF+1;}

		if (pos<(s.length())){
			String drawnText=s.substring(pos);
			if (!cleared){
				if (!cleared){
					int left=x*avgCharWidth; 
					int top=y*avgCharHeight+(avgCharHeight/5);
					int right=left+drawnText.length()*avgCharWidth;
					int bottom=top+avgCharHeight+(avgCharHeight/5);
					g.clipRect(left,top,right,bottom);
					g.drawColor(stream.getBackColor(STYLE_NORMAL));
					g.clipRect(0,0,getWidth(),getHeight());}
			}
			g.drawText(drawnText, (x*avgCharWidth), ((y+1)*avgCharHeight), font);
			x+=s.substring(pos).length();}

		paintNeeded=true;
	}
	
	void clear(boolean helpActive)
	{
		if (helpActive)
			return;
		
		checkBackStore();
		
		Canvas g=new Canvas(backStore);
		g.drawColor(stream.getBackColor(STYLE_NORMAL));
		
		x=0;
		y=0;
		cleared=true;
		
		paintNeeded=true;
	}
	
	void moveCursor(int x,int y)
	{
		this.x=x;
		this.y=y;
	}
	
	void setStyle(int style)
	{
		curStyle=style;
	}
	
	int getCursorX()
	{
		return (x);
	}
	
	int getCursorY()
	{
		return (y);
	}
	
	boolean getInputPending()
	{
		return (false);
	}
	
	public void onDraw(Canvas g)
	{
		paintNeeded = true;
		super.onDraw(g);
		
		cleared=false;
	}
}
