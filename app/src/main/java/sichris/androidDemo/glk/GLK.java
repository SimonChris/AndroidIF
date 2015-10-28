package sichris.androidDemo.glk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.TreeMap;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import sichris.androidDemo.util.DataTransforms;

public class GLK implements GLKConstants
{
	private boolean active;
	private boolean undosAvail;
	
	private Activity context;
	private GLKFrame frame;
	private InputArea inputArea;
	 
	private String homeDir;
	
	private TreeMap<Integer, Window> windows;
	private int nextWinID;
	
	private TreeMap<Integer, Stream> streams;
	private int nextStreamID;
	
	private TreeMap<Integer, FileRef> files;
	private int nextFileID;
	
	private ArrayList<TextJump> textJumps;
	
	private Stream activeStream;
	
	private Styles textBufferStyles;
	private Styles textGridStyles;
	
	private byte[] eventArray;
	private int eventPos;
	
	private boolean dialogFinished;
	private String dialogResult;
	
	private TextBufferWindow mainWin;
	
	private int paragraphCount;
	
	private String nextCommand;
	
	private boolean selected;
	
	private boolean helpActive;
	
	private FrameLayout storyPanel;
	
	private PrintPage startPage;
	
	private boolean pageToBottom;
	 
	public GLK(final Activity context,final GLKFrame frame,String homeDir) throws IOException
	{
		active=true;
		undosAvail=false;
		 
		this.context=context;
		this.frame=frame;
		this.homeDir=homeDir;
		
		textBufferStyles=new Styles(getFontSize(),true);
		textGridStyles=new Styles(onPhone() ? scaleFont(18) : scaleFont(21) ,false);

		storyPanel=new FrameLayout(context);
		storyPanel.setFocusable(false);

		if (!active)
			return;
		
		final GLK glk=this;
		context.runOnUiThread(new Runnable(){
			public void run(){
				inputArea=new InputArea(context,glk);
				
				frame.addView(storyPanel);
				resumeFromWindowAdd();}});
		
		synchronized(this){
			while (frame.getChildCount()==0)
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}}

		if (!active)
			return;

		windows=new TreeMap<Integer, Window>();
		nextWinID=1;

		streams=new TreeMap<Integer, Stream>();
		nextStreamID=1;
		
		files=new TreeMap<Integer, FileRef>();
		files.put(new Integer(1), FileRef.createFileRef(context,homeDir,"state",5));
		nextFileID=2;
		
		activeStream=null;
		 
		if (!active)
			return;
		
		eventArray=null;
		eventPos=0;
		
		dialogFinished=false;
		dialogResult=null;
		
		mainWin=null;
		
		nextCommand="";
		
		selected=false;
		helpActive=false;
		pageToBottom=false;

		startPage=null;
		
		textJumps=new ArrayList<TextJump>();
	}
	
	public int gestalt(int sel,int val)
	{
		switch (sel){
		case GESTALT_VERSION:
			return (0x701);
		case GESTALT_CHARINPUT:
			return (1);
		case GESTALT_LINEINPUT:
			return (1);
		case GESTALT_CHAROUTPUT:
			if (val<32||val==127)
				return (GESTALT_CHAROUTPUT_CANNOTPRINT);
			if (val<176)
				return (GESTALT_CHAROUTPUT_EXACTPRINT);
			return (GESTALT_CHAROUTPUT_APPROXPRINT);
		case GESTALT_TIMER:
			return (1);
		case GESTALT_GRAPHICS:
			return (1);
		case GESTALT_DRAWIMAGE:
			if (val==WINTYPE_GRAPHICS||val==WINTYPE_TEXTBUFFER)
				return (1);
			else
				return (0);
		case GESTALT_HYPERLINKS:
			return (1);
		case GESTALT_HYPERLINKINPUT:
			if (val==WINTYPE_TEXTBUFFER)
				return (1);
			else
				return (0);
		case GESTALT_GRAPHICSTRANSPARENCY:
			return (1);
		case GESTALT_UNICODE:
			return (1);
		default:
			return (0);}
	}
	
	public int windowIterate(int win,byte[] array,int rockPtr)
	{
		for (int i=win+1;i<nextWinID;i++){
			Window winObj= windows.get(new Integer(i));
			if (winObj!=null){
				if (rockPtr>=0){
					DataTransforms.putInt(array, rockPtr, winObj.getRock());}
				return (i);}
		}

		return (0);
	}
	
	public int streamIterate(int str,byte[] array,int rockPtr)
	{
		for (int i=str+1;i<nextStreamID;i++){
			Stream strObj= streams.get(new Integer(i));
			if (strObj!=null){
				if (rockPtr>=0){
					DataTransforms.putInt(array, rockPtr, strObj.getRock());}
				return (i);}
		}

		return (0);
	}
	
	public int fileRefIterate(int file,byte[] array,int rockPtr)
	{
		for (int i=file+1;i<nextFileID;i++){
			FileRef fileObj= files.get(new Integer(i));
			if (fileObj!=null){
				if (rockPtr>=0){
					DataTransforms.putInt(array, rockPtr, fileObj.getRock());}
				return (i);}
		}

		return (0);
	}
	
	public int windowOpen(int split,final int method,int size,int wintype,int rock) throws CloneNotSupportedException, InvocationTargetException
	{
		if (windows.isEmpty()){

			if (split!=0)
				return (0);
		
			final Window newWin=windowCreate(0,0,wintype,rock,false);
			if (newWin==null)
				return (0);
			
			context.runOnUiThread(new Runnable(){
				public void run(){
					context.setContentView(frame);
					storyPanel.addView(newWin);
					resumeFromWindowAdd();}});
			
			synchronized(this){
				while (storyPanel.getChildCount()==0)
					try {
						wait();
					} catch (InterruptedException e) {
						return (0);
					}}

			if (!active)
				return (0);}
			
		else{
			
			if (split==0||method<WINMETHOD_FIXED)
				return (0);
			
			if (wintype==WINTYPE_TEXTBUFFER&&mainWin!=null)
				return (0);
			
			final Window oldWin= windows.get(new Integer(split));
			
			if (oldWin==null)
				return (0);
			
			final ViewGroup parent=(ViewGroup)oldWin.getParent();
			
			context.runOnUiThread(new Runnable(){
				public void run(){
					parent.removeView(oldWin);
					synchronized(context){
						context.notifyAll();}}});
			
			synchronized(context){
				while (oldWin.getParent()!=null)
					try {
						context.wait();
					} catch (InterruptedException e) {
						return (0);
					}}

			final int pairMethod=oldWin.getMethod();
			final Window pairWin=windowCreate(pairMethod,oldWin.getSizeSetting(),WINTYPE_PAIR,0,oldWin.keyWin());
			oldWin.setKey(false);
			
			context.runOnUiThread(new Runnable(){
				public void run(){
					RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					if (pairMethod==0){
						parent.addView(pairWin,lp);}
					else{
						switch (pairMethod&3){
						case WINMETHOD_LEFT:
							lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
							parent.addView(pairWin,lp);
							break;
						case WINMETHOD_RIGHT:
							lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
							parent.addView(pairWin,lp);
							break;
						case WINMETHOD_ABOVE:
							lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
							parent.addView(pairWin,lp);
							break;
						case WINMETHOD_BELOW:
							lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
							parent.addView(pairWin,lp);
							break;}
					}
					synchronized(context){
						context.notifyAll();}}});

			synchronized(context){
				while (pairWin.getParent()==null)
					try {
						context.wait();
					} catch (InterruptedException e) {
						return (0);
					}}

			final Window newWin=windowCreate(method,size,wintype,rock,true);

			context.runOnUiThread(new Runnable(){
				public void run(){
					switch (method&3){
					case WINMETHOD_LEFT:
						pairWin.addView(oldWin);
						pairWin.addView(newWin);
						break;
					case WINMETHOD_RIGHT:
						pairWin.addView(oldWin);
						pairWin.addView(newWin);
						break;
					case WINMETHOD_ABOVE:
						pairWin.addView(oldWin);
						pairWin.addView(newWin);
						break;
					case WINMETHOD_BELOW:
						pairWin.addView(oldWin,RelativeLayout.ALIGN_PARENT_TOP);
						pairWin.addView(newWin,RelativeLayout.ALIGN_PARENT_BOTTOM);}
					synchronized(context){
						context.notifyAll();}}});

			synchronized(context){
				while (pairWin.getChildCount()<2)
					try {
						context.wait();
					} catch (InterruptedException e) {
						return (0);
					}}

			if (!active)
				return (0);}

		return (nextWinID-1);
	}
		
	private Window windowCreate(int method,int size,int wintype,int rock,boolean key) throws CloneNotSupportedException
	{
		Window newWin=null;
		WindowStream newStream;
		
		switch (wintype){
		case WINTYPE_PAIR:
			newWin=new PairWindow(context,nextWinID,method,size,rock,key);
			break;
		case WINTYPE_TEXTBUFFER:
			newStream=new WindowStream(nextStreamID,textBufferStyles); 
			streams.put(new Integer(nextStreamID), newStream);
			nextStreamID++;
			newWin=new TextBufferWindow(context,nextWinID,method,size,rock,key,newStream,homeDir,this,inputArea);
			mainWin=(TextBufferWindow)newWin;
			context.registerForContextMenu(mainWin);
			break;
		case WINTYPE_TEXTGRID:
			newStream=new WindowStream(nextStreamID,textGridStyles);
			streams.put(new Integer(nextStreamID), newStream);
			nextStreamID++;
			newWin=new TextGridWindow(context,nextWinID,method,size,rock,key,newStream);
			break;}

		windows.put(new Integer(nextWinID), newWin);
		nextWinID++;
		return (newWin);
	}
	
	public void setWindow(int win)
	{
		Window winObj= windows.get(new Integer(win));
		if ((winObj!=null)&&(winObj instanceof TextWindow))
			activeStream=((TextWindow)winObj).getStream();
		else
			activeStream=null;
	}
	
	public void windowSetArrangement(int win,final int method,final int size,int keyWin) throws InterruptedException, InvocationTargetException
	{
		final Window winObj= windows.get(new Integer(win));
		if (winObj==null||!(winObj instanceof PairWindow))
			return;
		
		if (winObj.getMethod()==method)
			return;
		
		final int keyChild;

		if (keyWin==0){
			if (((Window)winObj.getChildAt(1)).keyWin())
				keyChild=1;
			else
				keyChild=0;}
		else{
			if (winObj.getChildAt(0).getId()==keyWin)
				keyChild=0;
			else
				if (winObj.getChildAt(1).getId()==keyWin)
					keyChild=1;
				else
					return;}


		context.runOnUiThread(new Runnable(){
			public void run(){
				((PairWindow)winObj).setArrangement(method,size,keyChild);}});
	}

	public int windowGetParent(int win)
	{
		Window winObj= windows.get(new Integer(win));
		if (winObj!=null){
			ViewParent parent=winObj.getParent();
			if (parent instanceof Window){
				return (((Window) parent).getId());}
		}

		return (0);
	}
	
	public void windowGetSize(int win,byte[] array1,int widthPtr,byte[] array2,int heightPtr)
	{
		Window winObj= windows.get(new Integer(win));
		if (winObj!=null)
			winObj.getSize(array1,widthPtr,array2,heightPtr);
	}
	
	public void windowClear(int win){
		Window winObj= windows.get(new Integer(win));
		if (winObj!=null)
			winObj.clear(helpActive);
	}
	
	public void windowMoveCursor(int win,int x,int y)
	{
		Window winObj= windows.get(new Integer(win));
		if ((winObj!=null)&&(winObj instanceof TextGridWindow))
			((TextGridWindow) winObj).moveCursor(x,y);
	}	
	
	public void requestLineEvent(int win,byte[] buf,int bufStart,int maxLen,int initLen)
	{
		Window winObj= windows.get(new Integer(win));
		if ((winObj!=null)&&(winObj instanceof TextBufferWindow)){
			if (inputArea.getReady())
				inputArea.deactivate();
			inputArea.setup((TextBufferWindow)winObj, buf, bufStart, maxLen, initLen);}
	}
	
	public void requestHyperlinkEvent(int win)
	{
		Window winObj= windows.get(new Integer(win));
		if ((winObj==null)||!(winObj instanceof TextBufferWindow))
			return;
		
		((TextBufferWindow)winObj).toggleHyperlinks(true);
	}
	
	public void cancelHyperlinkEvent(int win)
	{
		Window winObj= windows.get(new Integer(win));
		if ((winObj==null)||!(winObj instanceof TextBufferWindow))
			return;

		((TextBufferWindow)winObj).toggleHyperlinks(false);
	}
	
	public void setHyperlink(int linkVal)
	{
		if (activeStream!=null){
			activeStream.setHyperlink(linkVal);}
	}
	
	public int streamOpenMemory(byte[] buf,int bufStart,int bufLen,int mode,int rock)
	{
		streams.put(new Integer(nextStreamID), new MemoryStream(nextStreamID,buf,bufStart,bufLen,mode,rock,false));
		nextStreamID++;
		return (nextStreamID-1);
	}
	
	public int streamOpenMemoryUni(byte[] buf,int bufStart,int bufLen,int mode,int rock)
	{
		streams.put(new Integer(nextStreamID), new MemoryStream(nextStreamID,buf,bufStart,bufLen,mode,rock,true));
		nextStreamID++;
		return (nextStreamID-1);
	}
	
	public int streamOpenFile(int file,int mode,int rock) throws IOException
	{
		FileRef fileObj= files.get(new Integer(file));
		if (fileObj==null)
			return (0);
		
		streams.put(new Integer(nextStreamID), new FileStream(nextStreamID,fileObj,mode,rock,false));
		nextStreamID++;
		
		if (fileObj.getRock()==500)
			mainWin.setLastSaveFile(fileObj.getName());
		
		return (nextStreamID-1);
	}
	
	public int streamGetCurrent()
	{
		if (activeStream!=null)
			return (activeStream.getID());
		
		return (0);
	}
	
	public void streamSetCurrent(int str)
	{
		Stream strObj= streams.get(new Integer(str));
		activeStream=strObj;
	}
	
	public void streamClose(int str,byte[] result,int ptr) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null)
			return;
		
		if (ptr>-1){
			DataTransforms.putInt(result, ptr, strObj.getReadCount());
			DataTransforms.putInt(result, ptr+4, strObj.getWriteCount());}

		if (strObj.getEchoSource()!=null){
			((WindowStream) strObj.getEchoSource()).setEchoStream(null);}

		if (activeStream==strObj)
			activeStream=null;
		
		if (strObj instanceof FileStream)
			((FileStream)strObj).close();
		
		streams.remove(new Integer(str));
	}
	
	public byte charToLower(byte ch)
	{
		return ((byte)Character.toLowerCase((char)ch));
	}
	
	public byte charToUpper(byte ch)
	{
		return ((byte)Character.toUpperCase((char)ch));
	}

	public void setStyle(int style)
	{
		if (activeStream instanceof WindowStream)
			((WindowStream)activeStream).setStyle(style);
	}
	
	public void styleHintSet(int winType,int styl,int hint,int val)
	{
		if (winType==WINTYPE_TEXTBUFFER||winType==WINTYPE_ALLTYPES){
			textBufferStyles.styleHintSet(styl,hint,val);}

		if (winType==WINTYPE_TEXTGRID||winType==WINTYPE_ALLTYPES){
			textGridStyles.styleHintSet(styl,hint,val);}
	}
	
	public void putChar(byte ch) throws IOException
	{
		if (activeStream!=null){
			activeStream.putChar(ch);}
	}
	
	public void putCharUni(int ch) throws IOException
	{
		if (activeStream!=null){
			activeStream.putCharUni(ch);}
	}
	
	public void putString(byte[] s,int startPos) throws IOException
	{
		if (activeStream!=null){
			activeStream.putString(s, startPos);}
	}
	
	public void putBuffer(byte[] s,int startPos,int len) throws IOException
	{
		if (activeStream!=null)
			activeStream.putBuffer(s, startPos, len);
	}
	
	public void putBufferStream(int str,byte[] s,int startPos,int len) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null)
			return;
		
		strObj.putBuffer(s, startPos, len);
	}
	
	public int getBufferStream(int str,byte[] buf,int startPos,int len) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null)
			return (0);
		
		return (strObj.getBuffer(buf, startPos, len));
	}
	
	public int fileRefCreateByPrompt(int usage,int mode,int rock) throws InterruptedException, InvocationTargetException, IOException, ClassNotFoundException
	{
		mainWin.setSerializeActive(false);
		
		final Intent dialog=new Intent(context,FileDialog.class);
		
		dialog.putExtra("dir", homeDir);
		
		switch (usage&255){
		case FILEUSAGE_DATA:
			dialog.putExtra("ext", ".dat");
			if (mode==FILEMODE_READ){
				dialog.putExtra("initialMessage", "Choose a data slot to read in from the list below, or tap here to cancel.");}
			else{
				dialog.putExtra("initialMessage", "Choose a data slot to write out from the list below, or tap here to cancel.");
				dialog.putExtra("confirmMessage", "Data Slot Name:");}
			break;
		case FILEUSAGE_SAVEGAME:
			dialog.putExtra("ext", ".sav");
			if (mode==FILEMODE_READ){
				dialog.putExtra("initialMessage", "Choose a bookmark to restore from the list below, or tap here to cancel.");}
			else{
				dialog.putExtra("initialMessage", "Choose a bookmark to store your progress from the list below, or tap here to cancel.");
				dialog.putExtra("confirmMessage", "Bookmark Name:");}
			break;
		case FILEUSAGE_TRANSCRIPT:
			dialog.putExtra("ext", ".txt");
			if (mode==FILEMODE_READ){
				return (0);}
			else{
				dialog.putExtra("initialMessage", "Choose a slot to store your transcript from the list below, or tap here to cancel.");
				dialog.putExtra("confirmMessage", "Transcript Name:");}
			break;
		case FILEUSAGE_INPUTRECORD:
			dialog.putExtra("ext", ".rec");
			if (mode==FILEMODE_READ){
				dialog.putExtra("initialMessage", "Choose a recording to read from the list below, or tap here to cancel.");}
			else{
				dialog.putExtra("initialMessage", "Choose a slot from the list below to record your command, or tap here to cancel.");
				dialog.putExtra("confirmMessage", "Recording Name:");}
			break;
		default:
			mainWin.setSerializeActive(true);
			return (0);}

		context.startActivityForResult(dialog,1);
		
		synchronized (this){
			while (!dialogFinished)
				wait();}

		if (dialogResult==null){
			mainWin.setSerializeActive(true);
			return (0);}

		if ((usage&FILEUSAGE_SAVEGAME)==FILEUSAGE_SAVEGAME){
			copySaveDir(dialogResult, files.values().toArray(new FileRef[0]),mode);
			if (mode==FILEMODE_READ)
				dialogFinished=false;}

		mainWin.setSerializeActive(true);
		return (1);
	}

	public int fileRefCreateByName(int usage,byte[] nameBuf,int bufLoc,int rock)
	{
		String filename="";
		for (int i=bufLoc;nameBuf[i]!=0;i++)
			filename+=(char)nameBuf[i];
		
		switch (usage&255){
		case FILEUSAGE_DATA:
			filename+=".dat";
			break;
		case FILEUSAGE_SAVEGAME:
			filename+=".sav";
			break;
		case FILEUSAGE_TRANSCRIPT:
			filename+=".txt";
			break;
		case FILEUSAGE_INPUTRECORD:
			filename+=".rec";
			break;
		default:
			return (0);}

		FileRef file=FileRef.createFileRef(context,homeDir,filename,rock);
		files.put(new Integer(nextFileID), file);
		nextFileID++;
		
		return (nextFileID-1);
	}
	
	private void copyFile(File inFile,File outFile) throws IOException
	{
		byte[] buffer=new byte[10000];
		FileInputStream from=new FileInputStream(inFile);
		FileOutputStream to=new FileOutputStream(outFile);
		int count=0;
		while (count!=-1){
			to.write(buffer,0,count);
			count=from.read(buffer);}
		from.close();
		to.close();
	}
	
	public int fileRefDoesFileExist(int file)
	{
		FileRef fileObj= files.get(new Integer(file));
		if (fileObj==null){
			return (0);}

		if (fileObj.exists())
			return (1);
		else
			return (0);
	}
	
	public void fileRefDeleteFile(int file)
	{
		FileRef fileObj= files.get(new Integer(file));
		if (fileObj!=null)
			fileObj.delete();
	}
	
	public void fileRefDestroy(int file)
	{
		files.remove(new Integer(file));
	}
	
	public int imageDraw(int win,int img,int x,int y) throws InterruptedException
	{
		Window winObj= windows.get(new Integer(win));
		if (winObj==null)
			return (0);
		
		if (winObj instanceof TextBufferWindow){
			((TextBufferWindow) winObj).putImage(img);
			return (1);}
		else{
			return (0);}
	} 
	         
	public void setBackgroundImage(int img)
	{
		if (mainWin!=null) 
			mainWin.setBackground(img);
	}
	
	public void showAltImage(int img) throws IOException, InterruptedException
	{
		Intent imageIntent=new Intent(context,ShowImage.class);
		imageIntent.putExtra("image", img);

		context.startActivityForResult(imageIntent,2);
		
		synchronized (this){
			while (!dialogFinished)
				wait();}
	}
	
	public void select(byte[] event,int pos) throws IOException
	{
		if (mainWin!=null){
			if (dialogFinished){
				dialogFinished=false;}
			else{
				if (startPage!=null){
					mainWin.setPage(startPage,selected);
					startPage=null;}
				else{
					if (pageToBottom)
						context.runOnUiThread(new Runnable(){
							public void run(){
								mainWin.makePageFromTop(null,false,true,false);}});
					else
						context.runOnUiThread(new Runnable(){
							public void run(){
								mainWin.makePageFromTop(null,false,false,true);}});
					pageToBottom=false;}
			}
		}

		selected=true;

		eventArray=event;
		eventPos=pos;
		
		if (nextCommand.length()>0){
			
			DataTransforms.putInt(eventArray, eventPos+EVENT_WIN, mainWin.getId());
			DataTransforms.putInt(eventArray, eventPos+EVENT_VAL2, 0);}
		
		else{
			
			if (!active)
				return;
			
			context.runOnUiThread(new Runnable(){
				public void run(){
					storyPanel.invalidate();}});
			
			DataTransforms.putInt(event, pos+EVENT_TYPE, EVTYPE_NONE);

			if (!active)
				return;

			mainWin.gestureListen(true);
			context.runOnUiThread(new Runnable(){
				public void run(){
					context.setProgressBarIndeterminateVisibility(false);}});
			
			if (inputArea.getReady()){
				inputArea.activate();}

			if (!active)
				return;
			
			synchronized (this){
				while (active&&DataTransforms.getInt(event, pos+EVENT_TYPE)==EVTYPE_NONE)
					try {
						wait();
					} catch (InterruptedException e) {
						return;
					}}

			if (!active)
				return;
			
			mainWin.gestureListen(false);}
		context.runOnUiThread(new Runnable(){
				public void run(){
					context.setProgressBarIndeterminateVisibility(true);}});

		if (mainWin!=null)
			paragraphCount=mainWin.getParagraphCount();

		if (DataTransforms.getInt(eventArray, eventPos+EVENT_TYPE)==EVTYPE_LINEINPUT){
			inputArea.finalizeInput();}

		if (mainWin!=null)
			mainWin.updateStartParaNum();
	}
	
	synchronized void resume(int type,int win,int val1,int val2)
	{
		DataTransforms.putInt(eventArray, eventPos+EVENT_TYPE, type);
		DataTransforms.putInt(eventArray, eventPos+EVENT_WIN, win);
		DataTransforms.putInt(eventArray, eventPos+EVENT_VAL1, val1);
		DataTransforms.putInt(eventArray, eventPos+EVENT_VAL2, val2);

		notifyAll();
	}
	
	public synchronized void resumeFromDialog(String result)
	{
		dialogFinished=true;
		dialogResult=result;

		toggleKeyboard(false);
		
		if (mainWin!=null){
			mainWin.setBitbucket(true);
			mainWin.trimParagraphs(mainWin.getParagraphCount()-2);}

		notifyAll();
	}
	
	public synchronized void resumeFromWindowAdd()
	{
		notifyAll();
	}

	void toggleKeyboard(boolean keyboard)
	{
		if (keyboard){
			if (!inputArea.isVisible()){
				frame.addView(inputArea);}
			return;}

		if (inputArea.isVisible()){
			frame.removeView(inputArea);}
	}
	
	public void toggleKeyboardFromThread(final boolean keyboard)
	{
		context.runOnUiThread(new Runnable(){
			public void run(){
				toggleKeyboard(keyboard);}});
	}
	
	public void formFeed()
	{
		if (mainWin!=null)
			mainWin.formFeed();
	}
	
	void deleteDirectoryOrFile(File path)
	{
		if (path.exists()){
			if (path.isDirectory()){
				File[] files=path.listFiles();
				for (int i=0;i<files.length;i++){
					deleteDirectoryOrFile(files[i]);}
			}
			path.delete();}
	}
	
	public String copySaveDir(String dirName,FileRef[] undoFiles,int mode) throws IOException, ClassNotFoundException, InterruptedException
	{

		FileRef dir=FileRef.createFileRef(context,homeDir,dirName,0);
		if (mode==FILEMODE_WRITE){
			if (dir.exists())
				deleteDirectoryOrFile(dir);
			dir.mkdir();
			for (int i=0;i<undoFiles.length;i++){
				if (undoFiles[i].exists()){
					File output=new File(dir,undoFiles[i].getName());
					copyFile(undoFiles[i],output);}
			}
		}
		else{
			if (!dir.exists())
				return(null);
			for (int i=0;i<undoFiles.length;i++){
				undoFiles[i].delete();
				File input=new File(dir,undoFiles[i].getName());
				if (input.exists())
					copyFile(input,undoFiles[i]);}
			readSerializedMainWin();}

		return (dir.getName()+"/"+dirName);
	}
	
	int getFontSize() throws IOException
	{
		int result;
		
		FileRef fontFile=FileRef.createFileRef(context,homeDir,"font.dat",0);
		if (fontFile.exists()){
			FileStream in=new FileStream(1000,fontFile,FILEMODE_READ,0,false);
			result=in.readInt();
			in.close();}
		else{
			result=onPhone() ? scaleFont(18) : scaleFont(21);}

		return (result);
	}
	
	//Scales a font's point size to accord with the device's display density.
	//ALL font sizes should be passed through this function to future-proof apps.
	//The only exception: fonts used in TextViews.
    private int scaleFont(int size) {
		final float scale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (0.5f + size * scale);
    }
    
	//Returns true if we're running on a phone -- screen size medium or smaller. Otherwise we must be on a tablet.
	private boolean onPhone() {
		return ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
				< Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

	private void changeFontSize(int newSize,boolean record) throws IOException
	{
		textBufferStyles.mapFonts(newSize);
		mainWin.updateCharSizes();
		inputArea.resetFont();
		mainWin.reprintPage();
		if (record){
			FileRef fontFile=FileRef.createFileRef(context,homeDir,"font.dat",0);
			FileStream out=new FileStream(1000,fontFile,FILEMODE_WRITE,0,false);
			out.writeInt(newSize);
			out.close();}
	}
	
	void growFont()
	{
		try {
			int curSize=getFontSize();
			if (curSize<scaleFont(36))
				changeFontSize(curSize+scaleFont(1),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void shrinkFont()
	{
		try {
			int curSize=getFontSize();
			if (curSize>scaleFont(17))
				changeFontSize(curSize-scaleFont(1),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readSerializedMainWin() throws IOException, ClassNotFoundException, InterruptedException
	{
		FileRef file= files.get(new Integer(1));
		if (file.exists()){
			ObjectInputStream in=new ObjectInputStream(new FileInputStream(file));
			FileRef saveFile=FileRef.createFileRef(context,homeDir,in.readUTF(),0);
			if (saveFile.exists()&&saveFile.getName().compareTo("current.sav")!=0){
				FileRef current=FileRef.createFileRef(context,homeDir,"current.sav",0);
				current.delete();
				copyFile(saveFile,current);}
			startPage=(PrintPage)in.readObject();
			in.close();}
	}
	
	public void setActive(boolean active)
	{
		this.active=active;
	}
	
	public void printFromBottom()
	{
		pageToBottom=true;
	}
	
	public void clearTextBuffer()
	{
		if (mainWin!=null)
			mainWin.clear(helpActive);
	}

	public void clearFiles()
	{
		FileRef file= files.get(new Integer(1));
		file.delete();
		int limit= files.lastKey().intValue();
		for (int i=1;i<=limit;i++){
			file= files.get(new Integer(i));
			if (file!=null){
				file.delete();
				if (i>1)
					files.remove(new Integer(i));}
		}
		nextFileID=2;
	}
	
	public void saveTextBuffer(int str) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null||!(strObj instanceof FileStream)||mainWin==null)
			return;
		
		mainWin.saveTextBuffer((FileStream) strObj);
	}
	
	public void storeTextBuffer() throws IOException, InterruptedException
	{
		if (mainWin!=null){
			mainWin.trimParagraphs(paragraphCount);
			mainWin.storeTextBuffer();
			helpActive=true;}
	}
	
	public void restoreTextBuffer() throws InterruptedException
	{
		if (mainWin!=null){
			mainWin.restoreTextBuffer();
			helpActive=false;}
	}
	
	public void writeInt(int str,int val) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj!=null&&strObj instanceof FileStream)
			((FileStream) strObj).writeInt(val);
	}

	public void loadTextBuffer(int str) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null||!(strObj instanceof FileStream)||mainWin==null)
			return;

		mainWin.loadTextBuffer((FileStream)strObj);
	}
	
	public int readInt(int str) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null||!(strObj instanceof FileStream))
			return (0);
			
		return (((FileStream) strObj).readInt());
	}
	
	void openMenu()
	{
		context.openContextMenu(mainWin);
	}
	
	public void doCommand(String command)
	{
		if (inputArea.getActive())
			inputArea.doCommand(command);
	}
	
	public void toggleUndoMenu(int enabled)
	{
		undosAvail=(enabled>0);
	}
	
	public boolean getUndosAvail()
	{
		return undosAvail;
	}

	public boolean bookmarksAvail()
	{
		FileRef bookmarks=FileRef.createFileRef(context,homeDir,"bookmarks",0);
		return (bookmarks.exists());
	}
	
	public void insertTextJump(byte[] nameBuf,int bufLoc)
	{
		String name="";
		for (int i=bufLoc;nameBuf[i]!=0;i++)
			name+=(char)nameBuf[i];
		
		for (int i=0;i<textJumps.size();i++)
			if (textJumps.get(i).name.compareTo(name)==0)
				return;
		
		textJumps.add(new TextJump(name,mainWin.getParagraphCount()));
	}
	
	public void clearTextJumps()
	{
		textJumps.clear();
	}
	
	public void saveTextJumps(int str) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null||!(strObj instanceof FileStream))
			return;
		
		((FileStream)strObj).writeInt(textJumps.size());
		for (int i=0;i<textJumps.size();i++){
			((FileStream)strObj).writeString(textJumps.get(i).name);
			((FileStream)strObj).writeInt(textJumps.get(i).loc);}
	}
	
	public void restoreTextJumps(int str) throws IOException
	{
		Stream strObj= streams.get(new Integer(str));
		if (strObj==null||!(strObj instanceof FileStream))
			return;
		
		textJumps.clear();
		
		int limit=((FileStream)strObj).readInt();
		for (int i=0;i<limit;i++)
			textJumps.add(new TextJump(((FileStream)strObj).readString(),((FileStream)strObj).readInt()));
	}
	
	public ArrayList<TextJump> getTextJumps()
	{
		return (textJumps);
	}
	
	public void textJumpTo(int pos)
	{
		if (mainWin!=null){
			mainWin.jumpTo(pos);}
	}
	
	public void finalize()
	{
		if (mainWin!=null)
			mainWin.finalize();
		
		context=null;
	}
	
	public boolean helpUp()
	{
		if (mainWin==null)
			return (false);
		
		return (mainWin.helpUp());
	}
	
	public void makePage() 
	{
		if (mainWin != null)
			mainWin.setCustomRedraw(true);
	}
}
