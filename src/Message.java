import com.example.raft.MessageProtos;

/*
Wraps generated Protobuf objects
 */
public class Message {

    private boolean isIncoming; // True if incoming; false if outgoing
    private Type type; // Type of underlying RPC
    private MessageProtos body;

    public enum Type {
        AppendEntries, AppendEntriesResponse, RequestVote, RequestVoteResponse
    }

    public Message(boolean isIncoming, Type type, MessageProtos body) {
        this.isIncoming = isIncoming;
        this.type = type;
        this.body = body;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public Type getType() {
        return type;
    }

    public MessageProtos getBody() {
        return body;
    }
}
