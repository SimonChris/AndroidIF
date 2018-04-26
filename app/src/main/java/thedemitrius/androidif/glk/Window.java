package thedemitrius.androidif.glk;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import thedemitrius.androidif.util.DataTransforms;

abstract class Window extends ViewGroup implements GLKConstants
{
	protected int method;
	protected int size;
	private int rock;
	protected boolean key;

	Window (Context context,int id,int method,int size,int rock,boolean key)
	{
		super(context);
		
		setId(id);
		this.method=method;
		this.size=size;
		this.rock=rock;
		this.key=key;
		
		setFocusable(false);
		setWillNotDraw(false);
	}

	int getMethod()
	{
		return (method);
	}
	
	int getSizeSetting()
	{
		return (size);
	}
	
	boolean keyWin()
	{
		return (key);
	}
	
	void setKey(boolean key)
	{
		this.key=key;
	}
	
	abstract void clear(boolean helpActive);
	
	void getSize(byte[] array1,int widthPtr,byte[] array2,int heightPtr)
	{
		if (widthPtr>=0)
			DataTransforms.putInt(array1, widthPtr, getWidth());
		
		if (heightPtr>=0)
			DataTransforms.putInt(array2, heightPtr, getHeight());
	}
	
	int getRock()
	{
		return (rock);
	}
	
	void setMethod(int method)
	{
		this.method=method;
	}
	
	void setSize(int size)
	{
		this.size=size;
	}
	
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
	}
	
	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
	{
		setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec),View.MeasureSpec.getSize(heightMeasureSpec));
	}
}
