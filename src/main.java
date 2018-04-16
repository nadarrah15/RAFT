import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class main {

    private static HashSet<String> ipSet; // Stores IP addresses of fellow nodes
    private static ArrayList<LogEntry> log;

    // Conveys node state
    private enum State {
        FOLLOWER, CANDIDATE, LEADER;
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
                    state = follower();
                    break;
                case CANDIDATE:
                    state = candidate();
                    break;
                case LEADER:
                    state = leader();
                    break;
            }
        }
    }

    private static State follower () {
        return State.CANDIDATE;
    }

    private static State candidate () {
        return State.LEADER;
    }

    private static State leader () {
        return State.FOLLOWER;
    }
}
