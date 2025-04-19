# droid-hunter-as
An Android based game.

### Background
I created this Android based game as a way to learn about the Android platform. It is a 
variation of my Java based [alien-hunter](https://github.com/ppaternostro/alien-hunter) game. The game was created in 2010 using the Eclipse 
IDE with the aid of the [Android Development Tools (ADT) for Eclipse](https://marketplace.eclipse.org/content/android-development-tools-eclipse) 
plugin. The last Eclipse version supported by the ADT plugin is **2018-09 (4.9)**. Later versions
of Eclipse typically used the [Andmore: Development Tools for Android™](https://marketplace.eclipse.org/content/andmore-development-tools-android%E2%84%A2) 
plugin to develop Android applications. However, the last supported Eclipse version for the Andmore plugin is **2021-03 (4.19)**.

Google officially dropped support for Eclipse ADT in 2015, making [Android Studio](https://developer.android.com/studio) the only 
officially supported IDE for Android development. I subsequently migrated this project under the
Android Studio IDE. Luckily, Android Studio provides a specialized wizard that imports an Eclipse
ADT based Android project to the new directory structure required by Android Studio. Note, this project's
name has the suffix **-as** to denote it uses the Android Studio folder structure. You should
also be able to use the [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/other.html) for Android development as it provides 
all the basic features for JVM and Android development. Android Studio was built on JetBrains' 
IntelliJ IDEA software, however, it only provides support for Android development.

### Building
You are not required to download either Android Studio or the IntelliJ IDEA Community Edition IDEs
to build this game. After this project has been cloned locally, run the below command from a terminal 
window in the application's root folder to build the application.

> gradlew build (use **./gradlew** for Unix/Linux based OSes)

After a successful build, navigate to the below folder structure under the project's root folder.

> app/build/outputs/apk/debug

An **app-debug.apk** file should be available. An APK (Android Package Kit) file is the file format 
for applications used on the Android operating system. An APK file contains all the data an 
application needs, including all of the program's code, assets and resources. Android applications
must be signed with a certificate before you can deploy your application to a device. The debug build 
is automatically signed with a debug key provided by the SDK tools which is the reason for using the
**app-debug.apk** version of the file.

You will need to [sideload and install](https://www.howtogeek.com/313433/how-to-sideload-apps-on-android/)
the **app-debug.apk** file to your mobile device.

### Caveat Emptor
This simple game is functional and pretty stable. The use of the Java **Thread** class does 
occasionally cause some anomalous issues in **multilevel** mode. At some point I need to update the code 
to address the anomalies. At some point. :-)

### Game Play (Screen Recording Video)
[Droid Hunter Screen Recording](https://github.com/user-attachments/assets/ebe623f0-2218-4c5b-8f05-a83339aa48f3)
