Android Typography by Jimmy Maher begins here.

"Defines custom text styles for Android games."

Chapter - Android Typography

Use no status line translates as (- Constant USE_NO_STATUS_LINE 1; -).

[A couple of useful custom text styles that display nicely on Android.]

Include (-

[   InitGlkWindow winrock;
      switch (winrock) { 
        GG_MAINWIN_ROCK: 

	glk_stylehint_set(wintype_TextBuffer, style_User1, stylehint_Justification,stylehint_just_Centered);
	glk_stylehint_set(wintype_TextBuffer, style_User1, stylehint_Size,1);
	glk_stylehint_set(wintype_TextBuffer, style_User1, stylehint_Weight,1);
	glk_stylehint_set(wintype_TextBuffer, style_User1, stylehint_Oblique,0);
	glk_stylehint_set(wintype_TextBuffer, style_User1, stylehint_Proportional,1);
	glk_stylehint_set(wintype_TextBuffer, style_User1, stylehint_ReverseColor,0);

	glk_stylehint_set(wintype_TextBuffer, style_User2, stylehint_Justification,stylehint_just_LeftFlush);
	glk_stylehint_set(wintype_TextBuffer, style_User2, stylehint_Size,0);
	glk_stylehint_set(wintype_TextBuffer, style_User2, stylehint_Weight,1);
	glk_stylehint_set(wintype_TextBuffer, style_User2, stylehint_Oblique,1);
	glk_stylehint_set(wintype_TextBuffer, style_User2, stylehint_Proportional,1);
	glk_stylehint_set(wintype_TextBuffer, style_User2, stylehint_ReverseColor,0);
	
	#ifdef USE_NO_STATUS_LINE;
	GG_STATUSWIN_ROCK:
		rtrue;
	#endif;
};

      rfalse; ! leaving out this line will lead to a messy crash! 
   ]; 
-) after "Definitions.i6t".

[Note that for text centering to work properly you must always end a title type section with a line break, before transitioning to another style such as roman.]

To say title type: (- glk_set_style(style_User1); -) .

[Tutor type is a nice script for an in-game tutorial -- thus the name. Also used by the Android menu system.]

To say tutor type: (- glk_set_style(style_User2); -).

Section - Replacement of DrawStatusLine()

Include (-

#ifdef USE_NO_STATUS_LINE;
Replace DrawStatusLine;
#endif;


-) after "Definitions.i6t".

Include (-

#ifdef USE_NO_STATUS_LINE;
[ DrawStatusLine;
]; 
#endif;

-) after "Definitions.i6t".


Android Typography ends here.