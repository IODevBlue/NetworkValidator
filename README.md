NetworkValidator
================
A lightweight module written in Kotlin for monitoring network state and airplane mode on native android.

[<img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/IODevBlue/NetworkValidator?label=Current Version&color=2CCCE4&style=for-the-badge&labelColor=0109B6">](https://github.com/IODevBlue/NetworkValidator/releases) <img alt="Repository Size" src="https://img.shields.io/github/repo-size/IODevBlue/NetworkValidator?color=2CCCE4&style=for-the-badge&labelColor=0109B6"> [<img alt="License" src="https://img.shields.io/github/license/IODevBlue/NetworkValidator?color=2CCCE4&style=for-the-badge&labelColor=0109B6">](http://www.apache.org/licenses/LICENSE-2.0) [<img alt="GitHub Repository stars" src="https://img.shields.io/github/stars/IODevBlue/NetworkValidator?color=2CCCE4&style=for-the-badge&labelColor=0109B6">](https://github.com/IODevBlue/NetworkValidator/stargazers)
<img alt="GitHub watchers" src="https://img.shields.io/github/watchers/IODevBlue/NetworkValidator?label=Repository Watchers&color=2CCCE4&style=for-the-badge&labelColor=0109B6"> [<img alt="Gradle version" src="https://img.shields.io/static/v1?label=Gradle version&message=7.5.1&color=2CCCE4&style=for-the-badge&labelColor=0109B6">](https://docs.gradle.org/7.5.1/release-notes) [<img alt="Kotlin version" src="https://img.shields.io/static/v1?label=Kotlin version&message=1.7.10&color=2CCCE4&style=for-the-badge&labelColor=0109B6">](https://KOTLINlang.org/docs/whatsnew1720)

Uses
----
NetworkValidator provides real-time network availability and airplane mode monitoring.

<p align="left"><img src="/art/offline.png" alt="Offline"></p> <p align="left"><img src="/art/airplane-mode.png" alt="Airplane moMde"></p> <p align="left"><img src="/art/mobile-data.png" alt="Mobile Data"></p>

Installation
------------
**current-version: 1.0.0-SNAPSHOT**

1. Grab a JAR artefact from the Maven Central Repository:
- On Gradle
```GROOVY
implementation 'io.github.iodevblue:networkvalidator:current-version'
```
- On Apache Maven
```XML
<dependency>
  <groudId> io.github.iodevblue </groudId>
  <artifactId> parallaxnavigationdrawer </artifactId>
  <version> current-version </version>
</dependency>
```
If it is a snapshot version, add the snapshot Maven Nexus OSS repository:
```GROOVY
maven {   
  url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
}
```
Then retrieve a copy:
```GROOVY
implementation 'io.github.iodevblue:parallaxnavigationdrawer:current-version'
```

2. Grab a JAR or AAR artifact from the [release](https://github.com/IODevBlue/ParallaxNavigationDrawer/releases) section.
- Place it in `libs` folder in your project module and install in your project.
```GROOVY
implementation fileTree(dir:' libs', include:'*jar')
```

Usage
-----
**NOTE:** Minimum supported Android SDK version = 23

Create a `NetworkValidator` instance using a `Context`:
```KOTLIN
val networkValidator = NetworkValidator(context)
```

Validate if there is internet connection on a device:
```KOTLIN
val isOnline = networkValidator.isOnline()

if(isOnline) {
    retrieveFromRemoteRepo()
} else {
    retrieveFromLocalCache()
}
```

Validate if there is internet connection through Wifi:
```KOTLIN
val isWifiAvail = networkValidator.isWifiAvailable()

if(isWifiAvail) {
    startPackageDownloading()
} else {
    informUser()
}
```

Validate if there is internet connection through mobile data:
```KOTLIN
val isMobileDataOn = networkValidator.isCellularAvailable()

if(isMobileDataOn) {
    connectToServer()
} else {
    disconnectFromServer()
}
```
<p align="center"><img src="/art/mobile-data.gif" alt="Mobile Data (Animated)"></p>

Validate if airplane mode is turned on:
```KOTLIN
val isOnAirplane = networkValidator.isAirplaneModeActive()

if(isOnAirplane) { 
    disconnectFromServer()
} else {
    doSomethingRemotely()
}
```
<p align="center"><img src="/art/airplane-mode.gif" alt="Airplane Mode (Animated)"></p>


To listen for changes in internet availability on the device (both Wifi and Mobile Data), set an `OnNetworkChangedListener` instance:
```KOTLIN
val networkValidator = NetworkValidator(context)

networkValidator.onNetworkChangedListener = object: NetworkValidator.OnNetworkChangedListener {
  override fun onNetworkChanged(isOnline: Boolean, network: Network) {
    if(isOnline) {
        contactRemoteServer()
    }
  }
}
```

Or apply an `OnNetworkChangedListener` using a Kotlin receiver function syntax:
```KOTLIN
networkValidator.setOnNetworkStateChangedListener { isOnline, _ -> 
  if(isOnline) {
    contactRemoteServer()
  }
}
```

**NOTE:** The `onNetworkChanged()` callback and the `setOnNetworkStateChangedListener {...}` receiver function all execute in a background `Thread`.
That means, User Interface functions that respond to network changes **MUST** be executed on a UI `Thread` like so on an Android `Activity`:
```KOTLIN
networkValidator.onNetworkChangedListener = object: NetworkValidator.OnNetworkChangedListener {
  override fun onNetworkChanged(isOnline: Boolean, network: Network) {
    runOnUiThread {
      if(isOnline) {
        contactRemoteServer()
      }
    }
  }
}
```

To listen for airplane mode changes, set an `OnAirplaneModeSwitchListener` instance:
```KOTLIN
networkValidator.onAirplaneModeSwitchListener = object: NetworkValidator.OnAirplaneModeSwitchListener {
  override fun onChanged(turnedOn: Boolean) {
      if(turnedOn) {
          pauseRemoteConnection()
      }
  }
}
```

Or apply an `OnAirplaneModeSwitchListener` using a Kotlin receiver function syntax:
```KOTLIN
networkValidator.setOnAirplaneModeSwitchListener { isOnline, _ -> 
  if(isOnline) {
    contactRemoteServer()
  }
}
```

Also, the `onChanged()` callback and the `setOnAirplaneModeSwitchListener {...}` receiver function all execute in a background `Thread` therefore, 
User Interface functions that respond to airplane mode changes **MUST** be executed on a UI `Thread` the same way as the `OnNetworkChangedListener`.

**NOTE:** `NetworkValidator` uses an internal `BroadcastReceiver` that handles receiving airplane mode change events and it is automatically registered when an `OnAirplaneModeSwitchListener` instance is defined. 
It is unregistered and nullified when the `OnAirplaneModeSwitchListener` is nullified.

(see library code for more details)

However this internal `BroadcastReceiver` needs to be unregistered when a `NetworkValidator` is not in use to avoid memory leaks.

In an Android `Activity`, it can be done like so:
```KOTLIN
override fun onResume() { 
  super.onResume()
  networkValidator.registerAirplaneModeSwitchListener
}

override fun onPause() { 
  super.onPause()
  networkValidator.unregisterAirplaneModeSwitchListener
}
```

Kotlin extension receiver functions (extensions on a `Context`) are also included to assist in prompt setup:

`networkValidator{}` returns a valid NetworkValidator instance:
```KOTLIN
networkValidator { 
  setOnNetworkStateChangedListener { b, _ ->
    runOnUiThread {
      tv.text = resources.getString(R.string.detect_network)
      showProgress()
      updateNetworkState(b)
    }
  }
}
```

`listenForNetwork{}` internally creates a NetworkValidator and registers an `OnNetworkChangedListener` that begins listening immediately:
```KOTLIN
listenForNetwork { b, network ->
  if(b) {
    updateNetworkState(b)
    reInitializeRemoteConnection()
  } else {
    invalidateRemoteConnection()
    informUser()
  }
}
```

`listenForAirplaneModeChanges{}` returns a NetworkValidator and registers an `OnAirplaneModeSwitchListener` that begins listening immediately:
```KOTLIN
val networkValidator = listenForAirplaneModeChanges {
    if(it) {
        cancelRemoteConnection()
    } else {
      reInitializeRemoteConnection()
    }
}
```
The returned `NetworkValidator` instance can then be used to `register()` and `unregister()` the internal `BroadcastReceiver`.


Java Interoperability
---------------------
This module is completely interoperable with Java.

**NOTE:** Extension functions are not applicable in Java.

Create a `NetworkValidator` instance:
```JAVA
NetworkValidator networkValidator = new NetworkValidator(this);
```

Validate if there is internet connection on a device:
```JAVA
boolean isOnline = networkValidator.isOnline();

if(isOnline) {
    retrieveFromRemoteRepo();
} else {
    retrieveFromLocalCache();
}
```

Validate if there is internet connection through Wifi:
```JAVA
boolean isWifiAvail = networkValidator.isWifiAvailable();

if(isWifiAvail) {
    startPackageDownloading();
} else {
    informUser();
}
```

Validate if there is internet connection through mobile data:
```JAVA
boolean isMobileDataOn = networkValidator.isCellularAvailable();

if(isMobileDataOn) {
    connectToServer();
} else {
    disconnectFromServer();
}
```

Validate if airplane mode is turned on:
```JAVA
boolean isOnAirplane = networkValidator.isAirplaneModeActive();

if(isOnAirplane) { 
    disconnectFromServer();
} else {
    doSomethingRemotely();
}
```

To listen for changes in internet availability on the device (both Wifi and Mobile Data), set an `OnNetworkChangedListener` instance:
```JAVA
NetworkValidator networkValidator = new NetworkValidator(this);

networkValidator.setOnNetworkChangedListener((isOnline, network) -> { 
  if(isOnline) {
      contactRemoteServer();
  }
}
```

To listen for airplane mode changes, set an `OnAirplaneModeSwitchListener` instance:
```JAVA
networkValidator.setOnAirplaneModeSwitchListener(turnedOn -> {
  if(turnedOn) {
	pauseRemoteConnection();
  }
}
```


Configurations:
|Variable |Default |Use |
|:---|:---:|:---:|
|`onNetworkChangedListener` |null |Listener for Network state changes. |
|`onAirplaneModeSwitchListener` |null |Listener for airplane mode changes. |

|Method |Returns |Use |
|:---|:---:|:---:|
|`isOnline()` |Boolean |Validates if there is internet connection. |
|`isWifiAvailable()` |Boolean | Validates if internet connection is available through Wifi. |
|`isCellularAvailable()` |Boolean |Validates if internet connection is available through mobile data. |
|`isAirplaneModeActive()` |Boolean |Validates is airplane mode is active. |
|`unregisterAirplaneModeSwitchListener()` |Unit |Unregisters the `onAirplaneModeSwitchListener` from listening to airplane mode events. |
|`registerAirplaneModeSwitchListener()` |Unit |Registers the `onAirplaneModeSwitchListener` to start listening for airplane mode events.  |
|`setOnNetworkStateChangedListener(execute: OnNetworkChangedListener.(Boolean, Network) -> Unit)` |Unit |Sets a network change listener. |
|`setOnAirplaneModeSwitchListener(execute: OnAirplaneModeSwitchListener.(Boolean) -> Unit)` |Unit |Sets an airplane mode switch listener. |

Contributions
-------------
Contributors are welcome!

***NOTE:*** This repository is split into two branches:
- [main](https://github.com/IODevBlue/NetworkValidator/tree/main) branch
- [development](https://github.com/IODevBlue/NetworkValidator/tree/development) branch

All developing implementations and proposed changes are pushed to the [development](https://github.com/IODevBlue/NetworkValidator/tree/development) branch and finalized updates are pushed to the [main](https://github.com/IODevBlue/NetworkValidator/tree/main) branch.

To note if current developments are being made, there would be more commits in the [development](https://github.com/IODevBlue/NetworkValidator/tree/development) branch than in the [main](https://github.com/IODevBlue/NetworkValidator/tree/main) branch.

Check the [Contributing](https://github.com/IODevBlue/NetworkValidator/blob/development/CONTRIBUTING.md) for more information.


Changelog
---------
* **1.0.0**
    * Initial release

More version history can be gotten from the [Change log](https://github.com/IODevBlue/NetworkValidator/blob/main/CHANGELOG.md) file.


License
=======
```
    Copyright 2023 IO DevBlue

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```