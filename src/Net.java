public class Net {

    private Node node;

    public void listen(int port) {
        while (true) {
            //1. launch a socket that listens on port p
            //2. when a connection comes in,
            //2a. launch a thread to receive the message and send it to the node queue
        }
    }

    public void send(String peer, int type, int length, byte[] data){
        //1. Launches a thread to
        //  a. open a socket to the peer
        //  b. send the type, data.length, data
        //      bi. Types: AppendEntries, 0; AppendEntriesResponse, 1; RequestVote, 2; RequestVoteResponse, 3
        //  c. close the socket
    }

}
