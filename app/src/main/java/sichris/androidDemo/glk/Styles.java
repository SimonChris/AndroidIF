package sichris.androidDemo.glk;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

class Styles implements GLKConstants, Cloneable
{
	private int baseSize;
	private boolean proportionalAllowed;
	
	private int[] indentation;
	private int[] paraIndentation;
	private int[] justification;
	private int[] size;
	private int[] weight;
	private boolean[] oblique;
	private boolean[] proportional;
	private int[] textColor;
	private int[] backColor;
	private boolean[] reverseColor;
	
	private Paint[] font;
	private int[][] widths;
	private int[] height;

	Styles(int baseSize,boolean proportionalAllowed)
	{
		this.proportionalAllowed=proportionalAllowed;
		
		indentation=new int[11];
		paraIndentation=new int[11];
		justification=new int[11];
		size=new int[11];
		weight=new int[11];
		oblique=new boolean[11];
		proportional=new boolean[11];
		textColor=new int[11];
		backColor=new int[11];
		reverseColor=new boolean[11];
		
		for (int i=0;i<11;i++){
			
			indentation[i]=0;
			paraIndentation[i]=0;
			justification[i]=STYLEHINT_JUST_LEFTFLUSH;
			size[i]=0;
			weight[i]=0;
			oblique[i]=false;
			proportional[i]=proportionalAllowed;
			textColor[i]=Color.BLACK;
			backColor[i]=Color.WHITE;
			reverseColor[i]=false;}

		oblique[STYLE_EMPHASIZED]=true;
		proportional[STYLE_PREFORMATTED]=false;
		justification[STYLE_HEADER]=STYLEHINT_JUST_CENTERED;
		size[STYLE_HEADER]=5;
		weight[STYLE_HEADER]=1;
		weight[STYLE_SUBHEADER]=1;
		weight[STYLE_ALERT]=1;
		size[STYLE_NOTE]=-5;
		weight[STYLE_INPUT]=1;
		
		font=new Paint[11];
		widths=new int[11][];
		height=new int[11];
		mapFonts(baseSize);
	}
	
	void mapFonts(int newSize)
	{
		baseSize=newSize;
		for (int i=0;i<11;i++){
			font[i]=initFont(i);
			widths[i]=null;
			height[i]=0;}
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		return (super.clone());
	}

	Paint getFont(int style)
	{
		return (font[style]);
	}    

	private Paint initFont(int style)
	{
		int styleSetting=0;
		if (weight[style]>0)
			styleSetting|=Typeface.BOLD;
		if (oblique[style])
			styleSetting|=Typeface.ITALIC;

		Paint font=new Paint();
		font.setAntiAlias(true);
		
		if (proportional[style])
			font.setTypeface(Typeface.create(Typeface.SERIF, styleSetting));
		else
			font.setTypeface(Typeface.create(Typeface.MONOSPACE, styleSetting));
		font.setTextSize(baseSize+(size[style]*4));
		
		if (reverseColor[style])
			font.setColor(backColor[style]);
		else
			font.setColor(textColor[style]);

		return (font);
	}

	int[] getWidths(int style)
	{
		if (widths[style]==null){
			char[] testString=new char[256];
			for (int i=0;i<256;i++)
				testString[i]=(char)i;
			float[] widthFloat=new float[256];
			font[style].getTextWidths(testString, 0, 256, widthFloat);
			widths[style]=new int[256];
			for (int i=0;i<256;i++)
				widths[style][i]=(int)widthFloat[i];}

		return (widths[style]);
	}
	
	int getWidth(int ch,int style)
	{
		char[] buffer=new char[1];
		buffer[0]=(char)ch;
		return (int)(font[style].measureText(buffer, 0, 1));
	}
	
	int getHeight(int style)
	{
		if (height[style]==0)
			height[style]=(int)font[style].getFontSpacing();
			
		return (height[style]);
	}
	
	int getBackColor(int style)
	{
		if (reverseColor[style])
			return (textColor[style]);
		else
			return (backColor[style]);
	}

	int getTextColor(int style)
	{
		if (reverseColor[style])
			return (backColor[style]);
		else
			return (textColor[style]);
	}
	
	int getIndentation(int style)
	{
		return indentation[style];
	}

	void styleHintSet(int styl,int hint,int val)
	{
		switch (hint){
		case STYLEHINT_INDENTATION:
			indentation[styl]=val;
			return;
		case STYLEHINT_PARAINDENTATION:
			paraIndentation[styl]=val;
			return;
		case STYLEHINT_JUSTIFICATION:
			if (val>=0&&val<=3)
				justification[styl]=val;
			return;
		case STYLEHINT_SIZE:
			size[styl]=val;
			break;
		case STYLEHINT_WEIGHT:
			if (val>=-1&&val<=1)
				weight[styl]=val;
			break;
		case STYLEHINT_OBLIQUE:
			if (val==1)
				oblique[styl]=true;
			else
				if (val==0)
					oblique[styl]=false;
			break;
		case STYLEHINT_PROPORTIONAL:
			if (proportionalAllowed)
				if (val==1)
					proportional[styl]=true;
				else
					if (val==0)
						proportional[styl]=false;
			break;
		case STYLEHINT_TEXTCOLOR:
			textColor[styl]=val;
			break;
		case STYLEHINT_BACKCOLOR:
			backColor[styl]=val;
			break;
		case STYLEHINT_REVERSECOLOR:
			if (val==1)
				reverseColor[styl]=true;
			else
				if (val==0)
					reverseColor[styl]=false;
			break;
		default:
			return;}
		
		font[styl]=initFont(styl);
		
	}
	
	int getJustification(int style)
	{
		return (justification[style]);
	}
	
	int getWeight(int style)
	{
		return weight[style];
	}
	
	boolean getOblique(int style)
	{
		return oblique[style];
	}
	
	int getFontSize()
	{
		return (baseSize);
	}
}
