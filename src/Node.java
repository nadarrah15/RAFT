import com.example.raft.MessageProtos;
import com.google.protobuf.GeneratedMessageV3;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
    private Queue<QueueEntry> taskQueue;    //TODO: Reply to 'What is this?'

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
        lastApplied = 0;
        state = State.FOLLOWER; // Begin life as Follower
        ClientHandler clientHandler = new ClientHandler(this); // Start new thread for console (local client) input
        clientHandler.start();
        //TODO: Find a way to randomly initialize unique ID
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

            /*
            try {
                //Create Single-Thread for listener
                Runnable r = () -> {
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
           */

            try {
                //Create Single-Thread for listener
                Runnable r = () -> {
                    // Check taskQueue
                    QueueEntry entry = taskQueue.remove();
                    // Check entry type
                    switch (entry.getType()) {
                        case Input:
                            // Check type of client input (command, crash, reboot, etc.)
                            // Redirect client commands to leader
                            break;

                        case Message:
                            Message message = (Message) entry.getBody();
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
                                                for (int i = appendEntries.getPrevLogIndex(); i < log.size(); ) {
                                                    if (log.get(i).term != appendEntries.getTerm()) {
                                                        log.remove(i);
                                                        continue;
                                                    }
                                                    i++;
                                                }

                                                // Add new entries to log
                                                for (int i = appendEntries.getPrevLogIndex(); i < appendEntries.getPrevLogIndex() + appendEntries.getEntriesCount(); i++) {

                                                }
                                            }

                                        }

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

                                        // Call Net object to actually send message across sockets

                                        break;
                                    // Ignore AppendEntries, RequestVote tasks as follower
                                }
                            } else {
                                // Send message to leader node
                            }
                    }
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

            //TODO Check if in-queue and out-queue are empty

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
            private void sendAll (com.google.protobuf.GeneratedMessageV3 message){
                //TODO: write code to send the message to all the nodes
            }

            //receives message from other nodes
            private Message getMessage () {
                //TODO: implement
                return null;
            }
            private void sendToLeader (Message message){
                //TODO: Write
            }
        }
