# AndroidIF

--This is an updated branch of Simon Christiansen's AndroidIF app. Updated to work with latest android builds.

How to turn your Inform 7 game into an Android app

1) Install Android Studio if you do not have it already.

2) If you are not familiar with Android development, you should start by working through a simple beginner's tutorial. 

3) Deploy the demo game and check that everything works. I recommend running a simulation of the game on an emulated or connected device just to check functionality. 

4) Now to import your own game, you will need to use the Android IF extension to compile a story file that can interface with the Android framework.

a) From Inform 7, install the "Android IF" and "Android Typography" extensions from the "Inform Extensions" folder in this project. 

Some notes: -while some extensions will work fine with AndroidIF, due to the nature of android keyboards, using Screen Effects "Pause the game" effect will -not- function.
Images -do- work, but you wil have to use the .pic system (refer to C and D), and you cannot have two images displaying at the same time -- this is a known issue that I will attempt to resolve, but no promises, as it exists in the original build as well!
	
b) Make a new copy of your game and include the Android IF extension. Note that this will disable save/restore functionality in the Inform window. Don't worry, it will work on the Android device.
	
c) If you want a background texture, add the statement "The background image is #n" to your game code, where "#n" can be any integer. Place your background texture in the app/assets folder and rename it "#n.pic". The actual format of the texture is not important, but it must be renamed to use the ".pic" extension.
	
d) If your game includes pictures, the Inform display statements must be replaced with "Show Android image #n", where "#n" is the number of the picture. The name of the picture cannot be anything else than a number. Copy your pictures into the app/assets fold and rename them "#n.pic", with the values of "#n" corresponding to the values used in the source. The actual format of the pictures is not important, but they must be renamed to use the ".pic" extension.
	
e) Compile your game to a raw ".ulx" file by selecting Glulxe format and unchecking the "Bind up into a Blorb file on release" checkbox. Copy the file into the app/assets folder and rename it "story.ulx".
	
f) Check the "story.ini" file in the "Inform Source" folder for an example.
	
5) **I'm leaving Simon's original instructions here, but I highly recommend using Android Studio to refactor-rename the package and application names instead of manually renaming them!) 
(if not refactor-renaming) - Open the project in Android Studio. In "build.gradle (Module:app)", change the applicationId "thedemitrius.androidif" to your desired package name (Remember, no capital letters or spaces). Do the same with the package name in app/manifests/AndroidManifest.xml. Finally, rename the app/java/thedemitrius.androidif folder to match. Clean the project, synchronize and rebuild.

6) Change the display name of the game in app/res/values/strings.xml.

7) Replace the icon.png files in the app/res/drawable folder.

**8) Either replace or remove "song.mp3", in the app/res/raw folder. (**As part of my implementation, I have commented out song settings at this time, as these were not functioning in the latest build.)

9) In Init.java, customize the menu title and entries to fit your needs. (EG Line 56, change to your story name!)

10) Build and deploy your game.

Known issues
- The app cannot draw more than one picture on the same screen. Ensure that there is always at least one screen's worth of text between your pictures.
- In-line font changes will cause text to go missing on some devices. Stick to one font per paragraph.
