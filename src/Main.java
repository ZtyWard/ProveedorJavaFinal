import server.SocketServer;

public class Main {

    public static void main(String[] args) {

        SocketServer servidor =
                new SocketServer();

        servidor.iniciarServidor();
    }
}
