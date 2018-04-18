
/*
Wraps either Messages or client Inputs
 */
public class QueueEntry<E> {

    Type type;
    E body;

    private enum Type {
        Input, Message
    }

    public QueueEntry(int index, E body) {
        type = Type.values()[index];
        this.body = body;
    }

    public int getType() {
        return type.ordinal();
    }

    public E getBody() {
        return body;
    }
}
