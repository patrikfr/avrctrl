package com.granular8;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

@Command(name = "avrctrl", description = "Control Denon AVR-Xx100 receivers")
public class AvrCtrl implements Callable<Integer> {

  @Option(names = {"-a", "--action"}, description = "on, off", required = true)
  private String action;

  @Override
  public Integer call(){
    return null;
  }

  public static void main(final String[] args) {
    try (
        final Socket echoSocket = new Socket("denon-avr-x2100w.local", 23);
        final PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
    ) {

      final Runnable eventReader = () -> {
        try {
          while (true) {
            System.out.println(in.readLine());
          }
        } catch (Exception e) {
          System.out.println(e);
        }
      };

      new Thread(eventReader).start();

      String userInput;
      System.out.println("INPUT COMMAND:");
      while ((userInput = stdIn.readLine()) != null) {
        final String command = userInput + "\r";
        out.print(command);
        out.flush();
      }

    } catch (
        final IOException e) {
      e.printStackTrace();
    }
  }
}
