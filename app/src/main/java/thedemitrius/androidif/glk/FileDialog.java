package thedemitrius.androidif.glk;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FileDialog extends Activity implements GLKConstants, View.OnTouchListener
{
	private LinearLayout frame;
	private TextView cancelSlot;
	private TextView[] saveSlots;
	private boolean[] slotUsed;
		
	private String ext;
	private String dir;
	private String confirmMessage;
	
	private boolean writeFile;

    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	
    	Intent intent=getIntent();
    	ext=intent.getStringExtra("ext");
    	dir=intent.getStringExtra("dir");
		String initialMessage = intent.getStringExtra("initialMessage");
    	confirmMessage=intent.getStringExtra("confirmMessage");

		writeFile = confirmMessage != null;

    	ScrollView scroller=new ScrollView(this);
    	
    	frame=new LinearLayout(this);
    	frame.setOrientation(LinearLayout.VERTICAL);
    	
    	saveSlots=new TextView[10];
    	slotUsed=new boolean[10];
    	for (int i=0;i<10;i++)
    		slotUsed[i]=false;
    	
    	frame.addView(new TextView(this));
    	frame.addView(new TextView(this));
	
    	cancelSlot=getTextViewMultiline(initialMessage);
    	frame.addView(cancelSlot);
    	
    	frame.addView(new TextView(this));
    	frame.addView(new TextView(this));
   	
    	for (int i=0;i<10;i++){
    		saveSlots[i]=addFileLabel("(Bookmark Not Used)");
    		frame.addView(new TextView(this));}

		frame.addView(new TextView(this));
    	
    	frame.setFocusable(true);
    	
    	scroller.addView(frame);
    	setContentView(scroller);
    	
		FileRef bookmarksFile=FileRef.createFileRef(this,dir,"bookmarks",0);
		if (bookmarksFile.exists()){
			try{
				FileStream bookmarksStream=new FileStream(0,bookmarksFile,FILEMODE_READ,0,false);
				for (int i=0;i<10;i++){
					slotUsed[i]=bookmarksStream.readBoolean();
					if (slotUsed[i])
						saveSlots[i].setText(bookmarksStream.readString());}
				bookmarksStream.close();}
			catch (IOException e){
				finish();}
		}
	}
    
    private TextView getTextViewMultiline(String text)
    {
    	TextView result=new TextView(this);
    	result.setText("\n"+text);
    	result.setFocusable(false);
    	result.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
    	result.setTextSize(onPhone() ? 18 : 25);
    	result.setPadding(10, 0, 10, 0);
    	result.setOnTouchListener(this);
    	return result;
    }
    
    private TextView addFileLabel(String filename)
    {
    	TextView label=new TextView(this);
    	label.setText(filename);
    	label.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
       	label.setTextSize(onPhone() ? 18 : 25);
        label.setGravity(Gravity.CENTER_HORIZONTAL);
    	label.setFocusable(true);
    	label.setOnTouchListener(this);
    	frame.addView(label);
    	return (label);
    }
    
	private void nameBookmark(int selected)
	{
		String text;
		if (slotUsed[selected])
			text=saveSlots[selected].getText().toString();
		else
			text="";
		
		NameDialog nameDialog=new NameDialog(this,confirmMessage,text,selected);
		nameDialog.show();
	}
	
	void saveBookmark(String result,int selected)
	{
		if (result!=null){
		
			if (result.length()==0)
				result="Unnamed Bookmark";
		
			result=result.replace('\n',' ');
			if (result.length()>50)
				result=result.substring(0,60)+"...";
		
			FileRef bookmarksFile=FileRef.createFileRef(this,dir,"bookmarks",0);
			if (bookmarksFile.exists())
				bookmarksFile.delete();
			slotUsed[selected]=true;
			saveSlots[selected].setText(result);
			try{
				FileStream bookmarksStream=new FileStream(0,bookmarksFile,FILEMODE_WRITE,0,false);
				for (int i=0;i<10;i++){
					bookmarksStream.writeBoolean(slotUsed[i]);
					if (slotUsed[i])
						bookmarksStream.writeString(saveSlots[i].getText().toString());}
				bookmarksStream.close();}
			catch (IOException e){
				return;}

			Intent intent=new Intent();
			intent.putExtra("result", "b"+selected+ext);
			setResult(Activity.RESULT_OK,intent);
			finish();}
	}
     

	public boolean onTouch(View v, MotionEvent event) 
	{
		if (event.getActionMasked()!=MotionEvent.ACTION_UP){
			v.performClick();
			return (true);}
		
		if (v==cancelSlot){
			setResult(Activity.RESULT_CANCELED);
			finish();}
		else{
			for (int i=0;i<10;i++){
				if (v==saveSlots[i]){
					if (writeFile)
						nameBookmark(i);
					else
						if (slotUsed[i]){
							Intent intent=new Intent();
							intent.putExtra("result", "b"+i+ext);
							setResult(Activity.RESULT_OK,intent);
							finish();}
					break;}
			}
		}

		return (true);
	}
	
	//Returns true if we're running on a phone -- screen size medium or smaller. Otherwise we must be on a tablet.
	boolean onPhone() {
		return ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
				< Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

    
}