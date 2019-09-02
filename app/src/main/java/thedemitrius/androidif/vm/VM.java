package thedemitrius.androidif.vm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import thedemitrius.androidif.glk.FileRef;
import thedemitrius.androidif.glk.GLK;
import thedemitrius.androidif.glk.GLKFrame;
import thedemitrius.androidif.glk.TextJump;

public class VM
{
	private String storyName;
	private Activity context;
	private GLKFrame frame;
	
	private GLK glk;
	private Glulx glulx;
	
	private boolean running;
	
	public VM(String storyName,Activity context,GLKFrame frame)
	{
		this.storyName=storyName;
		this.context=context;
		this.frame=frame;
		
		glk=null;
		glulx=null;
		
		running=false;
	}
	
	public void run()
	{
		if (running){
			File dir;
			if (FileRef.isExternalStorageAvailable()&&!FileRef.isExternalStorageReadOnly())
				dir=new File(context.getExternalFilesDir(null),storyName);
			else
				dir=new File(context.getFilesDir(),storyName);
			if (!dir.exists())
				dir.mkdir();}

		if (glk==null&&running){
			try {
				glk=new GLK(context,frame,storyName);
			} catch (IOException e1) {}

		if (glulx==null&&running){
			try {
				glulx=new Glulx(glk);
			} catch (IOException e) {}}
			try {
				if (running)
					glk.readSerializedMainWin();
				else
					glulx=null;
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			} catch (InterruptedException e) {
			}}

		if (running){
            assert glulx != null;
            glulx.setRunning(true);
			glulx.run();}
	}
	
	public boolean getSelecting()
	{
		if (glulx==null)
			return (false);
		
		return (glulx.getSelecting());
	}
	
	public void setRunning(boolean running)
	{
		this.running=running;
		
		if (glulx!=null)
			glulx.setRunning(running);
		
		if (glk!=null)
			glk.setActive(running);
	}
	
	public void resumeFromDialog(String result)
	{
		if (glk!=null)
			glk.resumeFromDialog(result);
	}
	
	public void cleanExit()
	{
		if (glulx==null||glk==null){
			return;}

		glulx.setQuitPending(true);
		glk.setActive(false);
		glulx.setRunning(false);
		
		synchronized(glk){
			glk.notifyAll();}

		glk.finalize();

		glk=null;
		glulx=null;
		context=null;
	}
	
	public void doCommand(String command)
	{
		if (glk!=null)
			glk.doCommand(command);
	}
	
	public boolean undosAvail()
	{
		if (glk!=null)
			return (glk.getUndosAvail());
		else
			return (false);
	}
	
	public boolean bookmarksAvail()
	{
		if (glk!=null)
			return (glk.bookmarksAvail());
		else
			return (false);
	}
	
	public ArrayList<TextJump> getTextJumps()
	{
		if (glk!=null)
			return (glk.getTextJumps());
		else
			return (null);
	}
	
	public void textJumpTo(int pos)
	{
		glk.textJumpTo(pos);
	}
	
	public boolean helpUp()
	{
		return (glk != null && glk.helpUp());
	}
	
	public void remakePage() {
		if (glk != null) {
			glk.makePage();
		}
	}
}
