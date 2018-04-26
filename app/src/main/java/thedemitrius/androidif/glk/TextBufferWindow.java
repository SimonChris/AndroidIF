package thedemitrius.androidif.glk;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import thedemitrius.androidif.Init;
import thedemitrius.androidif.util.DataTransforms;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

class TextBufferWindow extends TextWindow
{
	private InputArea inputArea;

	private ArrayList<Paragraph> paragraphs;
	private ArrayList<Paragraph> storedParagraphs;
	private Paragraph curParagraph;
	
	private PrintPage printPage;
	private PrintPage storedPrintPage;
	
	private boolean lineInputPending;

	private Bitmap image;

	private boolean bitbucket;
	
	private Activity context;
	private String homeDir;
	private String lastSaveFile;
	
	private ByteArrayOutputStream byteStream;
	private DataOutputStream textStream;
	
	private int textBoundary;

	private int saveCount;
		
	private TextBufferWindowGestureListener gestureDispatcher;
	private boolean listen;
	
	private boolean restoreKeyboard;
	
	@SuppressWarnings("unused")
	private PageFlipper pageFlipper;
	
	private Bitmap background;
	
	private int realStartParagraph;
	
	private boolean serializeActive;
	
	private boolean customRedraw;
	
	private static char arrows[]={0x21e6,0x21e8};
	
	TextBufferWindow(Activity context,int id,int method, int size, int rock, boolean key, WindowStream stream,String homeDir,
			GLK glk,InputArea inputArea) 
	{
		super(context,id, method, size, rock, key, stream);
		
		this.context=context;
		this.homeDir=homeDir;
		this.inputArea=inputArea;
		
		paragraphs=new ArrayList<Paragraph>();
		curParagraph=new Paragraph(curStyle);
		
		printPage=new PrintPage();
		
		storedParagraphs=null;
		
		lineInputPending=false;
		
		image=null;
		
		background=null;
		
		bitbucket=false;
		listen=false;
		
		lastSaveFile="";
		
		realStartParagraph=-1;
		
		serializeActive=true;
		
		customRedraw = false;
		
		resetTextStream();
		
		gestureDispatcher=new TextBufferWindowGestureListener(glk,this);
	}
	
	void putString(String s)
	{
		int pos=0;
		int nextLF;
		
		while ((nextLF=s.indexOf('\n',pos))!=-1){
			if (!bitbucket){
				curParagraph.text+=s.substring(pos,nextLF);
				paragraphs.add(curParagraph);
				curParagraph=new Paragraph(curStyle);}
			pos=nextLF+1;}

		if (pos<(s.length())){
			if (bitbucket)
				curParagraph.text=s.substring(pos);
			else
				curParagraph.text+=s.substring(pos);}
	}
	
	void putImage(int img)
	{
		if (curParagraph.text.length()>0)
			paragraphs.add(curParagraph);
		curParagraph=new Paragraph(img+100);
		paragraphs.add(curParagraph);
		curParagraph=new Paragraph(curStyle);
	}
	     
	private boolean textFollows(int num)
	{
		for (int i=num;i<paragraphs.size()-1;i++){
			Paragraph next=paragraphs.get(i);
			if (next.startStyle<100)
				for (int j=0;j<next.text.length();j++)
					if (next.text.charAt(j)=='\t')
						j++;
					else
						return (true);
			else if (next.startStyle<1000)
				return (true);
		}
						
		return (false);
	}
	
	void pageForward()
	{
		customRedraw = false;
		
		if (printPage.lastParaLine&&(printPage.endParaNum>=paragraphs.size()-1||!textFollows(printPage.endParaNum+1))){
			if (!helpUp())
				inputArea.display(true);
			return;}

		if (printPage.lastParaLine){
			printPage.startParaNum=printPage.endParaNum+1;
			printPage.startPos=0;}
		else{
			printPage.startParaNum=printPage.endParaNum;
			printPage.startPos=printPage.endPos;}

		Bitmap newPage=Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		makePageFromTop(newPage,false,false,false);
		pageFlipper=new PageFlipper(context,newPage,backStore,this,true);
	}  
	
	void makePageFromTop(Bitmap bitmap,boolean paint,boolean toBottom, boolean forceKeyboard)
	{
		try{
			
			if (bitmap==null)
				bitmap=backStore;
			
			if (paragraphs.size()==0  || getHeight() <= 0 || getWidth() <= 0)
				return;
			
			int startStyle=getLastStyle(paragraphs.get(printPage.startParaNum),printPage.startPos);
	
			int currentHyperlink=0;
			
			printPage.clear();
			
			int maxHeight;
			if (toBottom)
				maxHeight=1000000;
			else
				maxHeight=getHeight()-avgCharHeight*2;
			if (forceKeyboard && inputArea.isVisible())
				maxHeight += inputArea.getHeight();
			
			int endParaLoc=0;
			Paragraph printParagraph;
			
			for (int next=printPage.startParaNum;printPage.height<maxHeight&&next<paragraphs.size();next++){
				printParagraph= paragraphs.get(next);
				if (printParagraph.startStyle==1000)
					if (!pageHasText()&&!pageHasPicture())
						continue;
					else{
						break;}
				if (next==printPage.startParaNum){
					if (printParagraph.startStyle>=100)
						startStyle=printParagraph.startStyle;
					currentHyperlink=lineBreak(printParagraph.text.substring(printPage.startPos),startStyle,0,next,0);}
				else
					currentHyperlink=lineBreak(printParagraph.text,printParagraph.startStyle,printPage.size(),next,currentHyperlink);
				if (printParagraph.startStyle<100)
					endParaLoc=printParagraph.text.length();
				else
					endParaLoc=0;}

			maxHeight=getHeight()-avgCharHeight*2;
					
			if (forceKeyboard && inputArea.isVisible() && printPage.height > maxHeight) {
				inputArea.display(false);
				customRedraw = true;
				return;
			}

			printPage.lastParaLine=true;
			while (printPage.height>maxHeight){
				if (toBottom){
					printPage.removeFirst();
					continue;}
				PrintLine line=printPage.getLast();
				if (line.height>maxHeight&&!pageHasText())
					break;
				if (line.paraStart>0){
					printPage.lastParaLine=false;
					endParaLoc=line.paraStart;}
				else{
					printPage.lastParaLine=true;
					if (line.type>=100&&line.type<1000){
						if (printPage.getFirst().type>=1000){
							printPage.removeFirst();
							printPage.startParaNum=printPage.getFirst().paraNum;
							printPage.startPos=printPage.getFirst().paraStart;
							continue;}}}
				printPage.removeLast();}
			
			if (toBottom){
				printPage.startParaNum=printPage.getFirst().paraNum;
				printPage.startPos=printPage.getFirst().paraStart;}

			printPage.endParaNum=printPage.getLast().paraNum;
			printPage.endPos=endParaLoc;
				
			paintBackStore(bitmap);
			
			if (paint)
				postInvalidate();
		
		} catch (IndexOutOfBoundsException e){
			catchPageError(bitmap,paint);}
	}
	
	private int getLastStyle(Paragraph paragraph,int end)
	{
		if (paragraph.startStyle>=100)
			return (STYLE_NORMAL);
		
		if (paragraph.text==null)
			return paragraph.startStyle;
		
		String text=paragraph.text.substring(0,Math.min(end,paragraph.text.length()));
		
		if (text.length()<2)
			return (paragraph.startStyle);
		
		int lastIndex=text.lastIndexOf('\t');
		if (lastIndex==-1||lastIndex==text.length()-1)
			return (paragraph.startStyle);
		
		return (text.charAt(lastIndex+1)-16);
	}
	
	void pageBack() 
	{
		customRedraw = false;
		
		boolean keyboardDown = false;
		
		if (!gestureDispatcher.getKeyboardLocked()) {
			if (inputArea.isVisible()) 
				keyboardDown = true;
			inputArea.display(false);}
		
		if (printPage.startParaNum<=getRealStartParagraph()&&printPage.startPos==0){
			return;}

		if (printPage.startPos==0){
			printPage.endParaNum=printPage.startParaNum-1;
			printPage.endPos=paragraphs.get(Math.min(printPage.endParaNum,paragraphs.size()-1)).text.length();}
		else{
			printPage.endParaNum=printPage.startParaNum;
			printPage.endPos=printPage.startPos;}


		if (keyboardDown) {
			makePageFromBottom(null,true);}
		else {
			Bitmap newPage=Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
			makePageFromBottom(newPage,true);
			pageFlipper=new PageFlipper(context,newPage,backStore,this,false);}
	}
	
	private int getRealStartParagraph()
	{
		if (realStartParagraph==-1){
			for (int i=0;i<paragraphs.size();i++){
				Paragraph paragraph=paragraphs.get(i);
				if (paragraph.text.length()>0||(i > 0 && paragraph.startStyle>=100)){
					realStartParagraph=i;
					return (i);}
			}
		}

		return (realStartParagraph);
	}

	void makePageFromBottom(Bitmap bitmap,boolean paint)
	{
		try{
			
			if (bitmap==null)
				bitmap=backStore;
			
			if (paragraphs.size()==0 || getHeight() <= 0 || getWidth() <= 0)
				return;
			
			int currentHyperlink=0;

			printPage.clear();
			
			int maxHeight=getHeight()-avgCharHeight*2;
			
			for (int prev=Math.min(printPage.endParaNum,paragraphs.size()-1);prev>=0&&printPage.height<maxHeight;prev--){
				if (prev == 0) {
					printPage.startParaNum = 0;
					printPage.startPos = 0;
					makePageFromTop(bitmap, paint, false,false);
					return;
				}
				Paragraph printParagraph= paragraphs.get(prev);
				if (printParagraph.startStyle==1000){
					if (!pageHasText()&&!pageHasPicture()){
						printPage.lastParaLine=true;
						continue;}
					else
						break;}
				if (prev==printPage.endParaNum){
					currentHyperlink=lineBreak(printParagraph.text.substring(0,Math.min(printPage.endPos,printParagraph.text.length())),printParagraph.startStyle,0,prev,currentHyperlink);
					if (printParagraph.startStyle>=100)
						printPage.lastParaLine=true;
					else
						printPage.lastParaLine=(printPage.endPos>=printParagraph.text.length());}
				else
					currentHyperlink=lineBreak(printParagraph.text,printParagraph.startStyle,0,prev,currentHyperlink);
				}
	
			while (printPage.height>maxHeight){
				PrintLine line=printPage.getFirst();
				if (line.height>maxHeight&&!pageHasText())
					break;
				printPage.removeFirst();
			}
				
			printPage.startParaNum=printPage.getFirst().paraNum;
			printPage.startPos=printPage.getFirst().paraStart;
			 
			paintBackStore(bitmap);
			if (paint)
				postInvalidate();
		
		} catch (IndexOutOfBoundsException e){
			catchPageError(bitmap,paint);}
	}
	
	private void catchPageError(Bitmap bitmap,boolean paint)
	{
		printPage.endParaNum=paragraphs.size()-1;
		printPage.endPos=0;
		makePageFromBottom(bitmap,paint);
	}
	 
	void reprintPage()
	{
		makePageFromTop(backStore,true,false,false);
	}
	private int lineBreak(String paragraph,int style,int index,int paraNum,int hyperlink)
	{
		if (style>=100){
			PrintLine imageLine=prepareImage(style-100,paraNum);
			printPage.add(index,imageLine);
			return (hyperlink);}

		int lineHeight=stream.getHeight(style);
		int indent=avgCharWidth+(stream.getIndentation(style)*avgCharWidth);
		int totalHeight=0;
		String text="";
		
		boolean hyperlinkOff=false;

		if (paragraph.trim().length()>0){
			
			int[] charWidths=stream.getWidths(style);
			int lineWidth=getWidth()-avgCharWidth-indent;
			int pos=indent;
			int start=indent;
			int startChar=0;
			int lineStart=0;
			int lineCount=0;
			
			int[] styleMap=new int[60];
			int mapIndex=0;

			for (int i=0;i<paragraph.length();i++){
				
				if (paragraph.charAt(i)=='\t'){
					if (pos>indent){
						styleMap[mapIndex]=style;
						styleMap[mapIndex+1]=text.length();
						text+=paragraph.substring(startChar,i);
						styleMap[mapIndex+2]=text.length();
						mapIndex+=3;
						if ((i+2)==paragraph.length()){
							printPage.add(index,new PrintLine(text,lineHeight,indent,lineStart,paraNum,lineCount,hyperlink,
									styleMap,mapIndex,stream));
							if (hyperlinkOff){
								hyperlink=0;
								hyperlinkOff=false;}
							totalHeight+=lineHeight;}
					}
					else{
						lineHeight=0;}
					i++;
					if (paragraph.charAt(i)<100){
						style=paragraph.charAt(i)-16;
						charWidths=stream.getWidths(style);}
					else{
						if ((paragraph.charAt(i)>100)){
							hyperlinkOff=false;
							hyperlink=paragraph.charAt(i)-100;}
						else{
							hyperlinkOff=true;}
					}
					lineHeight=Math.max(lineHeight, stream.getHeight(style));
					startChar=i+1;
					continue;}

				if (paragraph.charAt(i)>255)
					pos+=stream.getWidth(paragraph.charAt(i), style);
				else
					pos+=charWidths[paragraph.charAt(i)];
				   
				if (pos>lineWidth){
					if (paragraph.substring(lineStart, i).indexOf(' ')!=-1){
						do{
							i--;}
						while (paragraph.charAt(i)!=' ');}
					if (i<startChar){
//						i=startChar-2;
						startChar = i;
						mapIndex = Math.max(0, mapIndex - 3);}
					else{
						styleMap[mapIndex]=style;
						styleMap[mapIndex+1]=text.length();
						text+=paragraph.substring(startChar,i);
						styleMap[mapIndex+2]=text.length();
						mapIndex+=3;
						i++;
						startChar=i;}
					if (stream.getJustification(style)==STYLEHINT_JUST_CENTERED)
						start=(int) (((getWidth()-stream.getFont(style).measureText(text)))/2);
					printPage.add(index,new PrintLine(text,lineHeight,start,lineStart,paraNum,lineCount,hyperlink,
						styleMap,mapIndex,stream));
					lineStart=i;
					if (hyperlinkOff){
						hyperlink=0;
						hyperlinkOff=false;}
					text="";
					mapIndex=0;
					index++;
					lineCount++;
					totalHeight+=lineHeight;
					pos=indent;
					start=indent;}
			}

			if (startChar<paragraph.length()){
				styleMap[mapIndex]=style;
				styleMap[mapIndex+1]=text.length();
				text+=paragraph.substring(startChar);
				styleMap[mapIndex+2]=text.length();
				mapIndex+=3;
				if (stream.getJustification(style)==STYLEHINT_JUST_CENTERED)
					start=(int) (((getWidth()-stream.getFont(style).measureText(text)))/2);
				printPage.add(index,new PrintLine(text,lineHeight,start,lineStart,paraNum,lineCount,hyperlink,
						styleMap,mapIndex,stream));
				if (hyperlinkOff){
					hyperlink=0;
					hyperlinkOff=false;}
				index++;
				lineCount++;
				totalHeight+=lineHeight;}
		}

		if (totalHeight==0){
			printPage.add(index,new PrintLine(lineHeight,indent,1000,paraNum,0,hyperlink));
			if (hyperlinkOff){
				hyperlink=0;
				hyperlinkOff=false;}
			index++;
			totalHeight=lineHeight;}

		return (hyperlink);
	}
	
	boolean pageHasText()
	{
		for (int i=0;i<printPage.size();i++)
			if (printPage.get(i).type==0)
				return (true);
		
		return (false);
	}
	
	boolean pageHasPicture()
	{
		for (int i=0;i<printPage.size();i++){
			PrintLine next=printPage.get(i);
			if (next.type>=100&&next.type<1000)
				return (true);}

		return (false);
	}
	
	PrintLine prepareImage(int num,int paraNum)
	{
		image=BitmapFactory.decodeStream(getClass().getResourceAsStream(Init.storyURL+num+".pic"));
		if (image==null)
			return (null);
		
		int width=image.getWidth();
		int height=image.getHeight();
		if (width>getWidth()){
			float scale=(float)getWidth()/(float)width;
			width*=scale;
			height*=scale;}
		if (height>getHeight()){
			float scale=(float)getHeight()/(float)height;
			width*=scale;
			height*=scale;}
		image=Bitmap.createScaledBitmap(image, width, height, false);
		
		int indent=(getWidth()-image.getWidth())/2;
		if (indent<0)
			indent=0;
		
		if (paraNum>=0)
			return (new PrintLine(height,indent,num,paraNum,0,0));
		else
			return (null);
	}
	
	public void paintBackStore(Bitmap bitmap)
	{
		if (bitmap==null){
			checkBackStore();
			bitmap=backStore;}
		Canvas g=new Canvas(bitmap);
		
		if (background!=null)
			g.drawBitmap(background, null, new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()),new Paint());
		else
			g.drawColor(stream.getBackColor(STYLE_NORMAL));
		
		gestureDispatcher.clearHyperlinks();
		
		if (printPage.size()>0){
		 
			int curLine=0;  
			PrintLine nextLine=null;
			boolean fullScreen=false;
			
			for (int i=0;i<printPage.size();i++){
				
				nextLine=printPage.get(i);
				
				if (nextLine.type>=100){
					if (nextLine.type<1000){
						if (image==null)
							prepareImage(nextLine.type-100,-1);
						if (nextLine.height>(getHeight()-avgCharHeight*2)){
							float scale=(float)getHeight()/(float)nextLine.height;
							float x=(getWidth()/2)-((image.getWidth()*scale)/2);
							g.drawBitmap(image,null,new RectF(x,0,x+(image.getWidth()*scale),(float)nextLine.height*scale),new Paint());
							fullScreen=true;}
						else
							g.drawBitmap(image,nextLine.startPos,curLine,new Paint());}
					curLine+=nextLine.height;}
				else{
					curLine+=nextLine.height;
					int pos=nextLine.startPos;
					for (int j=0;j<nextLine.styleLimit;j+=3){
						Paint font=stream.getFont(nextLine.styleMap[j]);
						g.drawText(nextLine.text, nextLine.styleMap[j+1], nextLine.styleMap[j+2], pos, curLine, font);
						pos+=font.measureText(nextLine.text,nextLine.styleMap[j+1],nextLine.styleMap[j+2]);}
				}
				if (gestureDispatcher.getHyperlinksActive()&&nextLine.hyperlink>0)
					gestureDispatcher.addHyperlink(nextLine.hyperlink, new Rect(0,curLine-nextLine.height,getWidth(),curLine));}

			textBoundary=curLine;
			
			if (!fullScreen){
			
				Paint font=stream.getFont(STYLE_HEADER);

				if (printPage.startParaNum>getRealStartParagraph()||printPage.startPos>0)
					g.drawText(arrows, 0, 1, avgCharWidth*4, getHeight()-5, font);
	
				if ((!inputArea.isVisible()&&!helpUp())||!(printPage.lastParaLine&&(printPage.endParaNum>=paragraphs.size()-1||!textFollows(printPage.endParaNum+1))))
					g.drawText(arrows, 1, 1, getWidth()-(avgCharWidth*6), getHeight()-5, font);}
		}

		paintNeeded=true;
			
			if (serializeActive)
				try {
					serialize();
				} catch (IOException e) {
				}
	}

	class Paragraph
	{
		int startStyle;
		String text;
		
		Paragraph(int startStyle)
		{
			this.startStyle=startStyle;
			text="";
		}
	}
	
	void clear(boolean helpActive)
	{
		resetTextStream();
		paragraphs.clear();
		
		printPage.fullClear();
	}
	
	void setStyle(int style)
	{
		curStyle=style;
		curParagraph.text+='\t';
		curParagraph.text+=(char)(style+16);
	}
	
	String getCommandPrompt()
	{
		return (curParagraph.text);
	}
	
	void updateStartParaNum()
	{
		gestureDispatcher.setKeyboardLocked(false);
		
		printPage.turnParaNum=printPage.startParaNum;
		printPage.turnPos=printPage.startPos;
		
		printPage.startPos=0;
		
		if (paragraphs.size()>0){
			printPage.startParaNum=paragraphs.size()-1;}
		else{
			printPage.startParaNum=0;}
	}
	
	void getSize(byte[] array,int widthPtr,int heightPtr)
	{
		if (widthPtr>=0)
			DataTransforms.putInt(array, widthPtr, ((getWidth()/avgCharWidth)-2));
		
		if (heightPtr>=0)
			DataTransforms.putInt(array, heightPtr, ((getHeight()/avgCharHeight)-2));
	}
	
	void setLineInputPending(boolean lineInputPending)
	{
		this.lineInputPending=lineInputPending;
	}
	
	void storeTextBuffer() throws IOException, InterruptedException
	{
		if (!helpUp()){
			storedParagraphs=paragraphs;
			paragraphs=new ArrayList<Paragraph>();
			storedPrintPage=printPage;
			printPage=new PrintPage();
			
			realStartParagraph=-1;
			
			restoreKeyboard=inputArea.isVisible();
			if (restoreKeyboard)
				post(new Runnable(){
					public void run(){
						inputArea.display(false);}});
	
			clear(true);}
	}
	
	void restoreTextBuffer() throws InterruptedException
	{
		if (helpUp()){
			
			paragraphs=storedParagraphs;
			storedParagraphs=null;
			printPage=storedPrintPage;
			storedPrintPage=null;
			printPage.startParaNum=printPage.turnParaNum;
			printPage.startPos=printPage.turnPos;
			
			realStartParagraph=-1;
        }
	}
	
	void saveTextBuffer(FileStream file) throws IOException
	{
		file.writeInt(printPage.startParaNum);
		file.writeInt(printPage.startPos);
		file.writeInt(printPage.endParaNum);
		file.writeInt(printPage.endPos);
		file.writeInt(printPage.height);
		file.writeBoolean(printPage.lastParaLine);

		int limit=paragraphs.size();
		file.writeInt(limit);
		
		for (;saveCount<limit;saveCount++){
			Paragraph next= paragraphs.get(saveCount);
			textStream.writeInt(next.startStyle);
			textStream.writeUTF(next.text);}

		file.putBuffer(byteStream.toByteArray(), 0, byteStream.size());
	}
	
	void loadTextBuffer(FileStream file) throws IOException
	{
		printPage.clear();
		paragraphs.clear();
		resetTextStream();
		
		printPage.startParaNum=file.readInt();
		printPage.startPos=file.readInt();
		printPage.endParaNum=file.readInt();
		printPage.endPos=file.readInt();
		printPage.height=file.readInt();
		printPage.lastParaLine=file.readBoolean();
		
		int limit=file.readInt();
		for (int i=0;i<limit;i++){
			Paragraph next=new Paragraph(file.readInt());
			next.text=file.readString();
			paragraphs.add(next);}
	}
	
	void formFeed()
	{
		if (curParagraph.text.length()>0){
			paragraphs.add(curParagraph);}
		else{
			while (paragraphs.size()>0){
				curParagraph= paragraphs.get(paragraphs.size()-1);
				if (curParagraph.text.length()>0){
					break;}
				else{
					resetTextStream();
					paragraphs.remove(paragraphs.size()-1);}
			}
		}

		curParagraph=new Paragraph(1000);
		paragraphs.add(curParagraph);
		curParagraph=new Paragraph(curStyle);
	}
	
	void setBitbucket(boolean bitbucket)
	{
		if (lineInputPending)
			this.bitbucket=bitbucket;
	}
	
	int getParagraphCount()
	{
		return (paragraphs.size());
	}
	
	void trimParagraphs(int size)
	{
		if (size<=0)
			return;
		
		if (paragraphs.size()>size)
			resetTextStream();
		
		while (paragraphs.size()>size)
			paragraphs.remove(paragraphs.size()-1);
		
		printPage.startParaNum=paragraphs.size()-1;
	}
	
	void jumpTo(int paraNum)
	{
		printPage.startParaNum=paraNum;
		printPage.startPos=0;
		makePageFromTop(backStore,true,false,false);
	}
	
	void setStartParaNum(int startParaNum)
	{
		printPage.startParaNum=startParaNum;
	}

	private void serialize() throws IOException
	{
		if (storedPrintPage==null&&lastSaveFile.length()>0){
			FileRef file=FileRef.createFileRef(context,homeDir,"state",0);
			if (file.exists())
				file.delete();
			ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(file));
			out.writeUTF(lastSaveFile);
			out.writeObject(printPage);
			out.close();}
	}
	
	void setLastSaveFile(String lastSaveFile)
	{
		this.lastSaveFile=lastSaveFile;
	}
	
	private void resetTextStream()
	{
		byteStream=new ByteArrayOutputStream();
		textStream=new DataOutputStream(byteStream);
		saveCount=0;
	}
	
	void setPage(PrintPage page,boolean revertToTurnParaNum)
	{
		printPage=page;
		
		if (revertToTurnParaNum){
			printPage.startParaNum=printPage.turnParaNum;
			printPage.startPos=printPage.turnPos;}

		post(new Runnable(){
			public void run(){
				makePageFromTop(null,true,false,false);
				invalidate();}});
	}
	
	int getTextBoundary()
	{
		return (textBoundary);
	}
	
	public Object getValue(String key) {
		return null;
	}

	public void putValue(String key, Object value) {}
	
	boolean helpUp()
	{
		return (storedParagraphs!=null);
	}
	
	void toggleHyperlinks(boolean active)
	{
		gestureDispatcher.toggleHyperlinks(active);
	}
	
	void setHyperlink(int linkVal)
	{
		if (gestureDispatcher.getHyperlinksActive()&&linkVal>=0){
			curParagraph.text+='\t';
			curParagraph.text+=(char)(linkVal+100);}
	}
	
	void setBackground(int num)
	{
		if (num==0)
			background=null;
		else
			background=BitmapFactory.decodeStream(getClass().getResourceAsStream(Init.storyURL+num+".pic"));
	}
	
	public boolean onTouchEvent(MotionEvent e)
	{
		if (!listen)
			return (true);
		
		return (gestureDispatcher.handleTouchEvent(e));
	}

	void gestureListen(boolean listen)
	{
		this.listen=listen;
	}
	
	public void onSizeChanged(int w,int h, int oldw, int oldh)
	{
		checkBackStore();
		
		if (customRedraw) {
			makePageFromTop(null, false, false, false);
			return;
		}
		
		if (helpUp())
			paintBackStore(backStore);
		else
			makePageFromBottom(backStore,false);
	}
	
	public void finalize()
	{
		gestureDispatcher.finalize();
	}
	
	void setSerializeActive(boolean serializeActive)
	{
		this.serializeActive=serializeActive;
	}
	
	void setCustomRedraw(boolean newRedraw) {
		customRedraw = newRedraw;
	}
}
