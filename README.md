# ESPTouch library for Java

This is a refactored version of [EsptouchForAndroid](https://github.com/EspressifApp/EsptouchForAndroid) from [EspressifApp](https://github.com/EspressifApp/).

Consult the [upstream README.md](README.upstream.md) for more information.

The major difference are:
1. stripped all Android specific codes from the protocol library, so it can be used on other platforms.
2. refactored out unnecessary interfaces, thus making the library smaller (and possibility a tiny bit faster)
3. released as a maven-repository for easier integration with [Android Studio](http://developer.android.com/sdk/) (or [Eclipse](https://eclipse.org/) / [IntelliJ IDEA](https://www.jetbrains.com/idea/))

# Using with Gradle
add the repository URL to the `repositories` section of your project's `build.gradle`:
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
    compile 'tw.idv.palatis:esptouch-java:+'
    ...
}
```

# License
- [ESPRESSIF MIT LICENSE V1](ESPRESSIF MIT LICENSE V1.md)