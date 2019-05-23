import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;


public class ServerThread extends Thread {


    String nickName;
    private Socket socket;
    int messageCode;
    private Server server;
    String[] message;
    Boolean isRegistered = false;
    BufferedReader reader;

    public ServerThread(Socket socket, Server server) {

        this.socket = socket;
        this.server = server;
    }


    public void run() {


        try {

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] msg = reader.readLine().split(" ");


            if (msg[0].equals("connect")) {
                if (server.serverState != 1 && server.serverState != 2) {
                    Server.SendMessage(Server.messagelist.get(10), socket); //


                } else {
                    nickName = msg[1];
                    Server.users.put(nickName, socket);
                    Server.users2.put(socket, nickName);
                    Server.scoreList.put(socket, 0);
                }

            }


            if (server.serverState == 1) {
                server.GameMaster = socket;
                Server.SendMessage(Server.messagelist.get(1), nickName); // "connected"
                sleep(1000);
                Server.SendMessage(Server.messagelist.get(8), nickName); // "you are game master"
                sleep(1000);
                Server.SendMessage(Server.messagelist.get(2), nickName);  //"waiting other clients to connect"

                server.serverState = 2;

            } else if (server.serverState == 2) {

                Server.SendMessage(Server.messagelist.get(1), nickName); // "connected"
                sleep(1000);
                Server.SendMessage(Server.messagelist.get(7), nickName);  // "waiting game master to start game "
                sleep(1000);
                Server.SendMessage(Server.messagelist.get(6), Server.users2.get(server.GameMaster));  // "a client connected"

            } else if (server.serverState == 3) {
                server.SendMessage(Server.messagelist.get(10));
                socket.close();
                reader.close();


            } else if (server.serverState == 4) {
            }

            GetMessage.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    Thread GetMessage = new Thread(new Runnable() {

        @Override
        public void run() {

            while (true) {
                try {
                    //reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String[] msg = reader.readLine().split(" ");
                    System.out.println(Arrays.toString(msg));

                    if (msg[0].equals("click")) {
                        if (server.serverState == 3) {
                            if (server.leader == null) {

                                synchronized (server.object) {
                                    server.leader = socket;
                                    server.isClicked = true;
                                    server.object.notify();
                                }
                            }
                        }
                    }
                    if (msg[0].equals("close")) {

                        socket.close();
                        reader.close();
                        Server.Close(socket);
                    }

                    if (msg[0].equals("start")) {
                        if (server.serverState == 2) {
                            if (Server.users2.get(socket).equals(Server.users2.get(server.GameMaster)))   //oyunu başlatma isteğini gönderen master ise
                            {


                                Server.isGameStarted = true;
                                server.GameStart.start();
                                server.serverState = 3;

                            }
                        }

                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
    });


}
