package backupserver;

import java.io.IOException;
import java.net.SocketException;

public class BackupServer {
    public static void main(String[] args) throws IOException, SocketException, InterruptedException {
        RunJobs.start();
    }
}