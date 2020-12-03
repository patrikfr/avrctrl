package com.granular8;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

@Command(name = "avrctrl", description = "Control Denon AVR-Xx100 receivers")
public class AvrCtrl implements Callable<Integer> {

  private static final String AVR_NETWORK_NAME = "denon-avr-x2100w.local";
  private static final int AVR_NETWORK_PORT = 23;
  public static final int READ_TIMEOUT = 2000;

  @CommandLine.Parameters(index = "0", description = "The action to perform.")
  private String action;

  @CommandLine.Parameters(index = "1", description = "Action input.", defaultValue = "")
  private String actionParameter;

  @Option(names = {"-d", "--debug"})
  private boolean debug;

  //avrctrl select-input MPLAY
  //avrctrl on
  //avrctrl standby
  //avrctrl vol-up
  //avrctrl vol-down
  //avrctrl vol <int>
  //-d print output
  @Override
  public Integer call() {
    try (
        final Socket echoSocket = new Socket(AVR_NETWORK_NAME, AVR_NETWORK_PORT);
        final PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
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

      final String command = action + actionParameter + "\r";
      out.printf(command);

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

  //COMMANDS:
  //SI {MPLAY, CD, BD}
  //PWON
  //PWSTANDBY
  //MV <int>
  //MVUP
  //MVDOWN

  //avrctrl select-input MPLAY
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
