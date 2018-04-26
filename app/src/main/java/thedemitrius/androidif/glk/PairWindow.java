package thedemitrius.androidif.glk;

import android.view.View;
import android.content.Context;

class PairWindow extends Window
{
	PairWindow(Context context,int id, int method, int size, int rock, boolean key)
	{
		super(context,id, method, size, rock, key);
		
		setWillNotDraw(true);
	}
	
	void clear(boolean helpActive)
	{
	}
	
	void setArrangement(int method,int size,int keyWin)
	{
		final Window child=(Window)getChildAt(keyWin);
		
		if (!child.keyWin()){
			child.setKey(true);
			if (keyWin==0)
				((Window)getChildAt(1)).setKey(false);
			else
				((Window)getChildAt(0)).setKey(false);
		}

		child.setMethod(method);
		child.setSize(size);

		requestLayout();
	}
	
	public void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (getChildCount()==2){
			Window key,nonkey;
			if (((Window)getChildAt(0)).keyWin()){
				key=(Window)getChildAt(0);
				nonkey=(Window)getChildAt(1);}
			else{
				key=(Window)getChildAt(1);
				nonkey=(Window)getChildAt(0);}
			int totalWidth=View.MeasureSpec.getSize(widthMeasureSpec);
			int totalHeight=View.MeasureSpec.getSize(heightMeasureSpec);
			int keyWidth,keyHeight;
			int nonkeyWidth,nonkeyHeight;
			if ((key.getMethod()&2)==2){
				if ((key.getMethod()&WINMETHOD_FIXED)==WINMETHOD_FIXED){
					keyHeight=key.getSizeSetting();}
				else{
					keyHeight=(totalHeight/100)*key.getSizeSetting();}
				keyWidth=totalWidth;
				nonkeyHeight=totalHeight-keyHeight;
				nonkeyWidth=totalWidth;}
			else{
				if ((key.getMethod()&WINMETHOD_FIXED)==WINMETHOD_FIXED){
					keyWidth=key.getSizeSetting();}
				else{
					keyWidth=(totalWidth/100)*key.getSizeSetting();}
				keyHeight=totalHeight;
				nonkeyWidth=totalWidth-keyWidth;
				nonkeyHeight=totalHeight;}
			key.measure(View.MeasureSpec.makeMeasureSpec(keyWidth, View.MeasureSpec.EXACTLY), 
					View.MeasureSpec.makeMeasureSpec(keyHeight,View.MeasureSpec.EXACTLY));
			nonkey.measure(View.MeasureSpec.makeMeasureSpec(nonkeyWidth, View.MeasureSpec.EXACTLY), 
					View.MeasureSpec.makeMeasureSpec(nonkeyHeight,View.MeasureSpec.EXACTLY));}
	}
	
	public void onLayout(boolean changed, int l, int t, int r, int b)
	{
		if (getChildCount()==2){
			Window key,nonkey;
			if (((Window)getChildAt(0)).keyWin()){
				key=(Window)getChildAt(0);
				nonkey=(Window)getChildAt(1);}
			else{
				key=(Window)getChildAt(1);
				nonkey=(Window)getChildAt(0);}
			int keyL=0; int keyT=0; int keyR=0; int keyB=0;
			int nonkeyL=0; int nonkeyT=0; int nonkeyR=0; int nonkeyB=0;
			switch (key.getMethod()&3){
			case WINMETHOD_LEFT:
				keyL=0; keyT=0; keyR=key.getMeasuredWidth(); keyB=b;
				nonkeyL=key.getMeasuredWidth(); nonkeyT=0; nonkeyR=r; nonkeyB=b;
				break;
			case WINMETHOD_RIGHT:
				keyL=nonkey.getMeasuredWidth(); keyT=0; keyR=r; keyB=b;
				nonkeyL=0; nonkeyT=0; nonkeyR=nonkey.getMeasuredWidth(); nonkeyB=b;
				break;
			case WINMETHOD_ABOVE:
				keyL=0; keyT=0; keyR=r; keyB=key.getMeasuredHeight();
				nonkeyL=0; nonkeyT=key.getMeasuredHeight(); nonkeyR=r; nonkeyB=b;
				break;
			case WINMETHOD_BELOW:
				keyL=0; keyT=nonkey.getMeasuredHeight(); keyR=r; keyB=b;
				nonkeyL=0; nonkeyT=0; nonkeyR=r; nonkeyB=nonkey.getMeasuredHeight();
				break;}
			key.layout(keyL, keyT, keyR, keyB);
			nonkey.layout(nonkeyL, nonkeyT, nonkeyR, nonkeyB);}
	}
}
