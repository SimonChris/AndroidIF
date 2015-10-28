Android IF by Jimmy Maher begins here.

"Prepares a game for release on Android."

Include Basic Screen Effects by Emily Short.
Include Android Typography by Jimmy Maher.

Chapter - Inform 6 Variable Decarations

[We save current state to the file system every single turn. This lets us save some memory by using these saves for undo, and also guarantees that a current saved state is always present when the user exits and returns to the app. This also, of course, guards against a loss of progress due to the worst -- a crash.]

[The following Arrays hold the filenames for each of our 6 save states -- "0" through "5" (the ".sav" extension is appended by the GLK library). Other miscellanious global variables also declared and initialized here.]

Include 
(-
Array sav0 -> 226 '0' 0;
Array sav1 -> 226 '1' 0;
Array sav2 -> 226 '2' 0;
Array sav3 -> 226 '3' 0;
Array sav4 -> 226 '4' 0;
Array sav5 -> 226 '5' 0;
Array saveNames --> sav0 sav1 sav2 sav3 sav4 sav5;
Array SaveFiles --> 6;
Array savCur -> 226 'c' 'u' 'r' 'r' 'e' 'n' 't' 0;
Global curSave=0;
Global skipUndo=0;
Constant KQUIT_WD = 'androidquit';
-)
after "Definitions.i6t".

[The title card is a figure-name that should be set in the game source. It is the image that will be displayed when play first begins.]

The title card is a figure-name that varies.

Chapter - State-Save System Initialization

[We will replace the normal undo system with our own. This prevents the game from trying to save undo state itself each turn.]

Use undo prevention.

[This sets up the save system. We initialize GLK FileRefs for each of the 6 save files.]

To setup saves: (- SETUP_SAVES(); -).

[The gestalt call, here and elsewhere, asks the GLK layer whether it is in fact an Android device. If not, it does nothing. (This is useful for testing in the built-in Inform 7 interpreter.)]

Include
(-
[SETUP_SAVES res i;
	@gestalt 100 0 res;
	if (res==0) return;
	for (i=0;i<6;i++)
		saveFiles-->i=glk_fileref_create_by_name(fileusage_SavedGame|fileusage_BinaryMode,saveNames-->i,500);
];
-).

[Initialization stuff that needs to be done as soon as the story launches.]

The title placard is some text that varies.

The first when play begins rule (this is the Android launch rule):
	reconfigure the final menu;
	if the background image is not 0, paint image background image onto background;
	setup saves;
	[Check to see if the user is returning to an already extant session. If so, restore her to where she left off.]
	check for current game;
	clear the screen;
	show Android image 0;
	say the title placard;
	page break.

Chapter - Saving State Each Turn

[We stash the current game state into a file each turn. Note that this happens just before a command is read, not just after as with the standard Inform library's implementation of undo. This has the advantage that we always have a current saved state on hand when the program is waiting on the user's command. Thus we can exit much more cleanly, without needing to save a lot of state.

We cycle through the 6 state files initialized earlier, always overwriting the oldest. Thus we always have on hand a current state and five previous states to undo to -- secure on the file system rather than taking space in memory.]

Before reading a command: stash game.

To stash game:
(- ANDROID_SAVE(0); -).

Include
(-
[ANDROID_SAVE done str prevSave res;
	@gestalt 100 0 res;
	if (res==0) return;
	if (skipUndo){
		skipUndo=0;
		return;};
	if (done==2)
		@saveundo res;
	if (done~=2||res==0){
		str=glk_stream_open_file(saveFiles-->curSave,filemode_Write,0);
		@save str res;
		glk_stream_close(str,0);
		prevSave=curSave-1;
		if (prevSave<0)
			prevSave=5;
		if (glk_fileref_does_file_exist(saveFiles-->prevSave))
			@copy 1 sp;
		else
			@copy 0 sp;
		@glk 519 1 res;
		curSave++;
		if (curSave>=6)
			curSave=0;
		if (res==0&&done==1)
			@quit;}
];
-).

Chapter - Restoring State

[Look for current state files. If so, restore to the game in memory.]

To check for current game:
(- RESTORE_CURRENT_GAME(); -)

Include
(-
[RESTORE_CURRENT_GAME fref str res;
	@gestalt 100 0 res;
	if (res==0) return;
	fref=glk_fileref_create_by_name(fileusage_SavedGame|fileusage_BinaryMode,savCur,500);
	res=glk_fileref_does_file_exist(fref);
	if (res==1){
		str=glk_stream_open_file(fref,filemode_Read,0);
		@restore str res;}
	glk_fileref_destroy(fref);
];
-).

Chapter - Android Undo

[We disable the normal undo mechanism to implement our own using our state files. Inform does not let us use the standard "understand as something new" approch to disabling its approach to undo, because an undo is trapped and executed immediately, even before an "After reading a command" rule executes. Therefore we have to do a bit of template hacking, replacing the standard library undo function itself.]

Include (-
[Perform_Undo str res old_pos;
	@gestalt 100 0 res;
	if (res==0) return;
	curSave--;
	if (curSave<0)
		curSave=5;
	old_pos=curSave;
	curSave--;
	if (curSave<0)
		curSave=5;
	if (glk_fileref_does_file_exist(saveFiles-->curSave)){
		str=glk_stream_open_file(saveFiles-->curSave,filemode_Read,0);
		glk_fileref_delete_file(saveFiles-->old_pos);
		@restore str res;}
	else{
	   curSave=old_pos;
		print "[Sorry! Cannot undo any more at this time.]";};
];
-) instead of "Perform Undo" in "OutOfWorld.i6t".

Chapter - Saving a Bookmark

[We have our own approach to saving a bookmark...]

Understand the command "save" as something new.

Saving a bookmark is an action out of world applying to nothing.
Carry out saving a bookmark: bookmark save.
Understand "save" as saving a bookmark.

To bookmark save:
(- SAVE_BOOKMARK(); -).

[When we ask for a FileRef by prompt with usage SavedGame, the Android engine takes it from there. We already have at least 1 and up to 6 extant state files, so it's just a matter of stashing them away as a bookmark along with the current transcript and a few other details. This is all handled automatically at the interpreter level.]

Include (-
[SAVE_BOOKMARK res;
	@gestalt 100 0 res;
	if (res==0) return;
	res=glk_fileref_create_by_prompt(fileUsage_SavedGame|fileusage_BinaryMode,filemode_Write,0);
	skipUndo=1;
];
-).

Chapter - Restoring a Bookmark

[And of course we have our own approach to restoring.]

Understand the command "restore" as something new.

Restoring a bookmark is an action out of world applying to nothing.
Carry out restoring a bookmark: bookmark restore.
Understand "restore" as restoring a bookmark.

To bookmark restore:
(- RESTORE_BOOKMARK(); -).

[Again, the interpreter does the work for us when we request a fileRef by prompt with usage SavedGame. After it moves the state files into place, we do need to call RESTORE_CURRENT_GAME (see above) to actually move them into the game in memory.]

Include
(-
[RESTORE_BOOKMARK res;
	@gestalt 100 0 res;
	if (res==0) return;
	res=glk_fileref_create_by_prompt(fileUsage_SavedGame|fileUsage_BinaryMode,filemode_Read,0);
	skipUndo=1;
	if (res)
		RESTORE_CURRENT_GAME();
];
-).

Chapter - Android Restart

[We need a custom restart implementation as well, to clear out the old state files. Otherwise a restart would just put the user right back where they started, thanks to the "when play begins" rule that tries to restore state.

Since this command accidentally typed could be catastrophic, we make it a bit harder to do by changing it to "restart story". The user will normally select this from the Android menu anyway.]

Understand the command "restart" as something new.
Restarting the Device is an action out of world applying to nothing.
Carry out restarting the Device: Android restart.
Understand "restart story" and "restart" as restarting the Device.

To Android restart:
(- RESTART_CURRENT_GAME(); -).

Include
(-
[RESTART_CURRENT_GAME fref res;
	@gestalt 100 0 res;
	if (res==0) return;
	fref=glk_fileref_create_by_name(fileusage_SavedGame|fileusage_BinaryMode,savCur,0);
	res=glk_fileref_does_file_exist(fref);
	if (res==1){
		glk_fileref_delete_file(fref);}
	glk_fileref_destroy(fref);
	@restart;
];
-).

Chapter - The Page Break Function

[This page break function works in lieu of "clear the screen" to present a chapter or title break but still preserve the earlier transcript -- like a book, in other words.]

To page break:
(- PAGE_BREAK(); -)

Include
(-
[PAGE_BREAK res;
	@gestalt 100 0 res;
	if (res==0) return;
	@glk 514 0 res;
];
-).

Chapter - Setting Text Markers

[Text markers are places in the extant text to which the user can quickly jump using a menu option. They normally correspond with chapter breaks or other appropriate divisions in the story. Without them, navigating through a long story would quickly become a nightmare of button-mashing.

The "set text mark" function should be called from the main game source whenever we want to add a marker. We need to be reasonable about how often we do this; about 10 markers is the limit because of limited slots in the Android menu system. If we go overboard, the resulting behavior is... undefined.

Once set, the functionality for letting the user jump around among text markers is of course handled at the interpreter level.]

To set text mark (marker - text):
(- INSERT_TEXT_MARKER({marker}); -)

Include
(-
[INSERT_TEXT_MARKER text ctext res;
	@gestalt 100 0 res;
	if (res==0) return;
	ctext=Glulx_ChangeAnyToCString(text);
	@copy ctext sp;
	@glk 516 1 res;
];
-).

Chapter - Menu Text Manipulation Utilities

[These functions are used by the menu system to separate the menus from the standard game text.]

[Storing the buffer means to stash the current game text away for little retrieval and begin afresh for now.]

Include (-
[STORE_BUFFER res;
	@gestalt 100 0 res;
     	if (res==0) return;
	@glk 512 0 res;
];
-).

To store buffer: (- STORE_BUFFER(); -).

[This little hack asks the Android terp to see the current last line of extant text as the point from which it should begin printing the next page of text. Kind of esoteric, but it is used by the menu system when the main game text gets stashed away.]

Include (-
[PRINT_FROM_BOTTOM res;
	@gestalt 100 0 res;
	if (res==0) return;
	@glk 518 0 res;
];
-).

To print from bottom: (-PRINT_FROM_BOTTOM(); -).

[Restoring the buffer just throws out whatever menu text is still around and drops us back into the game text where we left off.]

Include (-
[RESTORE_BUFFER res;
	@gestalt 100 0 res;
	if (res==0) return;
	@glk 513 0 res;
];
-).

To restore buffer: (- RESTORE_BUFFER(); -).

[We ask to skip undo when we want the terp to NOT take a normal undo in the next turn. It's our way of preventing an undo position from being stored upon exit from the menu system.]

To skip undo: (- skipUndo=1; -)


Chapter - Android Images

[We don't use blorb files on Android, so we have a somewhat different method of displaying images. They are numbered from 0, and always end with the extension -- regardless of actual format -- .pic. The first image -- 0.pic -- is, if present, always used as the game's cover art, displayed when play begins. We can then include and display other images as we like using the routine below.

The pictures will not be scaled up to fill the screen, but they will be scaled down to fit. Keep that in mind when setting their size.

Note also that you can't do anything remotely fancy with images. You can display in-line within the game's text, and that's it.]

To show Android image (img - a number): (- ANDROID_IMAGE({img}); -).

Include (-
[ANDROID_IMAGE img res;
	@gestalt 100 0 res;
	if (res==0) return;
	print "^";
	glk_image_draw(1,img,0,0);
	print "^";
];
-).

[This will allow us to specify a background that will always appear behind the text in the main text window. For instance, The King of Shreds and Patches uses this function to display a parchment-like background.]

The background image is a number that varies.

To paint image (img - a number) onto background: (- ANDROID_BACKGROUND({img}); -).

Include (-
[ANDROID_BACKGROUND img res;
   @gestalt 100 0 res;
   if (res==0) return;
   @copy img sp;
   @glk 520 1 res;
];
-).

Chapter - Disabled Commands

[These commands are not relevant on Android, so we shut them off entirely.]

Understand the command "script" as something new.
[Understand the command "script on" as something new.]
Understand the command "transcript" as something new.
[Understand the command "transcript on" as something new.]
[Understand the command "script off" as something new.]
[Understand the command "transcript off" as something new.]
Understand the command "score" as something new.
Understand the command "quit" as something new.
Understand the command "q" as something new.
Understand the command "verify" as something new.

Chapter - Ending the game on Android

[We need to modify the game-end menu to work properly on Android. We delete the reference to QUIT entirely.]

Before printing the player's obituary: stash game.

[By all rights we should be able to just provide an amended Table of Final Question Options, but Inform 7 stubbornly refuses to accept it for some reason. So we do it the hard way.]

To reconfigure the final menu: 
choose row 1 in the Table of Final Question Options;
now the final response rule entry is the restart the device rule;
choose row 2 in the Table of Final Question Options;
now the final response rule entry is the restore the device rule;
choose row 4 in the Table of Final Question Options;
blank out the whole row;
choose row 5 in the Table of Final Question Options;
now the final question wording entry is "UNDO";
now the final response rule entry is the undo the device rule.

The restart the device rule translates into I6 as "RESTART_CURRENT_GAME".
The restore the device rule translates into I6 as "RESTORE_BOOKMARK".
The undo the device rule translates into I6 as "Perform_Undo".

Android IF ends here.
