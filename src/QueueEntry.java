import java.util.Scanner;

/*
Wraps either Messages or client Inputs
 */
public class QueueEntry {

    Type type;
    Object body;

    public enum Type {
        Input, Message
    }

    public QueueEntry(Type type, Object body) {
        this.type = type;
        this.body = body;
    }

    public Type getType() {
        return type;
    }

    public Object getBody() {
        return body;
    }

    public String toString(){
        String s = "";
        switch (type){
            case Message:
                s += "Message";
                break;
            case Input:
                s += "Input";
                break;
        }
        s += " " + body.toString();

        return s;
    }

    public static QueueEntry parseFrom(byte[] b){
        String s = new String(b);
        Scanner scanner = new Scanner(s);
        String type = scanner.next();

        String body = "";
        while (scanner.hasNextByte()){
            body += scanner.nextByte();
        }

        switch (type){
            case "Message":
                return new QueueEntry(Type.Message, body);
            case "Input":
                return new QueueEntry(Type.Input, body);
            default:
                return null;
        }
    }
}
