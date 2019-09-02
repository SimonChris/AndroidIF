package thedemitrius.androidif.glk;

import java.io.Serializable;
import java.util.Vector;

class PrintPage implements Serializable
{
	private static final long serialVersionUID = -5579518361420395851L;

	int startParaNum;
	int startPos;
	int endParaNum;
	int endPos;
	int turnParaNum;
	int turnPos;
	int height;
	boolean lastParaLine;
	
	private Vector<PrintLine> lines;
	
	PrintPage()
	{
		lines=new Vector<PrintLine>();
		fullClear();
	}
	
	void fullClear()
	{
		startParaNum=0;
		startPos=0;
		endParaNum=0;
		endPos=0;
		turnParaNum=0;
		turnPos=0;
		height=0;
		lastParaLine=false;
		
		lines.clear();
	}
	
	void clear()
	{
		height=0;
		lines.clear();
	}
	
	int size()
	{
		return lines.size();
	}
	
	void add(PrintLine line)
	{
		height+=line.height;
		lines.add(line);
	}
	
	void add(int pos,PrintLine line)
	{
		height+=line.height;
		lines.add(pos,line);
	}
	 
	PrintLine get(int pos)
	{
		return lines.get(pos);
	}
	
	PrintLine getFirst()
	{
		return lines.get(0);
	}
	
	PrintLine getLast()
	{
		return lines.get(lines.size()-1);
	}
	
	void remove(int pos)
	{
		height-= lines.get(pos).height;
		lines.remove(pos);
	}
	
	void removeFirst()
	{
		if (lines.size()>0){
			height-= lines.get(0).height;
			lines.remove(0);}
	}
	
	void removeLast()
	{
		if (lines.size()>0){
			height-= lines.get(lines.size()-1).height;
			lines.remove(lines.size()-1);}
	}

	void appendData(Vector<?> newLines,int endParaNum,boolean lastParaLine)
	{
		for (int i=0;i<newLines.size();i++)
			add((PrintLine) newLines.get(i));
		this.endParaNum=endParaNum;
		this.lastParaLine=lastParaLine;
	}
	@SuppressWarnings("unchecked")
	Vector<PrintLine> cloneData()
	{
		return (Vector<PrintLine>) (lines.clone());
	}
}
