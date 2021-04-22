package appserver.server;

import java.util.ArrayList;

public class LoadManager {

    static ArrayList satellites = null;
    static int index_referred_last=-1;
    public LoadManager() {
        satellites = new ArrayList<String>();
    }

    public void satelliteAdded(String satelliteName) {
        satellites.add(satelliteName);
        System.out.println("[LoadManager.satelliteAdded] " + satelliteName + " added");
    }

    public void satelliteRemoved(String satelliteName) {
        satellites.remove(satelliteName);
    }
    
    public String nextSatellite() throws Exception {
        
        int numberSatellites;
        
        synchronized (satellites) {
            numberSatellites = satellites.size();
            if(numberSatellites == 0) {
                throw new Exception("No Satellites are available");
            }
            
            if(index_referred_last+1 == numberSatellites) {
            	index_referred_last = -1;
            }
            index_referred_last++;
        }

        return (String) satellites.get(index_referred_last);
    }
}