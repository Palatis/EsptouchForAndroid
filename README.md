# ESPTouch library for Java

This is a refactored version of [EsptouchForAndroid](https://github.com/EspressifApp/EsptouchForAndroid) from [EspressifApp](https://github.com/EspressifApp/).

Consult the [upstream README.md](README.upstream.md) for more information.

The major difference are:

1. stripped all Android specific codes from the protocol library, so it can be used on other platforms.
2. refactored out unnecessary interfaces, thus making the library smaller (and possibility a tiny bit faster)
3. released as a maven-repository for easier integration with [Android Studio](http://developer.android.com/sdk/) (or [Eclipse](https://eclipse.org/) / [IntelliJ IDEA](https://www.jetbrains.com/idea/))
4. with an android specific EspsyncAsyncTask for easier integration

## Examples

see [the app](https://github.com/Palatis/EsptouchForAndroid/tree/master/app) for the example.

## Versions

* esptouch-java
    * **v0.3.4.3-palatis-14-gc18215a** 
        * minor tweak to the underlying EsptouchTask, nothing big deal.
        * reorganize folder structures
* esptouch-android
    * **v0.3.4.3-palatis-14-gc18215a** 
        * first release

## Using with Gradle

Add the repository URL to the `repositories` section of your project's `build.gradle`:
```
...
allprojects {
    repositories {
        jcenter()
        ...
        maven { url "https://github.com/Palatis/EsptouchForAndroid/raw/master/maven-repository" }
    }
}
...
```
and depends on it in the `dependencies` of your module's `build.gradle`:
```
dependencies {
    ...
    compile 'tw.idv.palatis:esptouch-android:+' // for android integration
    compile 'tw.idv.palatis:esptouch-java:+' // for the jar
    ...
}
```

## License

This library is using a semi-open-source license, it is free - as in speech - as long as you're working on an [ESP8266](http://espressif.com/en/products/hardware/esp8266ex/overview) (may grant permission to [ESP32](http://espressif.com/en/products/hardware/esp32/overview) in the future). So if you're trying to use it with other hardware, **YOU ARE VIOLATING THE PERMISSION, AND ARE AT YOUR OWN RISK.**

- [ESPRESSIF MIT LICENSE V1](ESPRESSIF MIT LICENSE V1.md)