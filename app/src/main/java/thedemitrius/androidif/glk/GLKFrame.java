package thedemitrius.androidif.glk;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class GLKFrame extends ViewGroup
{
	public GLKFrame(Context context) 
	{
		super(context);
		
		setFocusable(false);
	}

	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
	{
		int proposedWidth = MeasureSpec.getSize(widthMeasureSpec);
		int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);

		if (proposedHeight>getHeight()&&getChildCount()==2)
			removeViewAt(1);
		
		setMeasuredDimension(proposedWidth,proposedHeight);
		
		if (getChildCount()>0){
			int height=proposedHeight;
			if (getChildCount()==2){
				InputArea child2=(InputArea)getChildAt(1);
				child2.measure(widthMeasureSpec,View.MeasureSpec.makeMeasureSpec(child2.getIdealHeight(),View.MeasureSpec.EXACTLY));
				height-=child2.getIdealHeight();
            }
			View child1=getChildAt(0);
			child1.measure(widthMeasureSpec,View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));}
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) 
	{
		if (getChildCount()==1){
			getChildAt(0).layout(l, t, r, b);
			return;}

		if (getChildCount()==2){
			InputArea child2=(InputArea)getChildAt(1);
			getChildAt(0).layout(l, t, r, b-child2.getIdealHeight());
			child2.layout(l, b-child2.getIdealHeight(), r, b);}
	}
}
