import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Queue;

public class main {

    private static HashSet<String> ipSet; // Stores IP addresses of fellow nodes
    private static int currentTerm; // Latest term server has seen (initialized to 0 on first boot)
    private static Integer votedFor; // Stores candidateId that received vote in current term (or null if none)
    private static ArrayList<LogEntry> log; // Stores log entries
    private static int commitIndex; // Index of highest log entry known to be committed (initialized to 0)
    private static int lastApplied; // Index of highest log entry applied to state machine (initialized to 0)

    // TODO Implement Client input queue, RPC outbox queue, RPC inbox
    // TODO Create class which wraps/unwraps client input, RPC messages
    // TODO Replace generic types with said wrapper classes
    private static Queue<Integer> clientInput;
    private static Queue<Integer> rpcOutbox;
    private static Queue<Integer> rpcInbox;

    //TODO implement LinkedHashMap of threads handling interaction with other nodes
    // A: Dedicate one thread to receiving all messages, one per node for sending messages?
    // B: Have each thread can contain server socket and client socket for one-way connections?

    // Conveys node state
    private enum State {
        FOLLOWER, CANDIDATE, LEADER
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        ipSet = new HashSet<String>(Files.readAllLines(Paths.get(args[0]))); // Store IP addresses in .txt file
        State state = State.FOLLOWER; // Begin life as Follower
        ClientHandler clientHandler = new ClientHandler(); // Start new thread for console (local client) input
        clientHandler.start();

        // Commence lifetime operations
        while (true) {
            switch (state) {
                case FOLLOWER:
                    state = performFollower();
                    break;
                case CANDIDATE:
                    state = performCandidate();
                    break;
                case LEADER:
                    state = performLeader();
                    break;
            }
        }
    }

    private static State performFollower() {
        //TODO Implement
        // Loop through performFollower operations
        while (true) {
            //listen for heartbeat
            //once the heartbeat stops, break loop
            break;
        }
        return State.CANDIDATE;
    }

    private static State performCandidate() {
        //TODO Implement
        // Loop through performCandidate operations

        currentTerm++;      //increment term
        int numVotes = 1;   //vote for self
        //start election timer
        //Send RequestVote() to all

        while (true) {
            /*

            if(We get a vote)
                numVotes++;

            if(numVotes >= majority)
                return State.leader
            else
                keep waiting

            if(heartBeat is heard)


            if(election times out)
                return performCandidate();

            */
            break;
        }
        return State.LEADER;
    }

    private static State performLeader() {
        // Initialize volatile state variables (reinitialized after election)
        LinkedHashMap<String, OtherServerState> otherStates = new LinkedHashMap<String, OtherServerState>();
        for (String ip : ipSet) {
            otherStates.put(ip, new OtherServerState(log.size()));
        }

        // Loop through performLeader operations
        while (true) {
            if (commitIndex > lastApplied) {
                lastApplied++;
                //TODO Implement applying to log
                // apply(log.get(lastApplied))
            }

            //TODO Check if in-queue and out-queue are empty
            /*
            if (client input queue has something) {
                send AppendEntries RPC
                reset timer
            } else if (RPC queue has something) {
                respond appropriately
            } else if (RPC Response queue has something) {
                respond appropriately
            } else {
                check timer
            }
            */

            //TODO replace with further implementation
            break;
        }
        return State.FOLLOWER;
    }
}
