import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

public class Net {

    private NetSerializer serializer;
    private boolean isPartitioned;

    public Net(NetSerializer serializer) {
        this.serializer = serializer;
        isPartitioned = false;
    }

    public boolean getIsPartitioned() {
        return isPartitioned;
    }

    public void setIsPartitioned(boolean isPartitioned) {
        this.isPartitioned = isPartitioned;
    }

    public void listen(int port) throws Exception {
        //1. launch a socket that listens on port p
        //2. when a connection comes in,
        //2a. launch a thread to receive the message and send it to the node queue
        Runnable rs = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    if (!isPartitioned) {
                        Socket clientSocket = serverSocket.accept();
                        // New thread per client
                        Runnable rc = () -> {
                            try {
                                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                                int type = dis.readInt();
                                byte[] data = new byte[dis.readInt()];

                                dis.readFully(data);
                                serializer.receive(type, data);
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        };
                        new Thread(rc).start();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        new Thread(rs).start();
    }

    public void send(Set<String> ipSet, int port, int type, int length, byte[] data) {
        for (String ip : ipSet) {
            send(ip, port, type, length, data);
        }
    }

    public void send(String ip, int port, int type, int length, byte[] data) {
        //1. Launches a thread to
        //  a. open a socket to the peer
        //  b. send the type, data.length, data
        //      bi. Types: AppendEntries, 0; AppendEntriesResponse, 1; RequestVote, 2; RequestVoteResponse, 3
        //  c. close the socket
        // New thread per recipient
        Runnable rs = () -> {
            try {
                Socket sendSocket = new Socket(ip, port);
                DataOutputStream dos = new DataOutputStream(sendSocket.getOutputStream());

                dos.writeInt(type);
                dos.writeInt(length);
                dos.write(data);

                sendSocket.close();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        new Thread(rs).start();
    }
}
