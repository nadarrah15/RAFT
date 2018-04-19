
/*
Wraps either Messages or client Inputs
 */
public class QueueEntry {

    Type type;
    Object body;

    private enum Type {
        Input, Message
    }

    public QueueEntry(int index, Object body) {
        type = Type.values()[index];
        this.body = body;
    }

    public int getType() {
        return type.ordinal();
    }

    public Object getBody() {
        return body;
    }
}
