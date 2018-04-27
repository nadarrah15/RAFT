public interface NetSerializer {

    /**
     * Handles received data over network
     */
    void receive(int type, byte[] data) throws Exception;
}
