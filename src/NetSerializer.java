public interface NetSerializer {

    /**
     * Handles received data over network
     */
    boolean receive(int type, byte[] data);
}
