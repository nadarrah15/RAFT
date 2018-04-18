import java.util.Scanner;

public class ClientHandler extends Thread {

    Scanner scan;
    Node node;

    public ClientHandler(Node node) {
        scan = new Scanner(System.in);
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            node.addToQueue(new QueueEntry(1, scan.next()));
        }
    }
}
