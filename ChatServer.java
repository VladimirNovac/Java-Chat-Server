package ie.gmit.dip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ChatServer {

    private static final Set<String> names = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        //System.out.println("The chat server is running...");
        final MainThread thread = new MainThread(args);
        thread.start();

        try (BufferedReader serverIn = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                final String line = serverIn.readLine();
                if (line.equalsIgnoreCase("\\q")) {
                    thread.shutdown();
                    break;
                } else if (line.startsWith("\\q ")) {
                    final String name = line.substring(3);
                    thread.shutdown(name);
                } else {
                    thread.broadcast("MESSAGE Server: " + line);
                    System.out.println("Server: " + line);
                }
            }
        }
    }

    private static class MainThread extends Thread {
        final ExecutorService pool;
        final Set<Handler> handlers = new HashSet<>();
        ServerSocket listener;
        String[] args;
        int portNumber;

        MainThread(String[] args) {
            this.args = args;
            this.pool = Executors.newFixedThreadPool(20);
        }



        @Override
        public void run() {
            if(args.length != 1){
                portNumber = 59001;
                System.out.println("No port number provided");
                System.out.println("The chat server is running on default port: " + portNumber);

            } else {
                portNumber = Integer.parseInt(args[0]);
                System.out.println("The chat server is running on port: " + portNumber);
            }
            try {
                listener = new ServerSocket(portNumber);
                System.out.println("Server is ready accept connections");
                while (true) {
                    final Handler handler = new Handler(listener.accept());
                    handlers.add(handler);
                    pool.execute(handler);
                }
            } catch (IOException e) {
                if (listener != null && !listener.isClosed()) {
                    try {
                        listener.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        void shutdown() throws InterruptedException, IOException {
            for (Handler handler : handlers) {
                handler.shutdown();
            }
            if (!pool.awaitTermination(5L, TimeUnit.SECONDS)) {
                System.out.println("The server is shutting down...");
                pool.shutdownNow();
            }
            listener.close();
        }

        void shutdown(String name) {
            for (final Iterator<Handler> handlerIt = handlers.iterator(); handlerIt.hasNext(); ) {
                final Handler handler = handlerIt.next();
                if (handler.name.equals(name)) {
                    handlerIt.remove();
                    handler.shutdown();
                }
            }
        }

        public void broadcast(String line) {
            for (Handler handler : handlers) {
                handler.tell(line);
            }
        }

        public boolean canIBeAdded(final String name, final Handler me) {
            boolean canBeAdded = true;
            for (final Handler handler : handlers) {
                if (handler.name.equals(name)) {
                    canBeAdded = false;
                    break;
                }
            }
            if (!canBeAdded) {
                handlers.remove(me);
            }
            return canBeAdded;
        }
    }


    private static class Handler implements Runnable {

        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean shutdown = false;


        Handler(Socket socket) {
            this.socket = socket;
        }


        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("Submit Name");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!name.isEmpty() && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }


                out.println("NAME ACCEPTED " + name);
                System.out.println(name + " has joined");
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }
                writers.add(out);

                while (!shutdown) {
                    if (in.ready()) {
                        String input = in.readLine();
                        if (input.toLowerCase().startsWith("\\q")) {
                            return;
                        }
                        System.out.println(name + ": " + input);
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println(name + " is leaving");
                    names.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        void shutdown() {
            shutdown = true;
        }

        void tell(String line) {
            out.println(line);
            out.flush();
        }
    }
}