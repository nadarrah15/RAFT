import com.example.raft.MessageProtos;
import com.google.protobuf.GeneratedMessageV3;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

public class Node {

    private Random rand = new Random();
    ExecutorService service = Executors.newSingleThreadExecutor();  //TODO: Reply to 'What is this?'
    private HashSet<String> ipSet; // Stores IP addresses of fellow nodes
    private String id;         // nodes ID
    private int currentTerm; // Latest term server has seen (initialized to 0 on first boot)
    private String votedFor; // Stores candidateId that received vote in current term (or null if none)
    private ArrayList<LogEntry> log; // Stores log entries
    private int commitIndex; // Index of highest log entry known to be committed (initialized to 0)
    private int lastApplied; // Index of highest log entry applied to state machine (initialized to 0)
    private State state; // Defines follower, candidate, or leader state
    private Net net;
    private Queue<QueueEntry> taskQueue;    //TODO: Reply to 'What is this?'

    //TODO implement LinkedHashMap of threads handling interaction with other nodes
    // A: Dedicate one thread to receiving all messages, one per node for sending messages?
    // B: Have each thread can contain server socket and client socket for one-way connections?
    // C: Dedicate one thread to receiving all messages, one per node for sending messages?

    // Conveys node state
    private enum State {
        FOLLOWER, CANDIDATE, LEADER
    }

    public Node(HashSet<String> ipSet) throws UnknownHostException {
        this.ipSet = ipSet; // Store IP addresses in .txt file
        currentTerm = 0;
        id = Inet4Address.getLocalHost().getHostAddress();
        commitIndex = 0;
        lastApplied = 0;
        state = State.FOLLOWER; // Begin life as Follower
        net = new Net(new MessageSerializer(this));
        taskQueue = new ConcurrentLinkedQueue<>();

        ClientHandler clientHandler = new ClientHandler(this); // Start new thread for console (local client) input
        new Thread(clientHandler).start();
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

    public void addToQueue(QueueEntry task) {
        taskQueue.add(task);
    }

    private State performFollower() {
        int timeout = rand.nextInt(150) + 150;
        if (commitIndex > lastApplied) {
            lastApplied++;
            //TODO Implement applying to log
            // apply(log.get(lastApplied))
        }
        //TODO Implement timer
        // Loop through performFollower operations
        while (true) {
            // Check taskQueue
            QueueEntry task = taskQueue.remove();
            // Check entry type
            switch (task.getType()) {
                case Input:
                    // Check type of client input (command, crash, reboot, etc.)
                    // Redirect client commands to leader
                    break;

                case Message:
                    Message message = (Message) task.getBody();
                    // Check if message is ingoing or outgoing
                    if (message.isIncoming()) {
                        // Process message
                        switch (message.getType()) {
                            case AppendEntriesResponse:
                                MessageProtos.AppendEntries appendEntries = (MessageProtos.AppendEntries) message.getBody();
                                MessageProtos.AppendEntriesResponse appendEntriesResponse;

                                // Construct response
                                if (appendEntries.getTerm() < currentTerm ||
                                        log.size() <= appendEntries.getPrevLogTerm() ||
                                        log.get(appendEntries.getPrevLogIndex()).term != appendEntries.getPrevLogTerm()) {
                                    // Prepare failure response
                                    appendEntriesResponse = MessageProtos.AppendEntriesResponse.newBuilder().setSuccess(false).setTerm(currentTerm).build();
                                } else {
                                    // Prepare success response
                                    appendEntriesResponse = MessageProtos.AppendEntriesResponse.newBuilder().setSuccess(true).setTerm(currentTerm).build();

                                    if (appendEntries.getEntriesCount() < 1) {
                                        // If entries[] is empty, acknowledge message as heartbeat
                                        //Reset timer
                                    } else {
                                        // If existing entry conflicts with new one (same index, different terms), delete existing entry and all that follow
                                        for (int start = appendEntries.getPrevLogIndex() + 1; log.size() > start; ) {
                                            log.remove(start);
                                        }

                                        // Append new entries to log
                                        for (int i = 0; i < appendEntries.getEntriesCount(); i++) {
                                            MessageProtos.AppendEntries.Entry entry = appendEntries.getEntries(i);
                                            log.add(new LogEntry(entry.getTerm(), entry.getMessage()));
                                        }
                                    }

                                }

                                byte[] data = appendEntriesResponse.toByteArray();

                                // Call Net object to actually send message across sockets
                                net.send(appendEntries.getLeaderId(), 1, data.length, data);

                                break;

                            case RequestVoteResponse:
                                MessageProtos.RequestVote requestVote = (MessageProtos.RequestVote) message.getBody();
                                MessageProtos.RequestVoteResponse requestVoteResponse;

                                // Construct response
                                if (requestVote.getTerm() >= currentTerm &&
                                        votedFor == null &&
                                        requestVote.getLastLogIndex() >= log.size() - 1 &&
                                        requestVote.getLastLogTerm() >= log.get(log.size() - 1).term) {
                                    // Prepare to grant vote
                                    requestVoteResponse = MessageProtos.RequestVoteResponse.newBuilder().setVoteGranted(true).setTerm(currentTerm).build();
                                } else {
                                    // Prepare to deny vote
                                    requestVoteResponse = MessageProtos.RequestVoteResponse.newBuilder().setVoteGranted(false).setTerm(currentTerm).build();
                                    votedFor = requestVote.getCandidateId();
                                }

                                data = requestVoteResponse.toByteArray();

                                // Call Net object to actually send message across sockets
                                net.send(requestVote.getCandidateId(), 3, data.length, data);

                                break;
                            // Ignore AppendEntries, RequestVote tasks as follower
                        }
                    } else {
                        // Send message to leader node
                    }
            }

            break;
        }

        return State.CANDIDATE;
    }

    private State performCandidate() {

        currentTerm++;      //increment term
        int numVotes = 1;   //vote for self

        //Build the RequestVote RPC
        MessageProtos.RequestVote.Builder requestVoteBuilder = MessageProtos.RequestVote.newBuilder();
        requestVoteBuilder.setTerm(currentTerm)
                .setCandidateId(id)
                .setLastLogIndex(log.size() - 1);
        if (log.size() > 0)
            requestVoteBuilder.setLastLogTerm(log.get(log.size() - 1).term);
        else
            requestVoteBuilder.setLastLogTerm(0);
        MessageProtos.RequestVote requestVote = requestVoteBuilder.build();

        //Send RequestVote() to all
        sendAll(requestVote);

        //start timer
        long start = System.nanoTime();

        //instantiate incoming message
        Message message = null;

        while (message == null) {

            //wait for incoming message until timeout. Once timeout occurs, restart candidacy
            long end = System.nanoTime();
            if (end - start == 500)
                break;

            //Receive either a heartbeat or a vote
            if (taskQueue.size() != 0)
                message = (Message) taskQueue.poll().getBody();     //TODO: resolve issues
        }


        switch (message.getType()) {
            case AppendEntries:
                //if (message.getTerm >= currentTerm){
                taskQueue.add(new QueueEntry(QueueEntry.Type.Message, message.getBody()));     //TODO: resolve issues
                return State.FOLLOWER;
            //}
            //break;
            case RequestVoteResponse:
                //if(message.getVoteGranted()){
                numVotes++;
                if (numVotes > ipSet.size() / 2)
                    return State.LEADER;
                break;
        }

        return State.CANDIDATE;
    }


    private State performLeader() {
        // Initialize volatile state variables (reinitialized after election)
        LinkedHashMap<String, OtherServerState> otherStates = new LinkedHashMap<String, OtherServerState>();
        for (String ip : ipSet) {
            otherStates.put(ip, new OtherServerState(log.size()));
        }

        // Loop through performLeader operations
        sendAll(MessageProtos.AppendEntries.newBuilder().build());
        while (true) {

            if (commitIndex > lastApplied) {
                lastApplied++;
                //TODO Implement applying to log
                // apply(log.get(lastApplied))
            }

            if (!taskQueue.isEmpty()) {
                QueueEntry entry = taskQueue.remove();
                // Check entry type
                switch (entry.getType()) {
                    case Input:
                        //Process Client Command
                        break;

                    case Message:
                        Message message = (Message) entry.getBody();
                        // Check if message is ingoing or outgoing
                        if (message.isIncoming()) {
                            // Process message
                            switch (message.getType()) {
                                case AppendEntries:
                                    com.google.protobuf.GeneratedMessageV3 appendEntries = message.getBody();
                                    sendAll(appendEntries);
                                    break;
                                case RequestVote:
                                    //Process RequestVotes
                            }
                        } else {
                            sendAll(MessageProtos.AppendEntries.newBuilder().build());
                        }


                        //TODO replace with further implementation
                        break;
                }

                return State.FOLLOWER;
            }
        }
    }

    // sends message to all nodes
    private void sendAll(com.google.protobuf.GeneratedMessageV3 message) {
        //TODO: write code to send the message to all the nodes
    }

    //receives message from other nodes
    private Message getMessage() {
        //TODO: implement
        return null;
    }
}
