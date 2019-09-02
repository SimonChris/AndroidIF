package thedemitrius.androidif.glk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class PrintLine implements Serializable
{
	private static final long serialVersionUID = 3948221974567874139L;

	int height;
	int startPos;
	int paraStart;
	int type;
	int paraNum;
	int lineNum;
	int hyperlink;
	String text;
	int[] styleMap;
	int styleLimit;
	
	PrintLine(String text,int height,int startPos,int paraStart,int paraNum,int lineNum,int hyperlink,int[] styleMap,int limit,WindowStream stream)
	{
		this.text=text;
		this.height=height;
		this.startPos=startPos;
		this.paraStart=paraStart;
		type=0;
		this.paraNum=paraNum;
		this.lineNum=lineNum;
		this.hyperlink=hyperlink;
		this.styleMap=new int[limit];
		System.arraycopy(styleMap, 0, this.styleMap, 0, limit);
		styleLimit=limit;
		
		boolean adjust=false;
		if (text.length()>1&&text.charAt(0)==' '&&text.charAt(1)!=' '){
			text=text.substring(1);
			adjust=true;}

		for (int i=0;adjust&&i<limit;i+=3){
			if (styleMap[i+1]<styleMap[i+2]){
				styleMap[i+1]=Math.max(0, styleMap[i+1]-1);
				styleMap[i+2]=Math.max(1, styleMap[i+2]-1);}
		}
	}
	
	PrintLine(int height,int startPos,int type,int paraNum,int lineNum,int hyperlink)
	{
		this.height=height;
		this.startPos=startPos;
		this.type=type+100;
		this.paraNum=paraNum;
		this.lineNum=lineNum;
		this.hyperlink=hyperlink;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeInt(height);
		out.writeInt(startPos);
		out.writeInt(type);
		out.writeInt(paraNum);
		out.writeInt(lineNum);
		out.writeInt(hyperlink);
		out.writeInt(type);
		
		if (type==0){
			out.writeInt(paraStart);
			out.writeUTF(text);
			out.writeInt(styleLimit);
			for (int i=0;i<styleLimit;i++)
				out.writeInt(styleMap[i]);}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		height=in.readInt();
		startPos=in.readInt();
		type=in.readInt();
		paraNum=in.readInt();
		lineNum=in.readInt();
		hyperlink=in.readInt();
		type=in.readInt();
		
		if (type==0){
			paraStart=in.readInt();
			text=in.readUTF();
			styleLimit=in.readInt();
			styleMap=new int[styleLimit];
			for (int i=0;i<styleLimit;i++)
				styleMap[i]=in.readInt();}
	}
}
