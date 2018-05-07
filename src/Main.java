import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class Main {

    public static void main(String[] args) throws IOException {
        final String IP_FILE = "ipSet.txt";
        Node node = new Node(new HashSet<>(Files.readAllLines(Paths.get(IP_FILE))));  //what is args[0]
        node.run();
    }
}
