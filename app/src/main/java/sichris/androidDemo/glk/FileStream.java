package sichris.androidDemo.glk;

import java.io.IOException;
import java.io.RandomAccessFile;

class FileStream extends Stream 
{
	
	private int mode;
	private boolean unicode;
	
	private RandomAccessFile stream;

	FileStream(int id,FileRef file,int mode,int rock,boolean unicode) throws IOException 
	{
		super(id,rock);
		
		this.mode=mode;
		this.unicode=unicode;

		if (mode==FILEMODE_READ){
			stream=new RandomAccessFile(file,"r");}
		else{
			stream=new RandomAccessFile(file,"rw");}

		if (mode==FILEMODE_WRITE){
			stream.setLength(0);}
		else{
			if (mode==FILEMODE_WRITEAPPEND){
				stream.seek(stream.length());}
		}
	}

	void putChar(byte ch) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){

			if (unicode)
				stream.writeInt(ch);
			else				
				stream.write(ch);

			writeCount++;}
	}

	void putString(byte[] s, int startPos) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){
			
			for (int i=startPos;s[i]!=0;i++){
				if (unicode)
					stream.writeInt(s[i]);
				else
					stream.write(s[i]);
				writeCount++;}
		}
	}
	
	void close() throws IOException
	{
		stream.close();
	}

	int getChar()
	{
		int result=-1;
		
		if ((mode&FILEMODE_READ)==FILEMODE_READ){
			
			try {
				if (unicode)
					result=(stream.readInt()&0xFF);
				else
					result=stream.read();
			} catch (IOException e) {
				return (-1);}}

		if (result>-1)
			readCount++;
		return (result);
	}
	
	void putBuffer(byte[] s,int startPos,int len) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){

			if (unicode)
				for (int i=0;i<len;i++){
					stream.write(0);
					stream.write(0);
					stream.write(0);
					stream.write(s[startPos+i]);}
			else
				stream.write(s, startPos, len);
			writeCount+=len;}
	}

	int getPosition() throws IOException
	{
		if (unicode)
			return (int) (stream.getFilePointer()/4);
		else
			return (int) (stream.getFilePointer());
	}
	
	void setPosition(int offset,int seekMode) throws IOException
	{
		if (unicode)
			offset*=4;
		int newPos;
		
		switch (seekMode){
		case SEEKMODE_START:
			newPos=offset;
			break;
		case SEEKMODE_CURRENT:
			newPos=(int) (stream.getFilePointer()+offset);
			break;
		case SEEKMODE_END:
			newPos=(int) (stream.length()+offset);
			break;
		default:
			return;}

		if (newPos>=0&&newPos<stream.length())
			stream.seek(newPos);
	}

	int getBuffer(byte[] buf,int startPos,int len) throws IOException
	{
		int result=0;
		
		if ((mode&FILEMODE_READ)==FILEMODE_READ){
		
			if (unicode)
				for (int i=0;i<len;i++){
					stream.skipBytes(3);
					buf[startPos+i]=stream.readByte();
					result++;}
			else
				result=stream.read(buf, startPos, len);
			readCount+=result;}

		return (result);
	}

	void writeInt(int val) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){

			stream.writeInt(val);
			writeCount+=4;}
	}
	
	int readInt() throws IOException
	{
		if ((mode&FILEMODE_READ)==FILEMODE_READ){
			
			readCount+=4;
			return (stream.readInt());}

		return (0);
	}
	
	void writeBoolean(boolean val) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){

			stream.writeBoolean(val);
			writeCount+=4;}
	}
	
	boolean readBoolean() throws IOException
	{
		if ((mode&FILEMODE_READ)==FILEMODE_READ){
			
			readCount+=4;
			return (stream.readBoolean());}

		return (false);
	}
	void writeString(String s) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){

			stream.writeUTF(s);
			writeCount+=s.length()+2;}
	}
	
	String readString() throws IOException
	{
		String result="";
		
		if ((mode&FILEMODE_READ)==FILEMODE_READ){
			
			result=stream.readUTF();
			readCount+=result.length()+2;}

		return (result);
	}

	void putCharUni(int ch) throws IOException
	{
		if ((mode&FILEMODE_WRITE)==FILEMODE_WRITE){

			if (unicode)
				stream.writeInt(ch);
			else				
				stream.write((byte)ch);
			
			writeCount++;}
	}

	RandomAccessFile getUnderlyingFile()
	{
		return (stream);
	}

}
