import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORTNR = 3000;

    public static void main(String[] args) throws IOException{
        ServerSocket server = new ServerSocket(PORTNR);
        System.out.println("HTTP-Server listening on port " + PORTNR);
        File index = new File("index.html");
        BufferedReader reader;

        while(true){
            Socket client = server.accept();
            System.out.println("Client connected");
            reader = new BufferedReader(new FileReader(index));
            PrintWriter writer = new PrintWriter(client.getOutputStream());
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println("Content-Length: " + index.length());
            writer.println("\r\n");
            String line = reader.readLine();
            while(line != null){
                writer.println(line);
                line = reader.readLine();
            }

            writer.close();
            reader.close();
            client.close();
        }
    }
}
