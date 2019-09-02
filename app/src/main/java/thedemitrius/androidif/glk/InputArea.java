package thedemitrius.androidif.glk;

import java.io.IOException;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

class InputArea extends EditText implements GLKConstants
{
	Context context;
	private GLK glk;
	 
	private TextBufferWindow window;
	private byte[] buf;
	private int bufStart;
	
	private boolean ready;
	private boolean active;

	public InputArea(Context context, GLK glk) {
		super(context);
		 
		this.context=context;
		this.glk=glk;
		
		window=null;
		buf=null;
		bufStart=0;
		
		ready=false;
		active=false;

        final InputArea inputArea = this;

		//Submits the keyboard input when a carriage return is detected.
        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s){
                String text = s.toString();
                if(text.contains("\n"))
                {
                    inputArea.setText(text.replace("\n", ""));
                    try {
                        inputArea.deactivate();
                    }
                    catch(Exception e)
                    {
                    }
                }
            }
            public void  beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            public void  onTextChanged (CharSequence s, int start, int before,int count) {
            }
        };

        this.addTextChangedListener(tw);

		setFocusable(true);
		setFocusableInTouchMode(true);
		setRawInputType(InputType.TYPE_CLASS_TEXT);

		setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        setLines(1);
	}

	void setup(final TextBufferWindow window,final byte[] buf,final int bufStart,int maxLen,final int initLen)
	{
		this.window=window;
		this.buf=buf;
		this.bufStart=bufStart;
		
		post(new Runnable(){
			public void run(){
				setTypeface(window.stream.getFont(STYLE_INPUT).getTypeface());
				if (initLen>0){
					setText("");}}});

		window.setLineInputPending(true);
			
		ready=true;
	}
	
	void resetFont()
	{
		post(new Runnable(){
			public void run(){
				setTypeface(window.stream.getFont(STYLE_INPUT).getTypeface());}});
	}
	
	boolean getReady()
	{
		return (ready);
	}
	
	void activate()
	{
		active=true;
	}
	
	void deactivate()
	{
		active=false;
		ready=false;
		
		window.setLineInputPending(false);
		
		glk.resume(EVTYPE_LINEINPUT, window.getId(), getText().length(), 0);
	}
	
	boolean getActive()
	{
		return (active);
	}
	
	
	void finalizeInput() throws IOException
	{
		String command=getText().toString();
		
		window.setBitbucket(false);
		int oldStyle=window.stream.getStyle();
		window.stream.setStyle(STYLE_INPUT);
        window.putString(command+"\n\n");

        window.stream.setStyle(oldStyle);

		for (int i=0;i<command.length();i++){
			buf[bufStart+i]=(byte)command.charAt(i);}

		buf[bufStart+command.length()]=0;

		if (window.stream.echoing()){
			window.stream.getEchoStream().putString(buf, bufStart);
			window.stream.getEchoStream().putChar((byte)'\n');}

		buf=null;
		command=null;

		post(new Runnable(){
			public void run(){
				setText("");}});
	}
	
	void doCommand(String command)
	{
		setText(command);
		deactivate();
	}
	
	void display(boolean toggle)
	{
		glk.toggleKeyboard(toggle);
	}

	public boolean isVisible()
	{
		return (getParent()!=null);
	}

    public int getIdealHeight()
    {
        return Math.max((int)(2.5 * getTextSize()), getSuggestedMinimumHeight());
    }

	public void onWindowVisibilityChanged(int invisibility)
	{
		if (invisibility==View.VISIBLE){
			showKeyboard();
			setText("");}
		else{
	        hideKeyboard();}
	}

	void showKeyboard()
	{
		requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		assert imm != null;
		imm.showSoftInput(this, 0);
	}
	
	void hideKeyboard()
	{
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		assert imm != null;
		imm.hideSoftInputFromWindow(this.getWindowToken(),0);
	}

	public void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
	{
		int width=Math.max(View.MeasureSpec.getSize(widthMeasureSpec), getSuggestedMinimumWidth());
		int height=Math.max(View.MeasureSpec.getSize(heightMeasureSpec), getSuggestedMinimumHeight());
		setMeasuredDimension(width,height);
	}

    //Originally used to submit the keyboard input when a carriage return was detected.
    //This event does not trigger on devices running Android 5.0 (Lollipop) and
    //has therefore been replaced by a TextWatcher initialized in the InputArea
    //constructor.
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if (!active)
			return (true);

//		if (keyCode==KeyEvent.KEYCODE_ENTER){
//			deactivate();
//			return (true);};

		return (super.onKeyDown(keyCode,event));
	}
}
