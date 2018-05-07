import com.example.raft.MessageProtos;

public class LogEntry {

    int term;
    String command;

    public LogEntry(int term, String command){
        this.term = term;
        this.command = command;
    }
}
