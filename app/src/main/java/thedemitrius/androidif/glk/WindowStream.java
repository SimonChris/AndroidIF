package thedemitrius.androidif.glk;

import java.io.IOException;

import android.graphics.Paint;

class WindowStream extends Stream
{
	private TextWindow window;
	
	private Styles styles;

	private Stream echo;
	
	WindowStream(int id, Styles styles) throws CloneNotSupportedException
	{
		super(id,0);
		
		window=null;
		echo=null;
		
		this.styles=(Styles) styles.clone();
	}

	void setWindow(TextWindow window)
	{
		this.window=window;
	}

	Paint getFont(int style)
	{
		return (styles.getFont(style));
	}
	
	void putString(byte[] s,int startPos) throws IOException
	{
		int len=0;
		while (s[startPos+len]!=0){
			len++;}

		putBuffer(s, startPos, len);
	}
	
	void putChar(byte ch) throws IOException
	{	
		window.putString(""+(char)ch);
		
		writeCount++;
			
		if (echo!=null){
			echo.putChar(ch);}
	}
	
	int[] getWidths(int style)
	{
		return (styles.getWidths(style));
	}
	
	int getWidth(int ch,int style)
	{
		return (styles.getWidth(ch, style));
	}
	
	int getHeight(int style)
	{
		return (styles.getHeight(style));
	}

	int getBackColor(int style)
	{
		return (styles.getBackColor(style));
	}
	
	int getTextColor(int style)
	{
		return (styles.getTextColor(style));
	}
	
	void setStyle(int style)
	{
		window.setStyle(style);
	}
	
	int getStyle()
	{
		return (window.curStyle);
	}
	
	int getIndentation(int style)
	{
		return (styles.getIndentation(style));
	}
	
	int getChar()
	{
		return (-1);
	}
	
	void setEchoStream(Stream echo)
	{
		this.echo=echo;
		
		if (echo!=null)
			echo.setEchoSource(this);
	}
	
	Stream getEchoStream()
	{
		return (echo);
	}
	
	boolean echoing()
	{
		return (echo!=null);
	}

	void putBuffer(byte[] s, int startPos, int len) throws IOException 
	{
		window.putString(new String(s,startPos,len));
		
		writeCount+=(len);
			
		if (echo!=null){
			echo.putBuffer(s, startPos, len);}
	}
	
	int getPosition()
	{
		return (writeCount);
	}
	
	void setPosition(int offset,int seekMode)
	{
	}
	
	int getBuffer(byte[] buf, int startPos, int len)
	{
		return (0);
	}

	void putCharUni(int ch) throws IOException
	{	
		window.putString(""+(char)ch);
		
		writeCount++;
			
		if (echo!=null){
			echo.putCharUni(ch);}
	}
	
	int getJustification(int style)
	{
		return (styles.getJustification(style));
	}
	
	int getWeight(int style)
	{
		return styles.getWeight(style);
	}
	
	boolean getOblique(int style)
	{
		return styles.getOblique(style);
	}
	
	int getFontSize()
	{
		return (styles.getFontSize());
	}
	
	void setHyperlink(int linkVal)
	{
		window.setHyperlink(linkVal);
	}
}
