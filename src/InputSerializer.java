
public class InputSerializer implements NetSerializer{
    Node node;

    public InputSerializer(Node node) {
        this.node = node;
    }

    @Override
    public boolean receive(int type, byte[] data) {
        QueueEntry queueEntry = null;

        queueEntry = new QueueEntry(QueueEntry.Type.Input, new String(data));

        if(node.getState() == 2) {
            node.addToQueue(new QueueEntry(QueueEntry.Type.Input, queueEntry));
            return true;
        }

        return false;
    }
}
