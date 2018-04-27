
import com.example.raft.MessageProtos;
import com.google.protobuf.GeneratedMessageV3;

import java.io.Serializable;

/*
Wraps generated Protobuf objects
 */
public class Message implements Serializable{

    private boolean isIncoming; // True if incoming; false if outgoing
    private Type type; // Type of underlying RPC
    private GeneratedMessageV3 body;

    public enum Type {
        AppendEntries, AppendEntriesResponse, RequestVote, RequestVoteResponse
    }

    public Message(boolean isIncoming, Type type, GeneratedMessageV3 body) {
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

    public GeneratedMessageV3 getBody() {
        return body;
    }
}
