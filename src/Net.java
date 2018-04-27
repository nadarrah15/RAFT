import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Net {

    private NetSerializer serializer;

    public Net(NetSerializer serializer) {
        this.serializer = serializer;
    }

    public void listen(int port) throws Exception {
        Runnable r = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                    int type = dis.readInt();
                    byte[] data = new byte[dis.readInt()];

                    dis.readFully(data);
                    serializer.receive(type, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        new Thread(r).start();

        //1. launch a socket that listens on port p
        //2. when a connection comes in,
        //2a. launch a thread to receive the message and send it to the node queue
    }

    public void send(String peer, int type, int length, byte[] data) {
        //1. Launches a thread to
        //  a. open a socket to the peer
        //  b. send the type, data.length, data
        //      bi. Types: AppendEntries, 0; AppendEntriesResponse, 1; RequestVote, 2; RequestVoteResponse, 3
        //  c. close the socket
    }

}
