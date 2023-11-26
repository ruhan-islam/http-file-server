import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;


public class HTTPServer {
    static final int PORT = 6789;
    static final String rootDir = System.getProperty("user.dir") + "/root";
    //static final Path rootDir = Paths.get(Paths.get("").toAbsolutePath().toString() + "/root");
    static FileWriter writer;
    
    public static void main(String[] args) throws IOException {

        if (!Files.exists(Paths.get("log.txt"))) {
            try {
                Files.createFile(Paths.get("log.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        writer = null;
        try {
            writer = new FileWriter("log.txt", false);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (writer == null) {
            System.exit(1);
        }

        writer.close();

        ServerSocket serverConnect = null;
        try {
            serverConnect = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (serverConnect == null) {
            System.exit(1);
        }

        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while(true)
        {
            Socket socket = null;
            try {
                socket = serverConnect.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket == null) {
                break;
            }

            new Thread(new Worker(socket)).start();
        }

        writer.close();
    }
}
