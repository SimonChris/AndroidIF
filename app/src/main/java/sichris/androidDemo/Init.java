package sichris.androidDemo;

import sichris.androidDemo.glk.GLKFrame;
import sichris.androidDemo.vm.VM;
import sichris.androidDemo.vm.VMThread;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
  
public class Init extends Activity
{ 
    private VM vm;
    private VMThread thread;
	private MediaPlayer songPlayer;
         
    private boolean fromDialog;
        
    private static final int MENU_HELP=1;
    private static final int MENU_UNDO=2;
    private static final int MENU_SAVE=3;
    private static final int MENU_RESTORE=4;
    private static final int MENU_RESTART=5;
	private static final int MENU_SONG=6;
	private static final int MENU_HINT=7;

	public static final String storyURL="/assets/";

    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	fromDialog=false;
    	if (onPhone())
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	setProgressBarIndeterminateVisibility(true);
    	
    	ImageView logo=new ImageView(this);
    	setContentView(logo);

    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	
    	GLKFrame frame=new GLKFrame(this);

		songPlayer = MediaPlayer.create(getApplicationContext(), R.raw.song);

		vm=new VM("androidDemo",this,frame);
    }
    
    public void onStart()
    {
    	super.onStart();
    	
    	if (fromDialog){
    		fromDialog=false;
    		return;}

		if (!vm.getSelecting()){
			vm.setRunning(true);
			thread=new VMThread(vm);
			thread.start();}
	}
    
    public void onStop()
    {
    	super.onStop();
    	
    	if (!vm.getSelecting())
    		vm.setRunning(false);
     }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	
    	vm=null;
    	thread.finalize();
    	thread=null;
    }
    
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
    	fromDialog=true;
    	
    	switch (requestCode)
    	{
    	case 1:
    		if (resultCode==Activity.RESULT_CANCELED)
    			vm.resumeFromDialog(null);
    		else
    			vm.resumeFromDialog(data.getStringExtra("result"));
    		return;
    	case 2:
    		vm.resumeFromDialog(null);
    		return;}
	}
    
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	menu.clear();
    	
    	if (vm.helpUp())
    		return (true);
    	
     	menu.add(Menu.NONE, MENU_HELP, MENU_HELP, "About this story");
     	menu.add(Menu.NONE, MENU_UNDO, MENU_UNDO, "Undo your last turn");
     	menu.add(Menu.NONE, MENU_SAVE, MENU_SAVE, "Save a bookmark");
     	menu.add(Menu.NONE, MENU_RESTORE, MENU_RESTORE, "Restore a bookmark");
    	menu.add(Menu.NONE, MENU_RESTART, MENU_RESTART, "Restart your story");
		menu.add(Menu.NONE, MENU_SONG, MENU_SONG, "Play/pause theme song");
		menu.add(Menu.NONE, MENU_HINT, MENU_HINT, "Get a hint");

		menu.findItem(MENU_UNDO).setEnabled(vm.undosAvail());
    	menu.findItem(MENU_RESTORE).setEnabled(vm.bookmarksAvail());

    	return (true);
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId()){
    	case MENU_HELP:
    		vm.doCommand("about");
    		return (true);
    	case MENU_UNDO:
    		vm.doCommand("undo");
    		return (true);
    	case MENU_SAVE:
    		vm.doCommand("save");
    		return (true);
    	case MENU_RESTORE:
    		vm.doCommand("restore");
    		return (true);
    	case MENU_RESTART:
    		vm.doCommand("restart");
    		return (true);
		case MENU_SONG:
			onPlayThemeSong();
			return (true);
    	case MENU_HINT:
    		vm.doCommand("hint");
    		return (true);
    	default:
    		return (false);
    	}
    }

	private void onPlayThemeSong()
	{
		if(songPlayer.isPlaying())
		{
			songPlayer.pause();
		}
		else {
			songPlayer.start();
		}
	}
      
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	menu.setHeaderTitle("Demo game");
    	
    	menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "About this story");
    	
    	menu.add(Menu.NONE, MENU_UNDO, Menu.NONE, "Undo your last turn");
    	menu.findItem(MENU_UNDO).setEnabled(vm.undosAvail());
    	
    	menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, "Save a bookmark");
    	
    	menu.add(Menu.NONE, MENU_RESTORE, Menu.NONE, "Restore a bookmark");
    	menu.findItem(MENU_RESTORE).setEnabled(vm.bookmarksAvail());
     	
    	menu.add(Menu.NONE, MENU_RESTART, Menu.NONE, "Restart your story");

		menu.add(Menu.NONE, MENU_SONG, Menu.NONE, "Play/pause theme song");

		menu.add(Menu.NONE, MENU_HINT, Menu.NONE, "Get a hint");
	}
    
    public boolean onContextItemSelected(MenuItem item)
    {
    	switch (item.getItemId()){
    	case MENU_HELP:
    		vm.doCommand("about");
    		return (true);
    	case MENU_UNDO:
    		vm.doCommand("undo");
    		return (true);
    	case MENU_SAVE:  
    		vm.doCommand("save");
    		return (true);  
    	case MENU_RESTORE:
    		vm.doCommand("restore");
    		return (true);
    	case MENU_RESTART:
    		vm.doCommand("restart");
    		return (true);
    	case MENU_HINT:
    		vm.doCommand("hint");
    		return (true);
    	default:
    		return (false);
    	}
    }
     
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	if (vm != null && vm.getSelecting()) {
    		vm.remakePage();
    	}
    }
    
	//Returns true if we're running on a phone -- screen size medium or smaller. Otherwise we must be on a tablet.
	boolean onPhone() {
		return ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
				< Configuration.SCREENLAYOUT_SIZE_LARGE);
	}
}