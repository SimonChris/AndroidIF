package thedemitrius.androidif.glk;

public interface GLKConstants {
	
	int WINTYPE_ALLTYPES=0;
	int WINTYPE_PAIR=1;
	int WINTYPE_BLANK=2;
	int WINTYPE_TEXTBUFFER=3;
	int WINTYPE_TEXTGRID=4;
	int WINTYPE_GRAPHICS=5;
	
	int WINMETHOD_LEFT=0;
	int WINMETHOD_RIGHT=1;
	int WINMETHOD_ABOVE=2;
	int WINMETHOD_BELOW=3;
	int WINMETHOD_FIXED=16;
	int WINMETHOD_PROPORTIONAL=32;
	
	int STYLE_NORMAL=0;
	int STYLE_EMPHASIZED=1;
	int STYLE_PREFORMATTED=2;
	int STYLE_HEADER=3;
	int STYLE_SUBHEADER=4;
	int STYLE_ALERT=5;
	int STYLE_NOTE=6;
	int STYLE_BLOCKQUOTE=7;
	int STYLE_INPUT=8;
	int STYLE_USER1=9;
	int STYLE_USER2=10;
	
	int STYLEHINT_INDENTATION=0;
	int STYLEHINT_PARAINDENTATION=1;
	int STYLEHINT_JUSTIFICATION=2;
	int STYLEHINT_SIZE=3;
	int STYLEHINT_WEIGHT=4;
	int STYLEHINT_OBLIQUE=5;
	int STYLEHINT_PROPORTIONAL=6;
	int STYLEHINT_TEXTCOLOR=7;
	int STYLEHINT_BACKCOLOR=8;
	int STYLEHINT_REVERSECOLOR=9;
	
	int STYLEHINT_JUST_LEFTFLUSH=0;
	int STYLEHINT_JUST_LEFTRIGHT=1;
	int STYLEHINT_JUST_CENTERED=2;
	int STYLEHINT_JUST_RIGHTFLUSH=3;
	
	int EVTYPE_NONE=0;
	int EVTYPE_TIMER=1;
	int EVTYPE_CHARINPUT=2;
	int EVTYPE_LINEINPUT=3;
	int EVTYPE_MOUSEINPUT=4;
	int EVTYPE_ARRANGE=5;
	int EVTYPE_REDRAW=6;
	int EVTYPE_SOUNDNOTIFY=7;
	int EVTYPE_HYPERLINK=8;
	
	int EVENT_TYPE=0;
	int EVENT_WIN=4;
	int EVENT_VAL1=8;
	int EVENT_VAL2=12;
	
	int FILEUSAGE_DATA=0;
	int FILEUSAGE_SAVEGAME=1;
	int FILEUSAGE_TRANSCRIPT=2;
	int FILEUSAGE_INPUTRECORD=3;
	
	int FILEUSAGE_TEXTMODE=0;
	int FILEUSAGE_BINARYMODE=256;
	
	int FILEMODE_WRITE=1;
	int FILEMODE_READ=2;
	int FILEMODE_READWRITE=3;
	int FILEMODE_WRITEAPPEND=5;
	
	int KEYCODE_LEFT=-2;
	int KEYCODE_RIGHT=-3;
	int KEYCODE_UP=-4;
	int KEYCODE_DOWN=-5;
	int KEYCODE_RETURN=-6;
	int KEYCODE_DELETE=-7;
	int KEYCODE_ESCAPE=-8;
	
	int GESTALT_VERSION=0;
	int GESTALT_CHARINPUT=1;
	int GESTALT_LINEINPUT=2;
	int GESTALT_CHAROUTPUT=3;
	int GESTALT_CHAROUTPUT_CANNOTPRINT=0;
	int GESTALT_CHAROUTPUT_APPROXPRINT=1;
	int GESTALT_CHAROUTPUT_EXACTPRINT=2;
	int GESTALT_TIMER=5;
	int GESTALT_GRAPHICS=6;
	int GESTALT_DRAWIMAGE=7;
	int GESTALT_HYPERLINKS=11;
	int GESTALT_HYPERLINKINPUT=12;
	int GESTALT_GRAPHICSTRANSPARENCY=14;
	int GESTALT_UNICODE=15;
	
	int SEEKMODE_START=0;
	int SEEKMODE_CURRENT=1;
	int SEEKMODE_END=2;
}
