import com.example.raft.MessageProtos;
import com.google.protobuf.InvalidProtocolBufferException;

public class MessageSerializer implements NetSerializer {

    Node node;

    public MessageSerializer(Node node) {
        this.node = node;
    }

    @Override
    public boolean receive(int type, byte[] data) {

        QueueEntry queueEntry = null;
        boolean flag = false; //flag for if this is input and if this should not be processed

        try {
            // Return if type invalid
            switch (type) {
                case 0:
                    queueEntry = new QueueEntry(QueueEntry.Type.Message, new Message(null, Message.Type.AppendEntries, MessageProtos.AppendEntries.parseFrom(data)));
                    break;
                case 1:
                    queueEntry = new QueueEntry(QueueEntry.Type.Message, new Message(null, Message.Type.AppendEntriesResponse, MessageProtos.AppendEntriesResponse.parseFrom(data)));
                    break;
                case 2:
                    queueEntry = new QueueEntry(QueueEntry.Type.Message, new Message(null, Message.Type.RequestVote, MessageProtos.RequestVote.parseFrom(data)));
                    break;
                case 3:
                    queueEntry = new QueueEntry(QueueEntry.Type.Message, new Message(null, Message.Type.RequestVoteResponse, MessageProtos.RequestVoteResponse.parseFrom(data)));
                    break;
                case 4:
                    queueEntry = new QueueEntry(QueueEntry.Type.Input, QueueEntry.parseFrom(data).getBody());
                    if(node.getState() == 2)
                        flag = true;
                    break;
                default:
                    return false;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return false;
        }

        if(!flag) {
            System.out.println("[SERIALIZER] Added message to queue");
            node.addToQueue(queueEntry);
            return true;
        }

        return false;
    }
}
