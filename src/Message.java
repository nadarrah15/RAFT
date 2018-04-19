import com.example.raft.MessageProtos;

/*
Wraps generated Protobuf objects
 */
public class Message {

    private boolean isIncoming; // True if incoming; false if outgoing
    private Type type; // Type of underlying RPC
    private MessageProtos body;

    private enum Type {
        AppendEntries, AppendEntriesResponse, RequestVote, RequestVoteResponse
    }

    public Message(boolean isIncoming, int index, MessageProtos body) {
        this.isIncoming = isIncoming;
        type = Type.values()[index];
        this.body = body;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public int getType() {
        return type.ordinal();
    }

    public MessageProtos getBody() {
        return body;
    }
}
