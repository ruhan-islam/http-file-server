import java.util.Scanner;

public class HTTPClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("path/filename.type: ");
            String fPath = scanner.nextLine();
            if (fPath.equals("") || fPath.startsWith(" ") || fPath.startsWith("\t")) {
                break;
            }

            new Thread(new Uploader(fPath)).start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
