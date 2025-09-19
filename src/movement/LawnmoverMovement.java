package movement;

import java.util.List;

import core.Connection;
import core.Coord;
import core.DTNHost;
import core.Settings;
import core.SimScenario;

/**
 * This movement model makes the host execute the lawnmover pattern
 */
public class LawnmoverMovement extends ExtendedMovementModel implements SwitchableMovement {
    /** Name space of the settings (append to group name space) */
    public static final String LAWNMOVER_MOVEMENT_NS = "LawnmoverMovement.";
    /** 
     * Per node group setting for defining the start coordinates of the 
     * line({@value})
     */
    public static final String START_LOCATION_S = "startLocation";

    private Coord startLoc;
    private int offset;
    private GranularMovement granularMM;
    private Coord initLoc;
    private Coord lastLoc;
    private Path nextPath;

    private String horizontalDirection;
    private String verticalDirection;
    private int connectionDownCount;

    private DTNHost host;
    private int hostCounter;
    private DTNHost fogHost;
    private Connection fogConnection;
    private List<Connection> connections;

    // Host-specific variables
    private static List<DTNHost> self;

    /**
    * Creates a new movement model based on a Settings object's settings.
    * @param s The Settings object where the settings are read from
    */
    public LawnmoverMovement(Settings settings) {
        super(settings);
        int coords[];
        granularMM = new GranularMovement(settings);
        coords = settings.getCsvInts(LAWNMOVER_MOVEMENT_NS + START_LOCATION_S, 2);
        this.startLoc = new Coord(coords[0], coords[1]);
        setCurrentMovementModel(granularMM);
    }

    /**
    * Copy constructor.
    * @param lm The LawnmoverMovement prototype
    */
    public LawnmoverMovement(LawnmoverMovement lm) {
        super(lm);
        granularMM.setLocation(initLoc);
        
        // default initial values for movement
        horizontalDirection = "left";
        verticalDirection = "up";
        connectionDownCount = 0;
    }

    public boolean newOrders() {
        self = SimScenario.getInstance().getHosts();
        while(hostCounter < self.size()) {
            DTNHost host = self.get(hostCounter++);
            String hostName = host.getName();
            if(hostName.startsWith("m")) {
                this.host = host;
            }
            if(hostName.startsWith("p")) {
                fogHost = host;
            }
        }

        this.connections = this.host.getConnections();
        for(Connection connection: connections) {
            if(connection.getOtherNode(this.host) == fogHost) {
                fogConnection = connection;
                // System.out.println(fogConnection);
                break;
            }
        }

        if(fogConnection != null) {
            if(fogConnection.isUp() == false && granularMM.isReady()) {
                connectionDownCount++;
                // switch vertical direction
                verticalDirection = (verticalDirection == "up") ? "down" : "up";
                
                if (connectionDownCount > 1) {
                    // switch horizontal direction
                    if (horizontalDirection == "left") {
                        horizontalDirection = "right";
                    } else if (horizontalDirection == "right") {
                        // stop
                    }
                    
                    // return to the fog vehicle
                }

                // generate path in horizontal direction then vertical direction
                granularMM.generateNextPath(verticalDirection);
            } else {
                connectionDownCount = 0;
                
                // generate a new path
                granularMM.generateNextPath(verticalDirection);
            }
        }
        return true;
    }

    @Override
    public Coord getInitialLocation() {
        return this.initLoc;
    }

    @Override
	public MovementModel replicate() {
		return new LawnmoverMovement(this);
	}

    /**
    * Tell the movment model what its current location is
    * @param lastWaypoint
    */
    public void setLocation(Coord lastWaypoint) {
        this.initLoc = lastWaypoint.clone();
    }
    
    /**
	 * Checks if the movement model is finished doing its task and it's time to
	 * switch to the next movement model. The method should be called between
	 * getPath() calls.
	 * @return true if ready
	 */
	@Override
	public boolean isReady() {
	    return true;
	}

    /**
	 * Get the last location the getPath() of this movement model has returned
	 * @return the last location
	 */
    @Override
	public Coord getLastLocation() {
    return this.lastLoc;
  }
}
