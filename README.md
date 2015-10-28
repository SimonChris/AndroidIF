# AndroidIF
How to turn your Inform 7 game into an Android app

1) Install Android Studio, if you do not have it already.

2) If you are not familiar with Android development, you should start by working through a simple beginners tutorial. Once you can get a basic "Hello World" app to run on your device, you are ready to proceed.

3) Deploy the demo game and check that everything works.

4) First, you will need to use the Android IF extension to compile a story file that can interface with the Android framework.

	a) From Inform 7, Install the "Android IF" and "Android Typography" extensions from the "Inform Extensions" folder in this project.
	
	b) Make a new copy of your game and include the Android IF extension. Note that this will disable save/restore functionality in the Inform window. Don't worry, it will work on the Android device.
	
	c) If you want a background texture, add the statement "The background image is #n" to your game code, where "#n" can be any integer. Place your background texture in the app/assets folder and rename it "#n.pic". The actual format of the texture is not important, but it must be renamed to use the ".pic" extension.
	
	c) If your game includes pictures, the Inform display statements must be replaced with "Show Android image #n", where "#n" is the number of the picture. The name of the picture cannot be anything else than a number. Copy your pictures into the app/assets fold and rename them "#n.pic", with the values of "#n" corresponding to the values used in the source. The actual format of the pictures is not important, but they must be renamed to use the ".pic" extension.
	
	d) Compile your game to a raw ".ulx" file by selecting Glulxe format and unchecking the "Bind up into a Blorb file on release" checkbox. Copy the file into the app/assets folder and rename it "story.ulx".
	
	e) Check the "story.ini" file in the "Inform Source" folder for an example.
	
5) Open the project in Android Studio. In "build.gradle (Module:app)", change the applicationId "sichris.androidDemo" to your desired package name. Do the same with the package name in app/manifests/AndroidManifest.xml. Finally, rename the app/java/sichris.androidDemo folder to match. Clean the project, synchronize and rebuild.

6) Change the display name of the game in app/res/values/strings.xml.

7) Replace the icon.png files in the app/res/drawable folder.

8) Either replace or remove "song.mp3", in the app/res/raw folder.

9) In Init.java, customize the menu title and entries to fit your needs.

10) Build and deploy your game.
