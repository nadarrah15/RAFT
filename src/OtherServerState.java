public class OtherServerState {
    private int nextIndex; // Index of next log entry to send to other server
    private int matchIndex; // Index of highest log entry known to be replicated other server

    public OtherServerState(int nextIndex) {
        this.nextIndex = nextIndex;
        matchIndex = 0;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }
}
