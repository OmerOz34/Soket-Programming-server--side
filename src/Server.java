import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.lang.Math;

public class Server {

    static HashMap<Integer, String> messagelist = new HashMap<>();
    static HashMap<String, Socket> users = new HashMap<>();
    static HashMap<Socket, String> users2 = new HashMap<>();
    int serverState = 1;
    Socket GameMaster = null;
    static boolean isClicked = false;
    static HashMap<Socket, Integer> scoreList = new HashMap<>();
    static Socket leader = null;
    static int counter = 1;
    ServerSocket serverSocket;
    static boolean isGameStarted = false;
    Object object = new Object();

    public static void main(String[] args) throws IOException, InterruptedException {


        messagelist.put(1, "message [1] connected");
        messagelist.put(2, "message [2] waiting other clients to connect");
        messagelist.put(3, "message [3] game will start in 5 seconds");
        messagelist.put(4, "message [4] a client dropped game");
        messagelist.put(5, "message [5] game over");
        messagelist.put(6, "message [6] a client connected");
        messagelist.put(7, "message [7] waiting game master to start game ");
        messagelist.put(8, "message [8] you are game master");
        messagelist.put(9, "message [9] game started");
        messagelist.put(10, "A game already started or ended");
        messagelist.put(11, "message [11] delete object");
        Server server = new Server();
        server.StartServer(9999);

    }


    void StartServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("server started");


        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("client baglandi" + socket);

            ServerThread clientThread = new ServerThread(socket, this);
            clientThread.start();

            //list.put(clientThread, counter);


        }

    }


    // OYUN BAŞLATAN THREAD
    Thread GameStart = new Thread(new Runnable() {

        @Override
        public void run() {

            try {
                Server.SendMessage(messagelist.get(3));
                GameStart.sleep(5000);
                Server.SendMessage(messagelist.get(9));

                Server.SendClientList();

                CreateObject();
                while (counter <= 10) {
                    synchronized (object) {


                        if (isClicked) {
                            DeleteObject();
                            Server.UpdateScore();
                            leader = null;
                            isClicked = false;
                            counter++;
                            CreateObject();

                        } else {
                            object.wait();
                        }

                    }

                }
                DeleteObject();
                String winners=DetectWinner();
                SendMessage("winner "+winners );
                SendMessage(messagelist.get(5));

                serverState = 4;


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });


    //SEND MESSAGE TO ALL CLIENTS
    static void SendMessage(String message) throws IOException {

        ArrayList<Socket> values = new ArrayList<>(users.values());
        for (Socket value : values) {
            PrintWriter output;
            output = new PrintWriter(value.getOutputStream(), true);
            output.println(message);
        }


    }


    //SEND MESSAGE TO A CLIENT
    static void SendMessage(String message, String nickName) throws IOException {
        Socket socket = users.get(nickName);
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        output.println(message);

    }

    //SEND MESSAGE TO A CLIENT WITH SOCKET
    static void SendMessage(String message, Socket socket) throws IOException {

        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        output.println(message);
    }


    //SEND USERS TO ALL CLIENT
    static void SendClientList() throws IOException {

        String clientList = "userlist";
        ArrayList<String> values = new ArrayList<>(users2.values());
        for (String value : values) {
            clientList = clientList + " " + value;
        }
        System.out.println(clientList);
        Server.SendMessage(clientList);


    }




     String DetectWinner(){
        String winners="";
        int maxValueInMap=(Collections.max(scoreList.values()));  // This will return max value in the Hashmap
        for (Map.Entry<Socket, Integer> entry : scoreList.entrySet()) {  // Itrate through hashmap
            if (entry.getValue()==maxValueInMap) {


                winners=winners+" "+users2.get(entry.getKey());
            }

        }
        return winners;

    }

    //TIKAMAYA GÖRE LİSTEDEN NİCKNAME E PUAN EKLE
    static void UpdateScore() throws IOException {
        int point = scoreList.get(leader);
        point += 5;
        scoreList.remove(leader);
        scoreList.put(leader, point);
        String nickName = users2.get(leader);
        String message = "score " + nickName + " update " + point;
        SendMessage(message);
    }


    //NESNE OLUŞTUR VE GÖNDER
    void CreateObject() throws IOException {



        Double[] coordinates=new Double[10];
        int counter=0;
      for(int i=0;i<5;i++){

          double startX = 80;
          double endX = 380;
          double randomX = new Random().nextDouble();
          double resultX = startX + (randomX * (endX - startX));


          double startY = 130;
          double endY = 380;
          double randomY = new Random().nextDouble();
          double resultY = startX + (randomY * (endY - startY));
          coordinates[counter]=resultX;
          counter++;
          coordinates[counter]=resultY;
          counter++;
      }
        String message="create "+coordinates[0]+" "+coordinates[1]+" "+coordinates[2]+" "+coordinates[3]+" "+coordinates[4]+
            " "+coordinates[5]+" "+coordinates[6]+" "+coordinates[7]+" "+coordinates[8]+" "+coordinates[9];

            System.out.println(message);




        SendMessage(message);


    }


    //BİRİSİ TIKLADIKTAN SONRA TÜM CLIENT LARA ŞEKİLLERİ SİLİP YENİ ÇEKİL GÖNDERİYOR
    void DeleteObject() throws IOException {
        String message = "delete object";
        SendMessage(message);
    }


    static void Close(Socket socket) throws IOException{

        String nickName=users2.get(socket);
        users.remove(users2.get(socket));
        users2.remove(socket);
        scoreList.remove(socket);
        SendMessage("dropclient "+nickName);





    }


}
