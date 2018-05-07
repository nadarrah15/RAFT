import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Client {

    public static void main(String[] args){
        while(true) {
            System.out.print("Enter request type or q to quit: ");
            Scanner in = new Scanner(System.in);
            String request = in.nextLine();

            if (request.equalsIgnoreCase("q"))
                break;
            else if (!request.equalsIgnoreCase("append") && !request.equalsIgnoreCase("delete")) {
                System.out.print("I do not recognize that request. Please try again. ");
                continue;
            }

            System.out.print("Enter the arguement: ");

            QueueEntry queueEntry = new QueueEntry(QueueEntry.Type.Input, request + in.nextLine());

            try {
                Scanner scan = new Scanner(new File("ipSet.txt"));
                int nodes = scan.nextInt();
                scan.nextLine();
                HashSet<String> ipSet = new HashSet<String>();

                byte[] data = queueEntry.toString().getBytes();
                send(ipSet, 6666, 4, data.length, data);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void send(Set<String> ipSet, int port, int type, int length, byte[] data) {
        for (String ip : ipSet) {
            send(ip, port, type, length, data);
        }
    }

    public static void send(String ip, int port, int type, int length, byte[] data) {
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
                System.out.println("[NET] Sent");
            } catch (IOException e) {
                //e.printStackTrace();
            }
        };

        new Thread(rs).start();
    }
}
