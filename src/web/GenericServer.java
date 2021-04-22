package web;

import java.io.*;
import java.net.*;
import java.util.Properties;
import utils.PropertyHandler;

public abstract class GenericServer implements Runnable {

    static ServerSocket serverSocket;
    static int port;

    public Socket socket;

    /**
     * The Constructor
     */
    public GenericServer(String serverPropertiesFile) {
        try {
            Properties properties;
            properties = new PropertyHandler(serverPropertiesFile);
            port = Integer.parseInt(properties.getProperty("PORT"));
           
        } catch (Exception e) {
            System.err.println("[GenericServer.GenericServer] Properties file " + serverPropertiesFile + " not found, exiting ...");
            System.exit(1);
        }

        GenericServer.port = port;
    }

    /**
     * The method <code>run()</code> implements the interface
     * <code>Runnable</code>
     */
    @Override
	public void run() {
        try {
            serverSocket = new ServerSocket(port);
              
            while (true) {
                System.out.println("[GenericServer.run] Waiting for connections on Port #" + port);
                socket = serverSocket.accept();
                System.out.println("[GenericServer.run] A connection to a client is established!");
                processConnection(socket);
            }

        } catch (IOException ioe) {
            System.err.println("IOException" + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    /**
     * The method <code>processConnection()</code> contains the "Intelligence"
     * of servers, i.e. his application specific functionality. This method has
     * to be overridden by concrete servers.
     */
    protected abstract void processConnection(Socket socket);
}
