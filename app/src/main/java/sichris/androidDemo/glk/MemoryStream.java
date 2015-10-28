package sichris.androidDemo.glk;

import java.io.IOException;

import sichris.androidDemo.util.DataTransforms;

class MemoryStream extends Stream 
{
	private byte[] buf;
	private int bufStart;
	private int bufLen;
	private int mode;
	private boolean unicode;
	
	private int pos;
	
	MemoryStream(int id,byte[] buf,int bufStart,int bufLen,int mode,int rock,boolean unicode)
	{
		super(id,rock);
		
		this.buf=buf;
		this.bufStart=bufStart;
		this.bufLen=bufLen;
		this.mode=mode;
		this.unicode=unicode;
		
		pos=0;
	}

	int getChar() 
	{
		
		int result=-1;
		
		if ((mode&FILEMODE_READ)==FILEMODE_READ){
			if (pos<bufLen){
				if (unicode)
					result=buf[bufStart+pos*4+3];
				else
					result=buf[bufStart+pos];
				readCount++;
				pos++;}
		}

		return (result);
	}

	void putBuffer(byte[] s, int startPos, int len) throws IOException 
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){
			if ((pos+len)>bufLen)
				len=bufLen-pos;
			if (unicode){
				int start=bufStart+pos*4;
				for (int i=0;i<len;i++){
					buf[start+i*4]=0;
					buf[start+i*4+1]=0;
					buf[start+i*4+2]=0;
					buf[start+i*4+3]=s[startPos+i];}
			}
			else
				System.arraycopy(s, startPos, buf, bufStart+pos, len);
			writeCount+=len;
			pos+=len;}
	}

	void putChar(byte ch) throws IOException 
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){
			if (pos<bufLen){
				if (unicode){
					int start=bufStart+pos*4;
					buf[start]=0;
					buf[start+1]=0;
					buf[start+2]=0;
					buf[start+3]=ch;}
				else
					buf[bufStart+pos]=ch;
				writeCount++;
				pos++;}
		}
	}

	void putString(byte[] s, int startPos) throws IOException 
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){
			
			for (int i=startPos;s[i]!=0&&pos<bufLen;i++){
				if (unicode){
					int start=bufStart+pos*4;
					buf[start]=0;
					buf[start+1]=0;
					buf[start+2]=0;
					buf[start+3]=s[startPos+i];}
				else
					buf[bufStart+pos]=s[startPos+i];
				writeCount++;
				pos++;}
        }
	}
	
	int getPosition()
	{
		return (pos);
	}
	
	void setPosition(int offset,int seekMode)
	{
		int newPos;
		
		switch (seekMode){
		case SEEKMODE_START:
			newPos=offset;
			break;
		case SEEKMODE_CURRENT:
			newPos=pos+offset;
			break;
		case SEEKMODE_END:
			newPos=bufLen+offset;
			break;
		default:
			return;}

		if (newPos>=0&&newPos<bufLen)
			pos=bufStart+newPos;
	}
	
	int getBuffer(byte[] buf,int startPos, int len)
	{
		if ((mode&FILEMODE_READ)==FILEMODE_READ){

			if (len>(bufLen-pos))
				len=bufLen-pos;
			
			if (unicode){
				int start=bufStart+pos*4;
				for (int i=0;i<len;i++)
					buf[startPos+i]=this.buf[start+i*4+3];}
			else
				System.arraycopy(this.buf, pos, buf, startPos, len);
			pos+=len;
			readCount+=len;
			return (len);}
		
		else
			return (0);
	}
	
	void putCharUni(int ch) throws IOException 
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){
			if (pos<bufLen){
				if (unicode)
					DataTransforms.putInt(buf, bufStart+pos*4, ch);
				else
					buf[bufStart+pos]=(byte)ch;
				writeCount++;
				pos++;}
		}
	}
}
