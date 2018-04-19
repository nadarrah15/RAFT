import com.example.raft.MessageProtos;

import java.io.Serializable;

/*
Wraps generated Protobuf objects
 */
public class Message implements Serializable{

    Type type;
    MessageProtos body;

    protected enum Type {
        AppendEntries, AppendEntriesResponse, RequestVote, RequestVoteResponse
    }

    public Message(int index, MessageProtos body) {
        type = Type.values()[index];
        this.body = body;
    }

    public int getType() {
        return type.ordinal();
    }

    public MessageProtos getBody() {
        return body;
    }
}
