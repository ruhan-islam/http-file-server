import javax.annotation.processing.Filer;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Worker implements Runnable {

    private static final int BUFFER_SIZE = 1024 * 1024 * 1024;
    Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream stream = socket.getInputStream();

            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            //DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            FileWriter writer = new FileWriter("log.txt", true);

            StringBuffer sb = new StringBuffer();
            for (int character = stream.read(); character != -1; character = stream.read()) {
                if ((char) character == '\n') break;
                sb.append((char) character);
            }

            String input = sb.toString();

            System.out.println(input);

            writer.write("\nHTTP request: ");
            writer.write(input);

            StringBuilder content = new StringBuilder();

            content.append("\n<HTML>\n" +
                    "<HEAD>\n" +
                    "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                    "</HEAD>\n" +
                    "<BODY>\n");

            content.append("<form enctype=\"multipart/form-data\" action=\"http://localhost:6789/upload?upload_progress_id=12344\" method=\"POST\">\n" +
                    "            File:\n" +
                    "            <input type=\"file\" name=\"file\" id=\"file\" /> <br/>\n" +
                    "            <input type=\"submit\" value=\"Upload\" name=\"upload\" id=\"upload\" />\n" +
                    "        </form>");

            if(input.length() > 0) {
                if(input.startsWith("GET")) {
                    String reqPath = input.split(" ")[1].replace("%20", " ");
                    Path path = Paths.get(HTTPServer.rootDir + reqPath);
                    File loc = path.toFile();

                    if (Files.exists(path)) {
                        if (reqPath.equals("/")) {
                            reqPath = "";
                        }

                        if (Files.isDirectory(path)) {
                            File [] fileList = loc.listFiles();
                            if (fileList == null) {
                                return;
                            }

                            content.append("<ul>\n");

                            for (File f : fileList) {
                                if (Files.isDirectory(f.toPath())) {
                                    content.append("<li> <a href = \"").append(reqPath).append("/").append(f.getName()).append("\"> <b>").append(f.getName()).append("</b> </a> </li> </br>\n");
                                }
                                else {
                                    content.append("<li> <a href = \"").append(reqPath).append("/").append(f.getName()).append("\"> <i>").append(f.getName()).append("</i> </a> </li> </br>\n");
                                }
                            }

                            content.append("</ul>\n");
                            content.append("</BODY>\n" + "</HTML>\n\n");

                            writer.write("\nHTTP response: ");
                            pr.write("HTTP/1.1 200 OK\r\n");
                            writer.write("HTTP/1.1 200 OK\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            writer.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            writer.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Type: text/html\r\n");
                            writer.write("Content-Type: text/html\r\n");
                            pr.write("Content-Length: " + content.length() + "\r\n");
                            writer.write("Content-Length: " + content.length() + "\r\n");
                            pr.write("\r\n");
                            writer.write(content.toString());
                            pr.write(content.toString());
                            pr.append("\r\n").flush();
                            writer.append("\r\n").flush();
                        }
                        else {
                            writer.write("\nHTTP response: ");
                            pr.write("HTTP/1.1 200 OK\r\n");
                            writer.write("HTTP/1.1 200 OK\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            writer.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            writer.write("Date: " + new Date() + "\r\n");
                            //pr.write("Accept-Ranges: bytes\r\n");
                            //pr.write("Cache-Control: public, max-age=0\r\n");
                            pr.write("Content-Disposition: attachment\r\n");
                            pr.write("Content-Type: " + Files.probeContentType(path) + "\r\n");
                            pr.write("Content-Type: application/x-force-download\r\n");
                            writer.write("Content-Type: application/x-force-download\r\n");
                            pr.write("Content-Length: " + Files.size(path) + "\r\n");
                            writer.write("Content-Length: " + Files.size(path) + "\r\n");
                            //pr.write("\r\n");
                            writer.append("\r\n").flush();
                            pr.append("\r\n").flush();

                            FileInputStream fis = new FileInputStream(path.toFile());
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int buffLen;
                            while ( (buffLen = fis.read(buffer)) != -1) {
                                dos.write(buffer, 0, buffLen);
                                dos.flush();
                            }
                            fis.close();
                        }
                    }
                    else {
                        System.out.println("404 Page Not Found");
                        content.append("\n<h1> 404 Page Not Found </h1>\n");
                        content.append("</BODY>\n" + "</HTML>\n\n");

                        writer.write("\nHTTP response: ");
                        pr.write("HTTP/1.1 404 Not Found\r\n");
                        writer.write("HTTP/1.1 404 Not Found\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        writer.write("Date: " + new Date() + "\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        writer.write("Content-Type: text/html\r\n");
                        pr.write("Content-Length: " + content.length() + "\r\n");
                        writer.write("Content-Length: " + content.length() + "\r\n");
                        pr.write("\r\n");
                        writer.write(content.toString());
                        pr.write(content.toString());
                        writer.append("\r\n").flush();
                        pr.append("\r\n").flush();
                    }
                }
                else if (input.equalsIgnoreCase("notFound")) {
                    System.out.println("Invalid path for file upload!!!");
                }
                else if (input.equalsIgnoreCase("notFile")) {
                    System.out.println("path is not a valid file!!!");
                }
                else if (input.startsWith("UPLOAD")) {
                    pr.print(BUFFER_SIZE);
                    pr.append("\r\n").flush();
                    String fileName = input.split(" ")[1];
                    if (!Files.exists(Paths.get(fileName))) {
                        try {
                            Files.createFile(Paths.get(fileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(fileName, false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (fos == null) {
                        System.exit(1);
                    }

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int buffLen;
                    while ( (buffLen = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, buffLen);
                        fos.flush();
                    }
                    fos.close();
                }
//------------------------------------------------------------------------------------------------------------------------------------------------
                else if (input.startsWith("POST")) {
                    Path path = Paths.get(HTTPServer.rootDir);
                    File loc = path.toFile();

                    File[] fileList = loc.listFiles();
                    if (fileList == null) {
                        return;
                    }

                    content.append("<ul>\n");

                    for (File f : fileList) {
                        if (Files.isDirectory(f.toPath())) {
                            content.append("<li> <a href = \"").append("/").append(f.getName()).append("\"> <b>").append(f.getName()).append("</b> </a> </li> </br>\n");
                        } else {
                            content.append("<li> <a href = \"").append("/").append(f.getName()).append("\"> <i>").append(f.getName()).append("</i> </a> </li> </br>\n");
                        }
                    }

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream("tmpFile0", false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (fos == null) {
                        System.exit(1);
                    }

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int buffLen;
                    boolean check = false;
                    while ( (buffLen = stream.read(buffer)) != -1) {
                        System.out.println("buflen = " + buffLen);
                        System.out.println(new String(buffer, 0, buffLen));
                        fos.write(buffer, 0, buffLen);
                        fos.flush();
                        if (buffLen < 65536) {
                            break;
                        }
                    }
                    fos.close();


                    content.append("</ul>\n");
                    content.append("</BODY>\n" + "</HTML>\n\n");

                    writer.write("\nHTTP response: ");
                    pr.write("HTTP/1.1 204 No Content\r\n");
                    writer.write("HTTP/1.1 204 No Content\r\n");
                    pr.write("Server: Java HTTP Server: 1.0\r\n");
                    writer.write("Server: Java HTTP Server: 1.0\r\n");
                    pr.write("Date: " + new Date() + "\r\n");
                    writer.write("Date: " + new Date() + "\r\n");
                    pr.write("Content-Type: text/html\r\n");
                    writer.write("Content-Type: text/html\r\n");
                    pr.write("Content-Length: " + content.length() + "\r\n");
                    writer.write("Content-Length: " + content.length() + "\r\n");
                    pr.write("\r\n");
                    writer.write(content.toString());
                    pr.write(content.toString());
                    pr.append("\r\n").flush();
                    writer.append("\r\n").flush();

                    System.out.println(content.toString());


                    BufferedReader reader = new BufferedReader(new FileReader("tmpFile0"));
                    long totLen = 0, start, finish;
                    String data, boundary, filename;
                    while (true) {
                        data = reader.readLine();
                        System.out.println(totLen);
                        System.out.println(data);
                        totLen += data.length() + 2;
                        if (data.contains("boundary=")) {
                            boundary = data.split("=")[1];
                            break;
                        }
                    }
                    while (true) {
                        data = reader.readLine();
                        System.out.println(totLen);
                        System.out.println(data);
                        totLen += data.length() + 2;
                        if (data.contains("filename")) {
                            filename = data.split("\"")[3];
                            break;
                        }
                    }
                    while (true) {
                        data = reader.readLine();
                        System.out.println(totLen);
                        System.out.println(data);
                        totLen += data.length() + 2;
                        if (data.equals("")) {
                            start = totLen;
                            break;
                        }
                    }
                    while (true) {
                        data = reader.readLine();
                        totLen += data.length() + 2;
                        if (data.contains(boundary)) {
                            finish = totLen - data.length() - 4;
                            break;
                        }
                    }
                    while (true) {
                        data = reader.readLine();
                        System.out.println(totLen);
                        System.out.println(data);
                        totLen += data.length() + 2;
                        if (data.endsWith(boundary + "--")) {
                            totLen -= 2;
                            System.out.println(totLen);
                            break;
                        }
                    }
                    reader.close();

                    finish = Files.size(Paths.get("tmpFile0")) - (totLen - finish) - 2;

                    if (!Files.exists(Paths.get(filename))) {
                        try {
                            Files.createFile(Paths.get(filename));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    FileInputStream fis = new FileInputStream("tmpFile0");
                    fos = null;
                    try {
                        fos = new FileOutputStream(filename, false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (fos == null) {
                        System.exit(1);
                    }

                    //for (int i = 0; i < start; i++) {
                    //    fis.read();
                    //}
                    fis.skip(start);
                    for (long i = start; i < finish; i++) {
                        fos.write(fis.read());
                    }

                    fis.close();
                    fos.close();

                    Files.delete(Paths.get("tmpFile0"));
                }
            }


            writer.close();
            dos.close();
            pr.close();
            stream.close();

            socket.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
