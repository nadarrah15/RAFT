
import com.google.protobuf.GeneratedMessageV3;

/**
 * Wraps generated Protobuf objects
 */
public class Message {

    private String recipient; // Incoming if null or empty
    private Type type; // Type of underlying RPC
    private GeneratedMessageV3 body;

    public enum Type {
        AppendEntries, AppendEntriesResponse, RequestVote, RequestVoteResponse
    }

    public static Type TypeFromInt(int i) {
        switch (i) {
            case 0:
                return Type.AppendEntries;
            case 1:
                return Type.AppendEntriesResponse;
            case 2:
                return Type.RequestVote;
            case 3:
                return Type.RequestVoteResponse;
            default:
                return null;
        }
    }

    public Message(String recipient, Type type, GeneratedMessageV3 body) {
        this.recipient = recipient;
        this.type = type;
        this.body = body;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isIncoming() {
        return recipient == null || recipient.isEmpty();
    }

    public Type getType() {
        return type;
    }

    public GeneratedMessageV3 getBody() {
        return body;
    }
}
