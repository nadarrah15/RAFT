import java.util.Scanner;

public class Client {

    public static void main(String[] args){
        while(true){
            System.out.print("Enter in request or q to quit: ");
            Scanner in = new Scanner(System.in);
            String s = in.nextLine();

            if(s.equalsIgnoreCase("q"))
                break;


        }
    }
}
