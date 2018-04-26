package thedemitrius.androidif.glk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class NameDialog extends AlertDialog implements DialogInterface.OnShowListener, DialogInterface.OnClickListener
{
	private FileDialog context;
	private int id;
	
	private EditText input;

	public NameDialog(FileDialog context,String msg,String initialText,int id) 
	{
		super(context);
		
		this.context=context;
		this.id=id;
		
		setOwnerActivity(context);
		
		setTitle("Choose a name...");
		setMessage(msg);
		
		input=new EditText(context);
		input.setText(initialText);
		setView(input);
		
		setButton(AlertDialog.BUTTON_POSITIVE,"Okay",this);
		setButton(AlertDialog.BUTTON_NEGATIVE,"Cancel",this);
		
		setOnShowListener(this);
	}

	public void onShow(DialogInterface dialog) 
	{
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(input, 0);
	}

	public void onClick(DialogInterface dialog, int which) 
	{
		if (which==AlertDialog.BUTTON_POSITIVE)
			context.saveBookmark(input.getText().toString(), id);
	}
	
	public boolean onKeyUp(int keyCode,KeyEvent event)
	{
		if (keyCode==KeyEvent.KEYCODE_ENTER){
			context.saveBookmark(input.getText().toString(), id);
			dismiss();
			return (true);}

		return (false);
	}
}
