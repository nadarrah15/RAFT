import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;
import com.example.raft.MessageProtos;

public class Node {

    private Random rand = new Random();
    ExecutorService service = Executors.newSingleThreadExecutor();
    private HashSet<String> ipSet; // Stores IP addresses of fellow nodes
    private int currentTerm; // Latest term server has seen (initialized to 0 on first boot)
    private String votedFor; // Stores candidateId that received vote in current term (or null if none)
    private ArrayList<LogEntry> log; // Stores log entries
    private int commitIndex; // Index of highest log entry known to be committed (initialized to 0)
    private int lastApplied; // Index of highest log entry applied to state machine (initialized to 0)
    private State state; // Defines follower, candidate, or leader state
    private Queue<QueueEntry> taskQueue;

    //TODO implement LinkedHashMap of threads handling interaction with other nodes
    // A: Dedicate one thread to receiving all messages, one per node for sending messages?
    // B: Have each thread can contain server socket and client socket for one-way connections?
    // C: Dedicate one thread to receiving all messages, one per node for sending messages?

    // Conveys node state
    private enum State {
        FOLLOWER, CANDIDATE, LEADER
    }

    public Node(HashSet<String> ipSet) {
        this.ipSet = ipSet; // Store IP addresses in .txt file
        currentTerm = 0;
        commitIndex = 0;
        state = State.FOLLOWER; // Begin life as Follower
        ClientHandler clientHandler = new ClientHandler(this); // Start new thread for console (local client) input
        clientHandler.start();
    }

    public void run() {

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

    public void addToQueue(QueueEntry entry) {
        taskQueue.add(entry);
    }

    private State performFollower() {
        int timeout = rand.nextInt(150) + 150;
        if (commitIndex > lastApplied) {
            lastApplied++;
            //TODO Implement applying to log
            // apply(log.get(lastApplied))
        }
        //TODO Implement
        // Loop through performFollower operations
        while (true) {
            try {
                //Create Single-Thread for listener
                Runnable r = () -> {
                    /*
                        Message message = incoming message;
                        switch(message.getType()){
                            case AppendEntries:
                                if(AppendEntry.term > currentTerm)
                                    currentTerm = AppendEntry.term;
                                if(message.getBody().term < currentTerm)
                                    return new AppendEntryResponse(false);
                                if(prevLogIndex >= log.size() || log.get(prevLogIndex).getTerm() != prevLogTerm)
                                    return new AppendEntryResponse(false);
                                if(log.get(prevLogIndex).getTerm() != prevLogTerm){
                                    log.removeRange(prevLogIndex, log.size());
                                for(all entries in AppendEntries)
                                    log.add(entry);
                                if(leaderCommit > commitIndex){
                                    commitIndex = min(leaderCommit, log.get(log.size() - 1).index);
                                return new AppendEntryResponse(true);
                            case RequestVote:
                                if(term < currentTerm)
                                    return new RequestVoteResponse(false);
                                if((votedFor == null || votedFor == candidateId) && log is up to date)
                                    return new RequestVoteResponse(true);

                     */
                };

                Future<?> f = service.submit(r);
                f.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                return State.CANDIDATE;
            } catch (InterruptedException e) {
                System.out.println("Something Went Wrong In Execution");
            } catch (ExecutionException e) {
                System.out.println("Error in Entry Handling");
            }

            break;
        }
        return State.CANDIDATE;
    }

    private State performCandidate() {
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

            if(heartBeat is heard && heartBeat.currentTerm >= currentTerm)
                return State.FOLLOWER;

            if(election times out)
                return performCandidate();

            */
            break;
        }
        return State.LEADER;
    }

    private State performLeader() {
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
                send heartbeat
            }
            */

            //TODO replace with further implementation
            break;
        }

        return State.FOLLOWER;
    }
}
