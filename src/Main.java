import java.io.*;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    final static String IP_FILE = "ipSet.txt";

    public static void main(String[] args) throws IOException {

        Scanner scan = new Scanner(new File(IP_FILE));
        int nodes = scan.nextInt();
        scan.nextLine();
        HashSet<String> ipSet = new HashSet<String>();
        String thisIp = Inet4Address.getLocalHost().getHostAddress();

        for (int i = 0; i < nodes; i++) {
            String current = scan.nextLine();
            if (!thisIp.equals(current))
                ipSet.add(current);
        }

        Node node = new Node(ipSet);  //what is args[0]
        node.run();
    }
}
