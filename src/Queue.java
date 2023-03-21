package src;


import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Queue {
    private static int serverPort = 6000;

    //FIXME: QUEUE
    public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static void main(String args[]){


        int numero=0;

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            System.out.println("A escuta no porto 6000");
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
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

        try {
            //while(true){
                //an echo server
                String data = in.readUTF();
                System.out.println("T[" + thread_number + "] Recebeu: "+data);

                //add urls to linked list
                queue.add(data);
                System.out.println("queue contains " + queue);

                //out.writeUTF(data);
            //}
        } catch(EOFException e) {
            System.out.println("EOF:" + e);
        } catch(IOException e) {
            System.out.println("IO:" + e);
        }
    }

}
