package src.Queue;

import java.net.*;
import java.io.*;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.concurrent.*;

import static java.lang.Integer.parseInt;

public class Queue {
    private static int serverPort = 6000;

    //LinkedBlockingQueue does not need size
    public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();;



    public static void main(String args[]){
        int numero=0;
        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            //System.out.println("A escuta no porto 6000");
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                //System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                numero++;
                new Connection(clientSocket, numero, queue);
            }
        } catch(IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }
}

//= Thread para tratar de cada canal de comunicação com um cliente
class Connection extends Thread {
    DataInputStream in;
    //DataOutputStream out;
    Socket clientSocket;
    int thread_number;
    LinkedBlockingQueue<String> queue;


    public Connection (Socket aClientSocket, int numero, LinkedBlockingQueue<String> fifo) {
        thread_number = numero;
        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            //out = new DataOutputStream(clientSocket.getOutputStream());
            queue = fifo;
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    //=============================
    public void run(){

        String resposta;
        try {

            //an echo server
            String data = in.readUTF();
            int i = 0;
            int length = data.length();

            String[] words = new String[length];
            StringTokenizer tokens = new StringTokenizer(data);
            while (tokens.hasMoreElements()) {

                words[i] = tokens.nextToken();
                if (words[i].equals("url")) {
                    String garbage = tokens.nextToken();
                    String Url = tokens.nextToken();
                    if (!queue.contains(Url)) {
                        //System.out.println("T[" + thread_number + "] Recebeu: "+Url);
                        queue.add(Url);
                    }
                }
                i = i + 1;
            }

            //receber o index do cliente mas pode ser otimizado
            String[] aux =data.split(" ");
            if (aux.length == 2){

                String Url = aux[1];
                if (!queue.contains(Url)) {
                    //System.out.println("T[" + thread_number + "] Recebeu: "+Url);
                    queue.add(Url);
                }
            }

            System.out.println("queue contains " + queue);
        } catch(EOFException e) {
            System.out.println("EOF:" + e);
        } catch(IOException e) {
            System.out.println("IO:" + e);
        }
    }

}
