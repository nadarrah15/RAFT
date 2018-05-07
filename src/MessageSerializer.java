import com.example.raft.MessageProtos;
import com.google.protobuf.InvalidProtocolBufferException;

public class MessageSerializer implements NetSerializer {

    Node node;

    public MessageSerializer(Node node) {
        this.node = node;
    }

    @Override
    public boolean receive(int type, byte[] data) {

        Message message = null;

        try {
            // Return if type invalid
            switch (type) {
                case 0:
                    message = new Message(null, Message.Type.AppendEntries, MessageProtos.AppendEntries.parseFrom(data));
                    break;
                case 1:
                    message = new Message(null, Message.Type.AppendEntriesResponse, MessageProtos.AppendEntriesResponse.parseFrom(data));
                    break;
                case 2:
                    message = new Message(null, Message.Type.RequestVote, MessageProtos.RequestVote.parseFrom(data));
                    break;
                case 3:
                    message = new Message(null, Message.Type.RequestVoteResponse, MessageProtos.RequestVoteResponse.parseFrom(data));
                    break;
                default:
                    return false;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("[SERIALIZER] Added message to queue");
        node.addToQueue(new QueueEntry(QueueEntry.Type.Message, message));
        return true;
    }
}
