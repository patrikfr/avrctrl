package com.granular8;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * Simple CLI for controlling a subset of Denon AVR Xx100 receivers over the network.
 */
@Command(name = "avrctrl",
    description = "Control Denon AVR-Xx100 receivers",
    version = "${bundle:build.version}",
    resourceBundle = "AvrCtrl",
    mixinStandardHelpOptions = true,
    defaultValueProvider = PropertiesDefaultProvider.class)
public class AvrCtrl {

  private static final int READ_TIMEOUT = 2000;
  private static final Set<String> validInputSources = Set.of(
      "PHONO", "CD", "TUNER DVD", "BD", "TV SAT/CBL", "MPLAY", "GAME", "HDRADIO", "NET", "PANDORA",
      "SIRIUSXM", "SPOTIFY", "LASTFM", "FLICKR", "IRADIO", "SERVER", "FAVORITES", "AUX1", "AUX2", "AUX3",
      "AUX4", "AUX5", "AUX6", "AUX7", "BT", "USB/IPOD", "USB", "IPD", "IRP", "FVP");

  @Spec
  CommandSpec spec;

  @Option(names = {"-d", "--debug"}, scope = INHERIT)
  private boolean debug;

  @Option(names = {"-n", "--host-name"}, scope = INHERIT, description = "The AVR network name or IP address (default: ${DEFAULT-VALUE})")
  private String host = "denon-avr-x2100w.local";

  @Option(names = {"-s", "--sources"}, scope = INHERIT, split = "\\|", splitSynopsisLabel = "|", description = "Sources map in format '-s \"Sonos=CD|AppleTV=MPLAY\"'")
  private Map<String, String> sourcesMap;

  @Command(name = "select-input")
  int selectInput(@Parameters(paramLabel = "<input source>") final String input) {
    final String mappedSource = mapSource(input);
    if (!validInputSources.contains(mappedSource)) {
      throw new ParameterException(spec.commandLine(),
          String.format("Invalid value '%s' for action 'select-input': " +
              "<input source> must be mappable to one of %s. Source map: %s", input, String.join(",", validInputSources), sourcesMap));
    }
    return sendCommand("SI" + mappedSource);
  }

  @Command(name = "on", description = "Turn on receiver")
  int on() {
    return sendCommand("PWON");
  }

  @Command(name = "standby", description = "Set receiver to standby")
  int standby() {
    return sendCommand("PWSTANDBY");
  }

  @Command(name = "vol-up", description = "Increase master volume 0.5 step")
  int volUp() {
    return sendCommand("MVUP");
  }

  @Command(name = "vol-down", description = "Decrease master volume 0.5 step")
  int volDown() {
    return sendCommand("MVDOWN");
  }

  @Command(name = "vol", description = "Set master volume")
  int vol(@Parameters(paramLabel = "<master volume level>") int volume) {
    if (volume < 0 || volume > 70) {
      throw new ParameterException(spec.commandLine(),
          String.format("Invalid value '%d' for action 'vol': " +
              "<master volume level> must be between 0 and 70.", volume));
    }
    return sendCommand("MV" + volume);
  }

  private String mapSource(final String source) {
    final Map<String, String> sourceMapIgnoreKeyCase = new TreeMap<>(CASE_INSENSITIVE_ORDER);
    sourceMapIgnoreKeyCase.putAll(sourcesMap);
    return sourceMapIgnoreKeyCase.getOrDefault(source, source);
  }

  private int sendCommand(final String command) {
    try (
        final Socket echoSocket = new Socket(host, 23);
        final PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
    ) {
      if (debug) {
        echoSocket.setSoTimeout(READ_TIMEOUT);
        final Runnable eventReader = () -> {
          try {
            //noinspection InfiniteLoopStatement
            while (true) {
              System.out.println(in.readLine());
            }
          } catch (final SocketTimeoutException ste) {
            //Silently suppress
          } catch (final Exception e) {
            e.printStackTrace(System.out);
          }
        };
        new Thread(eventReader).start();
      }

      out.printf(command + "\r");

      if (debug) {
        Thread.sleep(READ_TIMEOUT);
      }

      return 0;
    } catch (final ConnectException ce) {
      System.out.printf("Failed to connect to %s%n", host);
    } catch (final IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public static void main(final String[] args) {
    int exitCode = new CommandLine(new AvrCtrl()).execute(args);
    System.exit(exitCode);
  }
}