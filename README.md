#EsCam
EsCam is a document enhacement application. This application is still under developement, and it takes part of the final engineering project of Jose Luís Adelantado (Computer Science), supervised by [Joan Pastor](https://github.com/joapaspe) and María José Castro.


## Requeriments
- You will need [Android sdk](http://developer.android.com/sdk/index.html?utm_source=weibolife) to generate the application . Since the repositori is a eclipse project himself, it is strongly recommended to use it to build the application.
- Android 4.0 (API 14)

##Install
If you use eclipse, you can use Eclipse import tools:

`File->Import->Git->Projects from Git`

Install the correct api:

`Android SDK Manager 'Android 4.0 (API 14)'`

Configure the android device (Android Virtural or Mobile phone), and run the application within Eclipse.

## Install OpenCV on the Virtual Device Manager

**Update: EsCam works now with installing OpenCV on the device as it is provided within the application.**

You can follow the steps in this (tutorial)[http://docs.opencv.org/doc/tutorials/introduction/android_binary_package/O4A_SDK.html]

But basically, you can download the apk of the tool and install it on the virtual device.

`<Android SDK path>/platform-tools/adb install <OpenCV4Android SDK path>/apk/OpenCV_2.4.8_Manager_2.16_armv7a-neon.apk`

