import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class WebSocket {
    private static final int PORTNR = 3001;
    private static ArrayList<WebSocketThread> threads = new ArrayList<>();

    public static void main(String[] args) throws IOException{
        ServerSocket server = new ServerSocket(PORTNR);
        System.out.println("WS-Server listening on port " + PORTNR);

        while(true){
            Socket client = server.accept();
            System.out.println("Client connected");
            WebSocketThread thread = new WebSocketThread(client);
            threads.add(thread);
            new Thread(thread).start();
        }
    }

    static void sendToAll(byte[] bytes){
        ArrayList<WebSocketThread> trash = new ArrayList<>();
        try{
            for(WebSocketThread thread : threads){
                try{
                    thread.getSocket().getOutputStream().write(bytes);
                }catch(SocketException se){
                    trash.add(thread);
                }
            }
            for(WebSocketThread thread : trash){
                threads.remove(thread);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

class WebSocketThread implements Runnable{
    private Socket client;
    private boolean running = true;

    public WebSocketThread(Socket client){
        this.client = client;
    }

    @Override
    public void run(){
        try{
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            Scanner scanner = new Scanner(in, "UTF-8");

            String line = scanner.nextLine();
            if(line.contains("GET")){
                byte[] response = handleHandshake(scanner);
                out.write(response);
            }

            while(running){
                byte[] bytes = in.readNBytes(1);
                if(bytes.length > 0){
                    byte first = bytes[0];
                    if(first + 128 == 1){
                        int length = (in.readNBytes(1)[0] + 128);
                        byte[] keys = in.readNBytes(4);
                        byte[] encoded = in.readNBytes(length);
                        byte[] decoded = new byte[length];

                        for(int i=0; i < length; i++){
                            decoded[i] = (byte) (encoded[i] ^ keys[i & 0x3]);
                        }

                        byte[] response = new byte[length + 2];
                        response[0] = (byte) 0x81;
                        response[1] = (byte) length;
                        for(int i=2; i<length+2; i++){
                            response[i] = decoded[i-2];
                        }
                        WebSocket.sendToAll(response);
                    }
                }
            }

            scanner.close();
            out.close();
            in.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private byte[] handleHandshake(Scanner scanner) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        String line;
        while(!(line = scanner.nextLine()).equals("")){
            if(line.contains("Sec-WebSocket-Key")){
                String keyString = line.split(":")[1].trim();
                String encoded = Base64
                .getEncoder()
                .encodeToString(MessageDigest.
                getInstance("SHA-1")
                .digest((keyString + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                .getBytes("UTF-8")));
                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + encoded
                + "\r\n\r\n").getBytes("UTF-8");
                while(!(line = scanner.nextLine()).equals(""));
                return response;
            }
        }
        return null;
    }

    Socket getSocket(){
        return client;
    }

    void stop(){
        running = false;
    }
}
