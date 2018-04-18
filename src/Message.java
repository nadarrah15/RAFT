import com.example.raft.MessageProtos;

/*
Wraps generated Protobuf objects
 */
public class Message {

    Type type;
    MessageProtos body;

    private enum Type {
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
