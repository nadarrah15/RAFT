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
            String input = scan.next();
            if (input.equals("partition")) {
                node.net.setIsPartitioned(true);
            } else if (input.equals("unpartition")) {
                node.net.setIsPartitioned(false);
            } else {
                node.addToQueue(new QueueEntry(QueueEntry.Type.Input, input));
            }
        }
    }
}

