# avrctrl

CLI for controlling Denon AVR-Xx100 receivers

## Building

Using a Java 11 JDK:
`mvn clean install`

## Running

`java -jar target/avr-ctrl-0.11.jar --help`

For a more native feeling, create an alias, and/or build a native image (see _Building a Native Image Using GraalVM_
below).

## Usage and Settings

### AVR Network Name
The default network name used for the receiver is `denon-avr-x2100w.local` this can be overridden with a new name or IP
address using option `-n` or `--host-name`.

### Input Sources

The default sources are (not all of these will be available on your receiver):  
PHONO, CD, TUNER, DVD, BD, TV, SAT/CBL, MPLAY, GAME, HDRADIO, NET, PANDORA, SIRIUSXM, SPOTIFY, LASTFM, FLICKR, IRADIO,
SERVER, FAVORITES, AUX1, AUX2, AUX3, AUX4, AUX5, AUX6, AUX7, BT, USB/IPOD, USB, IPD, IRP, FVP.

For a more pleasant user experience, the sources can be mapped to better fit what is actually setup in your system using
option `-s` or `--sources`. E.g.: `avrctrl select-input Sonos -s "Sonos=CD|AppleTV=MPLAY"` The mapped source,
i.e. `Sonos` and `AppleTV` in this example, are case-insensitive, so both `Sonos` and `sonos` will work. To avoid having
to supply it each time `avrctrl` is invoked, see next section on how to store the mappings in a separate user settings
file.

### User Default Settings
It's possible to store default user settings in a properties file.

1. Create file `.avrctrl.properties` in you home directory
2. Configure default settings as key=value pairs in this file using the long option name as key

Example file:

```text
host-name=denon-avr-x2100w.local
sources=Sonos=CD|AppleTV=MPLAY|Cable=SAT/CBL|Chromecast=DVD|Blu-ray=BD|Switch=GAME|Spotify=SPOTIFY
```

With the example above you can now invoke `avrctrl` as: `avrctrl input-source sonos`

## Building a Native Image Using GraalVM
For faster startup and stand-alone execution, a native image can be built
using [GraalVM](https://www.graalvm.org/reference-manual/native-image/). The Maven build sets up what is needed to build
a native image. For this to work, GraalVM and the Native Image tool must be installed. First, build the project
using `mvn clean install` — preferably using the GraalVM JDK to avoid version mismatch issues — then run:  
`native-image --no-server --no-fallback -jar target/avr-ctrl-0.11.jar avrctrl`

The resulting executable `avrctrl` can be run from the command line as:  
`./avrctrl --help`
Add it to the PATH, and you are good to go!

If building using a JDK containing then the `native-image` tool, there is also
a [maven plugin](https://www.graalvm.org/reference-manual/native-image/NativeImageMavenPlugin/) that can help.
