import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class main {

    private static HashSet<String> ipSet; // Stores IP addresses of fellow nodes
    private static int currentTerm; // Latest term server has seen (initialized to 0 on first boot)
    private static Integer votedFor; // Stores candidateId that received vote in current term (or null if none)
    private static ArrayList<LogEntry> log; // Stores log entries
    private static int commitIndex; // Index of highest log entry known to be committed (initialized to 0)
    private static int lastApplied; // Index of highest log entry applied to state machine (initialized to 0)

    //TODO Implement Client input queue, RPC outbox queue, RPC inbox queue

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
        // Loop through performFollower operations
        while (true) {
            break;
        }
        return State.CANDIDATE;
    }

    private static State performCandidate() {
        // Loop through performCandidate operations
        while (true) {
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
