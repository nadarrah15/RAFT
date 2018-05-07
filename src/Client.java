import java.util.Scanner;

public class Client {

    public static void main(String[] args){
        while(true){
            System.out.print("Enter request type or q to quit: ");
            Scanner in = new Scanner(System.in);
            String request = in.nextLine();

            if(request.equalsIgnoreCase("q"))
                break;
            else if(!request.equalsIgnoreCase("append") && !request.equalsIgnoreCase("delete")) {
                System.out.print("I do not recognize that request. Please try again. ");
                continue;
            }

            System.out.print("Enter the arguement: ");

            QueueEntry queueEntry = new QueueEntry(QueueEntry.Type.Input, request + in.nextLine());

        }
    }
}
