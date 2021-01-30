package cc.cc1234.domain.zookeeper.entity;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FourLetterCommand {

    private String host;

    private int port;

    public FourLetterCommand(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String request(String command) {
        final Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
            return response(socket);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String response(Socket client) {
        try {
            var reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            var builder = new StringBuilder("");
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str).append("\n");
            }
            cleanup(client);
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void cleanup(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

}
