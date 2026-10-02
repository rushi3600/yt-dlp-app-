DOWNLY ANDROID STARTER PROJECT
================================

What this version does:
- Minimal dark Android interface.
- Accepts a YouTube HTTPS URL.
- Lets user choose MP3 or MP4.
- Sends yt-dlp arguments to Termux RUN_COMMAND in the background.
- Saves output to /storage/emulated/0/Download/Downly/.

Important limitations:
- This is a starter project, not a tested release APK.
- It does NOT show live percentage progress or download history yet.
- Termux must be installed, compatible, and configured.
- The app uses Termux's documented RUN_COMMAND integration.
- Only download content you have permission to save and follow platform terms.

SETUP
1. Install Android Studio on your laptop.
2. Extract this ZIP.
3. Android Studio > Open > select the extracted DownlyAndroid folder.
4. Let Gradle sync. If Android Studio asks to install SDK 36, accept.
5. Connect Android phone with USB debugging enabled.
6. Click Run to install the app.

TERMUX SETUP
1. Open Termux and run:
   pkg update
   pkg install python ffmpeg
   python -m pip install -U yt-dlp
   termux-setup-storage
2. In Termux, run:
   mkdir -p ~/.termux
   nano ~/.termux/termux.properties
   Add this line:
   allow-external-apps = true
   Save and restart Termux.
3. Open Android Settings > Apps > Downly > Permissions.
   Grant "Run commands in Termux environment" / RUN_COMMAND if shown.
4. Ensure Termux has storage access. Create the folder if needed:
   mkdir -p ~/storage/shared/Download/Downly
5. Try a permitted test link from Downly.

NOTES
- If your Termux installation is from a different source or has a different package
  identity, the integration may not work. Install Termux and its add-ons from the
  same trusted source; do not mix signatures/sources.
- Some Android versions/OEMs may require extra battery/background settings.
- The app does not embed yt-dlp; Termux executes it.
- The output folder must be writable by Termux.
