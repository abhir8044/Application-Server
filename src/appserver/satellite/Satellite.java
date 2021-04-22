package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import utils.PropertyHandler;


public class Satellite extends Thread {

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private HashMap<String, Tool> toolsCache = null; //maps class name to Tool object
    
    public PropertyHandler satelliteProperties;
    public PropertyHandler classLoaderProperties;
    public PropertyHandler serverProperties;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {
    	//declare property handler variables
    	
        
        //declare variables for storing satellite name, port and web server port information
        String satelliteName;
        String serverHost;
        String webServerHost;
        int satellitePort;
        int webServerPort;
        
    	try {
    		
    		// read properties of satellite, application server and class loader
    		satelliteProperties = new PropertyHandler(satellitePropertiesFile);
            classLoaderProperties = new PropertyHandler(classLoaderPropertiesFile);
            serverProperties = new PropertyHandler(serverPropertiesFile);
  
            // populate satelliteInfo object,
            // which later on will be sent to the server
            satellitePort = Integer.parseInt(satelliteProperties.getProperty("PORT"));
            satelliteName = satelliteProperties.getProperty("NAME");
            serverHost = serverProperties.getProperty("HOST");
            satelliteInfo.setPort(satellitePort);
            satelliteInfo.setName(satelliteName);
            satelliteInfo.setHost(serverHost);   
           
            // populate serverInfo object
            // other than satellites, the as doesn't have a human-readable name, so leave it out
            webServerPort = Integer.parseInt(classLoaderProperties.getProperty( "PORT" ));
            webServerHost = classLoaderProperties.getProperty( "HOST" );
            

            // create class loader
            classLoader = new HTTPClassLoader(webServerHost, webServerPort);
            

        	// create tools cache
            toolsCache = new HashMap<>();
           
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public void run() {

        // register this satellite with the SatelliteManager on the server
        // Currently not needed 
    	try{
            // register this satellite with the SatelliteManager on the server
            // ---------------------------------------------------------------
            Message message = new Message(3,satelliteInfo);
           Socket socket = new Socket(serverProperties.getProperty("HOST"),
                    Integer.parseInt(serverProperties.getProperty("PORT")));
            ObjectOutputStream writeToNet = new ObjectOutputStream(socket.getOutputStream());
            writeToNet.writeObject(message);
        }catch (IOException e) {
            System.err.println("[Satellite.run] Error: " + e);
        }        
    	// declare variables for holding server socket
      	ServerSocket serverSocket;
      
        try{
        	// create server socket
        	serverSocket = new ServerSocket( satelliteInfo.getPort());
        	
           
            
            // start taking job requests in a server loop
            while (true) {
            	 System.out.println("Waiting to accept a request on port " + satelliteInfo.getPort());
                (new Thread(new SatelliteThread(serverSocket.accept() ,this))).start();
            }

        } 
        catch (IOException e) {
          e.printStackTrace();
        }
    }

    // inner helper class that is instantiated in above server loop and processes single job requests
    private class SatelliteThread extends Thread {

        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite) {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() {
        	try {
        		// setting up object streams
        		readFromNet = new ObjectInputStream(jobRequest.getInputStream());
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());
                
                // reading message
                message = (Message)readFromNet.readObject();
                
        		switch (message.getType()) {
                
        		case JOB_REQUEST:
                    // processing job request
        			
                    // type cast received message to Job object
                    Job job = (Job)message.getContent();
                    
                    // get the tool object which returns the corresponding object of class
                    // needed for job
                    Tool tool = getToolObject(job.getToolName());
                    
                    // get the result from the received tool by passing the job parameters
                    Object result = tool.go(job.getParameters());
                    
                    // write result to output stream
                    writeToNet.writeObject(result);
                    
                    break;

                default:
                    System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
        		}
	
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     * If the tool has been used before, it is returned immediately out of the cache,
     * otherwise it is loaded dynamically
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        // if tool exists in cache then return from cache
    	Tool toolObject = toolsCache.get(toolClassString);
        if( toolObject != null){
            System.out.print( "[Satellite.getToolObject] Tool already in cache..." );
            return toolObject;
        }
        else{
            System.out.print( "[Satellite.getToolObject] Tool not in cache, loading class from server..." );
            
            // request the file(represented by toolClassString) from server with help of class loader
            Class toolClass = classLoader.loadClass(toolClassString);
            
            // Create instance of tool object from received tool class
            toolObject = (Tool)toolClass.getDeclaredConstructor().newInstance();
            // store the tool in cache for future calls
            toolsCache.put( toolClassString, toolObject );
            
            return toolObject;
        }
    }

    public static void main(String[] args) {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.run();
    }
}