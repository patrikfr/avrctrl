# avrctrl

CLI for controlling Denon AVR-Xx100 receivers

## Building

Using a Java 11 JDK:
`mvn clean install`

## Running

`java -jar target/avr-ctrl-0.1.jar --help`

## Building a native image using GraalVM

For faster startup and stand-alone execution, a native image can be built
using [GraalVM](https://www.graalvm.org/reference-manual/native-image/). The Maven build sets up what is needed to build
a native image. For this to work, GraalVM and the Native Image tool must be installed. First, build the project
using `mvn clean install` — preferably using the GraalVM JDK to avoid version mismatch issues — then run:  
`native-image --no-server --no-fallback -jar target/avr-ctrl-0.1.jar avrctrl`

The resulting executable `avrctrl` can be run from the command line as:  
`./avrctrl --help`
Add it to the PATH, and you are good to go!

If building using a JDK containing then the `native-image` tool, there is also
a [maven plugin](https://www.graalvm.org/reference-manual/native-image/NativeImageMavenPlugin/) that can help.
