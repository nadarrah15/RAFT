import com.example.raft.MessageProtos;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

public class Node {

    final int PORT = 6666;

    private Random rand = new Random();
    ExecutorService service = Executors.newSingleThreadExecutor();  //TODO: Write description
    private HashSet<String> ipSet; // Stores IP addresses of fellow nodes
    private String id;         // nodes ID
    private int currentTerm; // Latest term server has seen (initialized to 0 on first boot)
    private String votedFor; // Stores candidateId that received vote in current term (or null if none)
    private ArrayList<LogEntry> log; // Stores log entries
    private int commitIndex; // Index of highest log entry known to be committed (initialized to 0)
    private int lastApplied; // Index of highest log entry applied to state machine (initialized to 0)
    private State state; // Defines follower, candidate, or leader state
    public Net net;
    private Queue<QueueEntry> taskQueue;    //TODO: Reply to 'What is this?'
    private String database;


    //TODO implement LinkedHashMap of threads handling interaction with other nodes
    // A: Dedicate one thread to receiving all messages, one per node for sending messages?
    // B: Have each thread can contain server socket and client socket for one-way connections?
    // C: Dedicate one thread to receiving all messages, one per node for sending messages?

    // Conveys node state
    private enum State {
        FOLLOWER, CANDIDATE, LEADER
    }

    public int getState(){
        switch(state){
            case FOLLOWER:
                return 0;
            case CANDIDATE:
                return 1;
            case LEADER:
                return 2;
            default:
                return -1;
        }
    }

    public Node(HashSet<String> ipSet) throws Exception {
        System.out.println("[NODE] Constructing");
        this.ipSet = ipSet; // Store IP addresses in .txt file
        currentTerm = 0;
        id = Inet4Address.getLocalHost().getHostAddress();
        log = new ArrayList<LogEntry>();
        commitIndex = 0;
        lastApplied = 0;
        taskQueue = new ConcurrentLinkedQueue<QueueEntry>();
        state = State.FOLLOWER; // Begin life as Follower
        net = new Net(new MessageSerializer(this));
        taskQueue = new ConcurrentLinkedQueue<>();
        database = "";
        log = new ArrayList<>();
        log.add(new LogEntry(0, null));

        net.listen(PORT);
        System.out.println("[NODE] Starting ClientHandler");
        ClientHandler clientHandler = new ClientHandler(this); // Start new thread for console (local client) input
        new Thread(clientHandler).start();
    }

    public void run() {
        // Commence lifetime operations
        while (true) {
            System.out.println("[NODE] term " + currentTerm);
            switch (state) {
                case FOLLOWER:
                    System.out.println("[NODE] State -> follower");
                    state = performFollower();
                    break;
                case CANDIDATE:
                    System.out.println("[NODE] State -> candidate");
                    state = performCandidate();
                    break;
                case LEADER:
                    System.out.println("[NODE] State -> leader");
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
        long timeStart = System.currentTimeMillis();
        // Do performFollower operations
        while (commitIndex > lastApplied) {
            lastApplied++;
            //TODO Implement applying to state machine
            apply(log.get(lastApplied));
        }
        // Check taskQueue
        while(true) {
            QueueEntry task = taskQueue.poll();
            // If no tasks available, just check timer
            if (task != null) {
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
                                case AppendEntries:
                                    MessageProtos.AppendEntries appendEntries = (MessageProtos.AppendEntries) message.getBody();
                                    MessageProtos.AppendEntriesResponse appendEntriesResponse;

                                    // Construct response
                                    if (log.size() <= appendEntries.getPrevLogIndex() ||
                                            log.get(appendEntries.getPrevLogIndex()).term != appendEntries.getPrevLogTerm()) {
                                        // Increase currentTerm to received term if received term exceeds currentTerm
                                        if (appendEntries.getTerm() >= currentTerm) {
                                            currentTerm = appendEntries.getTerm();
                                        }

                                        // Prepare failure response
                                        appendEntriesResponse = MessageProtos.AppendEntriesResponse.newBuilder().setSuccess(false).setTerm(currentTerm).build();
                                    } else {
                                        // Reset election timer
                                        timeStart = System.currentTimeMillis();
                                        // Prepare success response
                                        // Update currentTerm if necessary
                                        currentTerm = appendEntries.getTerm();
                                        appendEntriesResponse = MessageProtos.AppendEntriesResponse.newBuilder().setSuccess(true).setTerm(currentTerm).build();

                                        // If existing entry conflicts with new one (same index, different terms), delete existing entry and all that follow
                                        for (int start = appendEntries.getPrevLogIndex() + 1; log.size() - 1 > start; ) {
                                            log.remove(start);
                                        }

                                        if (appendEntries.getEntriesCount() > 0) {
                                            // Append new entries to log
                                            for (int i = 0; i < appendEntries.getEntriesCount(); i++) {
                                                MessageProtos.AppendEntries.Entry entry = appendEntries.getEntries(i);
                                                log.add(new LogEntry(entry.getTerm(), entry.getMessage()));
                                            }
                                        }
                                    }
                                    // Add pending response to task queue
                                    taskQueue.add(new QueueEntry(QueueEntry.Type.Message, new Message(appendEntries.getLeaderId(), Message.Type.AppendEntriesResponse, appendEntriesResponse)));
                                    break;

                                case RequestVote:
                                    MessageProtos.RequestVote requestVote = (MessageProtos.RequestVote) message.getBody();
                                    MessageProtos.RequestVoteResponse requestVoteResponse;

                                    // Use default term of 0 if log is empty
                                    int term = (log.isEmpty()) ? 0 : log.get(log.size() - 1).term;

                                    // Construct response
                                    if (requestVote.getTerm() >= currentTerm &&
                                            votedFor == null &&
                                            requestVote.getLastLogIndex() >= log.size() - 1 &&
                                            requestVote.getLastLogTerm() >= term) {
                                        // Prepare to grant vote
                                        // Update currentTerm if necessary
                                        currentTerm = requestVote.getTerm();
                                        requestVoteResponse = MessageProtos.RequestVoteResponse.newBuilder().setVoteGranted(true).setTerm(currentTerm).build();
                                        votedFor = requestVote.getCandidateId();
                                        // Reset election timer
                                        timeStart = System.currentTimeMillis();
                                    } else {
                                        // Prepare to deny vote
                                        requestVoteResponse = MessageProtos.RequestVoteResponse.newBuilder().setVoteGranted(false).setTerm(currentTerm).build();
                                    }
                                    // Add pending response to task queue
                                    taskQueue.add(new QueueEntry(QueueEntry.Type.Message, new Message(requestVote.getCandidateId(), Message.Type.RequestVoteResponse, requestVoteResponse)));
                                    break;
                                // Ignore incoming AppendEntriesResponse, RequestVoteResponse tasks as follower
                            }
                        } else {
                            // If message is outgoing, send response
                            switch (message.getType()) {
                                case AppendEntriesResponse:
                                    MessageProtos.AppendEntriesResponse appendEntriesResponse = (MessageProtos.AppendEntriesResponse) message.getBody();
                                    byte[] data = appendEntriesResponse.toByteArray();
                                    // Call Net object to actually send message across sockets
                                    net.send(message.getRecipient(), PORT, 1, data.length, data);
                                    break;
                                case RequestVoteResponse:
                                    MessageProtos.RequestVoteResponse requestVoteResponse = (MessageProtos.RequestVoteResponse) message.getBody();
                                    data = requestVoteResponse.toByteArray();
                                    // Call Net object to actually send message across sockets
                                    net.send(message.getRecipient(), PORT, 3, data.length, data);
                                    break;
                                // Ignore outgoing AppendEntries, RequestVote tasks as follower
                            }
                        }
                }
            }
            // Become candidate if election timer expires
            if (System.currentTimeMillis() - timeStart >= timeout)
                return State.CANDIDATE;
            //else {
               // return State.FOLLOWER;
            //}
        }
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
        //System.out.println("[NODE] Sending requestVote");
        sendAll(requestVote, 2);

        //start timer
        long start = System.currentTimeMillis();

        //instantiate incoming message
        Message message = null;

        Random rand = new Random();
        int timeout = rand.nextInt(150) + 150;

        while (message == null) {

            //wait for incoming message until timeout. Once timeout occurs, restart candidacy
            long end = System.currentTimeMillis();

            if (end - start >= timeout)
                break;

            //Receive either a heartbeat or a vote
            QueueEntry entry = taskQueue.peek();

            /*
            if(!message.isIncoming())
                This isn't possible, change_my_mind_meme.jpg
             */

            //determine message type
            if (entry != null) {
                message = (Message) entry.getBody();
                switch (message.getType()) {
                    case AppendEntries:
                        MessageProtos.AppendEntries appendMessage = (MessageProtos.AppendEntries) message.getBody();
                        //check to see if this is the real leader
                        if (appendMessage.getTerm() >= currentTerm) {
                            return State.FOLLOWER;
                        }
                        message = null;
                        break;
                    case RequestVoteResponse:
                        MessageProtos.RequestVoteResponse response = (MessageProtos.RequestVoteResponse) message.getBody();
                        if(response.getTerm() > currentTerm) {
                            currentTerm = response.getTerm();
                            return State.FOLLOWER;
                        }
                        else if (response.getVoteGranted()) {
                            numVotes++;
                            start = System.currentTimeMillis();
                            //check if we have majority
                            //TODO Determine correct majority
                            if (numVotes > ipSet.size() / 2 + 1)
                                return State.LEADER;
                        }
                        taskQueue.remove();
                        message = null;
                        break;
                    case RequestVote:
                        MessageProtos.RequestVote requestVote1 = (MessageProtos.RequestVote) message.getBody();
                        if(requestVote1.getTerm() > currentTerm){
                            currentTerm = requestVote1.getTerm();
                            return State.FOLLOWER;
                        }
                        taskQueue.remove();
                        message = null;
                        break;
                }
            }
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

        while (true) {

            if (commitIndex > lastApplied) {
                lastApplied++;
                apply(log.get(lastApplied));
            }

            sendAll(MessageProtos.AppendEntries.newBuilder()
                    .setTerm(currentTerm).setLeaderId(id)
                    .setPrevLogIndex(log.size() - 1)
                    .setPrevLogTerm(log.isEmpty() ? 0 : log.get(log.size() - 1).term)
                    .setLeaderCommit(commitIndex)
                    .build(), 0);

            if (!taskQueue.isEmpty()) {
                QueueEntry entry = taskQueue.poll();
                // Check entry type
                Message message;
                switch (entry.getType()) {
                    case Input:
                        String body = (String) entry.body;
                        LogEntry e = new LogEntry(currentTerm, body);
                        apply(e);
                        lastApplied++;
                        byte[] data = body.getBytes();
                        try {
                            sendAll(MessageProtos.AppendEntries.newBuilder().setTerm(currentTerm).setLeaderId(id).setPrevLogIndex(commitIndex).setPrevLogTerm(currentTerm).setEntries(commitIndex + 1, MessageProtos.AppendEntries.Entry.parseFrom(data)).build(), 0);
                        }catch(InvalidProtocolBufferException ex){

                        }
                        break;

                    case Message:
                        message = (Message) entry.getBody();
                        // Check if message is ingoing or outgoing
                        if (message.isIncoming()) {
                            // Process message
                            switch (message.getType()) {
                                case AppendEntries:
                                    MessageProtos.AppendEntries appendMessage = (MessageProtos.AppendEntries) message.getBody();
                                    if(appendMessage.getTerm() >= currentTerm){
                                        currentTerm = appendMessage.getTerm();
                                        return State.FOLLOWER;
                                    }
                                    sendAll(appendMessage, 0);
                                    addToFront(entry);
                                    break;
                                case RequestVote:
                                    MessageProtos.RequestVote requestVote = (MessageProtos.RequestVote) message.getBody();
                                    if (requestVote.getTerm() >= currentTerm &&
                                            votedFor == null &&
                                            requestVote.getLastLogIndex() >= log.size() - 1 &&
                                            requestVote.getLastLogTerm() >= log.get(log.size() - 1).term) {
                                        currentTerm = requestVote.getTerm();
                                        MessageProtos.RequestVoteResponse requestVoteResponse = MessageProtos.RequestVoteResponse.newBuilder().setVoteGranted(true).setTerm(currentTerm).build();
                                        votedFor = requestVote.getCandidateId();
                                        return State.FOLLOWER;
                                    }
                                    else{
                                        sendAll(MessageProtos.AppendEntries.newBuilder()
                                                .setTerm(currentTerm).setLeaderId(id)
                                                .setPrevLogIndex(log.size() - 1)
                                                .setPrevLogTerm(log.isEmpty() ? 0 : log.get(log.size() - 1).term)
                                                .setLeaderCommit(commitIndex)
                                                .build(), 0);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            sendAll(MessageProtos.AppendEntries.newBuilder()
                                    .setTerm(currentTerm).setLeaderId(id)
                                    .setPrevLogIndex(log.size() - 1)
                                    .setPrevLogTerm(log.isEmpty() ? 0 : log.get(log.size() - 1).term)
                                    .setLeaderCommit(commitIndex)
                                    .build(), 0);
                        }


                        //TODO replace with further implementation
                        break;
                }

            }else{
                sendAll(MessageProtos.AppendEntries.newBuilder()
                        .setTerm(currentTerm).setLeaderId(id)
                        .setPrevLogIndex(log.size() - 1)
                        .setPrevLogTerm(log.isEmpty() ? 0 : log.get(log.size() - 1).term)
                        .setLeaderCommit(commitIndex)
                        .build(), 0);
            }
        }
    }

    //adds entry to the front of the queue
    private void addToFront(QueueEntry entry) {
        Queue<QueueEntry> temp = new ConcurrentLinkedQueue<QueueEntry>();   //TODO: make sure this is the right structure
        temp.add(entry);
        temp.addAll(taskQueue);
        taskQueue = temp;
    }

    // sends message to all nodes
    private void sendAll(com.google.protobuf.GeneratedMessageV3 message, int type) {
        //TODO: write code to send the message to all the nodes
        byte[] data = message.toByteArray();
        net.send(ipSet, 6666, type, data.length, data);
    }

    //receives message from other nodes
    private Message getMessage() {
        //TODO: implement
        return null;
    }

    private boolean apply(LogEntry entry) {
        Scanner scan = new Scanner(entry.command);
        // TODO Error handling
        switch (scan.next().toLowerCase()) {
            case "append":
                database.concat(scan.next());
                break;
            case "delete":
                int start = scan.nextInt();
                database.substring(0, database.length() - start);
                break;
            default:
                return false;
        }

        return true;
    }
}
