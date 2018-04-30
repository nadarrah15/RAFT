import com.example.raft.MessageProtos;
import com.google.protobuf.InvalidProtocolBufferException;

public class MessageSerializer implements NetSerializer {

    Node node;

    public MessageSerializer(Node node) {
        this.node = node;
    }

    @Override
    public void receive(int type, byte[] data) throws Exception {

        Message message = null;

        // Return if type invalid
        switch (type) {
            case 0:
                message = new Message(true, Message.Type.AppendEntries, MessageProtos.AppendEntries.parseFrom(data));
                break;
            case 1:
                message = new Message(true, Message.Type.AppendEntriesResponse, MessageProtos.AppendEntries.parseFrom(data));
                break;
            case 2:
                message = new Message(true, Message.Type.RequestVote, MessageProtos.AppendEntries.parseFrom(data));
                break;
            case 3:
                message = new Message(true, Message.Type.RequestVoteResponse, MessageProtos.AppendEntries.parseFrom(data));
                break;
            default:
                throw new IllegalArgumentException("Invalid type");
        }

        node.addToQueue(new QueueEntry(QueueEntry.Type.Message, message));
    }
}
