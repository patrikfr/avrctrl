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
import java.util.Set;

import static picocli.CommandLine.ScopeType.INHERIT;

@Command(name = "avrctrl", description = "Control Denon AVR-Xx100 receivers")
public class AvrCtrl {
  private static final String AVR_NETWORK_NAME = "denon-avr-x2100w.local";
  private static final int AVR_NETWORK_PORT = 23;
  private static final int READ_TIMEOUT = 2000;
  private static final Set<String> validInputSources = Set.of("MPLAY", "CD", "BD");

  @Spec
  CommandSpec spec;

  @Option(names = {"-d", "--debug"}, scope = INHERIT)
  private boolean debug;

  private int sendCommand(final String command) {
    try (
        final Socket echoSocket = new Socket(AVR_NETWORK_NAME, AVR_NETWORK_PORT);
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
      System.out.printf("Failed to connect to %s%n", AVR_NETWORK_NAME);
    } catch (final IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return -1;
  }

  @Command(name = "select-input")
  int selectInput(@Parameters(paramLabel = "<input source>") final String input) {
    if (!validInputSources.contains(input)) {
      throw new ParameterException(spec.commandLine(),
          String.format("Invalid value '%s' for action 'select-input': " +
              "<input source> must be one of %s.", input, String.join(",", validInputSources)));
    }
    return sendCommand("SI" + input);
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

  //avrctrl select-input {MPLAY, CD}
  //avrctrl on
  //avrctrl standby
  //avrctrl vol-up
  //avrctrl vol-down
  //avrctrl vol <int>
  //-d print output

  public static void main(final String[] args) {
    int exitCode = new CommandLine(new AvrCtrl()).execute(args);
    System.exit(exitCode);
  }
}