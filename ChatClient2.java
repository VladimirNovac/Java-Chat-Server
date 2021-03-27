package ie.gmit.dip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient2 {

    public static void main(String[] args) {
        serverStart(args);

    }

    private static void serverStart(String[] args) {
        Socket socket = null;
        ReceiveThread receiveThread;
        SendThread sendThread;
        BufferedReader in = null;
        PrintWriter out = null;
        Scanner scanner = new Scanner(System.in);
        String host;
        int portNumber;

        if (args.length == 2) {
            host = String.valueOf(args[0]);
            portNumber = Integer.parseInt(args[1]);
            System.out.println("The Chat client is connected to " + host + " on port: " + portNumber);
        } else {
            host = "localHost";
            portNumber = 59001;
            System.out.println("No IP address or Port number provided");
            System.out.println("The Chat client is running on localhost with default port: 59001");
        }


        try {
            socket = new Socket(host, portNumber);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);


            while (true) {
                String message = in.readLine();
                if (message.startsWith("Submit Name")) {
                    System.out.println("Enter your user name for the chat session:");
                    System.out.println("No duplicate user names allowed");
                    out.println(scanner.nextLine());
                }
                if (message.startsWith("NAME ACCEPTED")) {
                    System.out.println("You have joined the chat server on port: " + socket.getPort());
                    System.out.println("You can begin a conversation by typing in the console");
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Could not connect to host...");
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Could not connect to host...");
            System.exit(0);
            //e.printStackTrace();
        }

        sendThread = new SendThread(out);
        receiveThread = new ReceiveThread(socket, in);
        sendThread.start();
        receiveThread.start();
    }


    public static class SendThread extends Thread {
        private PrintWriter out;
        private Scanner scanner;

        SendThread(PrintWriter out) {
            this.out = out;
            scanner = new Scanner(System.in);
        }

        @Override
        public void run() {
            while (true) {
                String message = scanner.nextLine();
                out.println(message);
            }
        }
    }

    public static class ReceiveThread extends Thread {
        private BufferedReader in;
        private Socket socket;

        ReceiveThread(Socket socket, BufferedReader in) {
            this.socket = socket;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    if (message.startsWith("MESSAGE")) {
                        System.out.println(message.substring(8));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Socket already closed");
                }
                System.out.println("the connection to the server on port: " + socket.getPort() + " has finished");
                System.exit(0);
            }
        }
    }

}