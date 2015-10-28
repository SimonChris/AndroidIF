package sichris.androidDemo.glk;

import java.io.IOException;

abstract class Stream implements GLKConstants
{
	private int id;
	private int rock;
	
	private Stream echoSource;
	
	protected int readCount,writeCount;

	Stream (int id,int rock)
	{
		this.id=id;
		this.rock=rock;
		
		echoSource=null;
		
		readCount=0;
		writeCount=0;
	}

	int getID()
	{
		return (id);
	}
	
	abstract void putString(byte[] s,int startPos) throws IOException;
	
	abstract void putChar(byte ch) throws IOException;
	
	int getReadCount()
	{
		return (readCount);
	}
	
	int getWriteCount()
	{
		return (writeCount);
	}
	
	abstract int getChar();
	
	Stream getEchoSource()
	{
		return (echoSource);
	}
	
	void setEchoSource(Stream echoSource)
	{
		this.echoSource=echoSource;
	}
	
	int getRock()
	{
		return (rock);
	}
	
	abstract void putBuffer(byte[] s,int startPos,int len) throws IOException;
	
	abstract int getPosition() throws IOException;
	
	abstract void setPosition(int offset,int seekMode) throws IOException;
	
	abstract int getBuffer(byte[] buf,int startPos,int len) throws IOException;
	
	abstract void putCharUni(int ch) throws IOException;
	
	void setHyperlink(int linkVal)
	{}
}
