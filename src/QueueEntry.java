
/*
Wraps either Messages or client Inputs
 */
public class QueueEntry {

    Type type;
    Object body;

    public enum Type {
        Input, Message
    }

    public QueueEntry(Type type, Object body) {
        this.type = type;
        this.body = body;
    }

    public Type getType() {
        return type;
    }

    public Object getBody() {
        return body;
    }
}
