import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class Main {

    final static String  IP_FILE = "IPSet.txt";

    public static void main(String[] args) throws IOException {

        Node node = new Node(new HashSet<String>(Files.readAllLines(Paths.get(IP_FILE))));  //what is args[0]
        node.run();
    }
}
