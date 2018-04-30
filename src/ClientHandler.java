import java.util.Scanner;

public class ClientHandler implements Runnable {

    Scanner scan;
    Node node;

    public ClientHandler(Node node) {
        scan = new Scanner(System.in);
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            node.addToQueue(new QueueEntry(QueueEntry.Type.Input, scan.next()));
        }
    }
}
