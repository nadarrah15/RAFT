public class Net {

    private Node node;

    public void listen(int p) {
        while (true) {
            //1. launch a thread that listens on port p
            //2. when a connection comes in,
            //2a. launch a thread to receive the message and send it to the node queue
        }
    }

    public void send(String peer, int typ, byte[] data){
        //1. Launches a thread to
        //  a. open a socket to the peer
        //  b. send the type, data.length, data
        //  c. close the socket
    }
}
