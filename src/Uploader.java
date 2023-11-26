import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Uploader implements Runnable {

    private int BUFFER_SIZE = 1024;

    Socket socket;
    String fileName;
    Path filePath;

    public Uploader(String filePath) {
        this.socket = null;
        this.fileName = filePath.split("/")[filePath.split("/").length -1];
        this.filePath = Paths.get(filePath);
    }

    @Override
    public void run() {
        try {
            try {
                socket = new Socket("localhost", 6789);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket == null) {
                System.exit(1);
            }

            //DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (!Files.exists(filePath)) {
                pr.write("notFound");
                pr.append("\r\n").flush();

                System.out.println("Invalid path!!!");
            }
            else if (Files.isDirectory(filePath)) {
                pr.write("notFile");
                pr.append("\r\n").flush();

                System.out.println("not a valid file!!!");
            }
            else {

                String req = "UPLOAD " + fileName;
                pr.write(req);
                pr.append("\r\n").flush();

                String response = in.readLine();
                BUFFER_SIZE = Integer.parseInt(response);

                FileInputStream fis = new FileInputStream(filePath.toFile());
                byte[] buffer = new byte[BUFFER_SIZE];
                int buffLen;
                while( (buffLen = fis.read(buffer)) != -1 ){
                    dos.write(buffer, 0, buffLen);
                }
                fis.close();
            }

            dos.close();
            pr.close();
            in.close();

            socket.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
