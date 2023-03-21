package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.Scanner;

public class SearchModule extends UnicastRemoteObject implements ClientServer {

    private static int serversocket = 6000;



    private static final long serialVersionUID = 1L;

    public SearchModule() throws RemoteException {
        super();
    }



    //FIXME: RMI troca de mensagens cleint - seacrhModule
    public String exchangeInfo(String s) throws RemoteException {
        System.out.println("Servidor recebeu: " + s);

        // 1o passo - criar socket
        try (Socket socket = new Socket("localhost", serversocket)) {
            //System.out.println("SOCKET=" + s);

            // 2o passo
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // 3o passo
            try (Scanner sc = new Scanner(System.in)) {
                //while (true) {
                    // READ STRING FROM KEYBOARD
                    //String texto = sc.nextLine();

                    // WRITE INTO THE SOCKET
                    out.writeUTF(s);

                    // READ FROM SOCKET
                    //String data = in.readUTF();

                    // DISPLAY WHAT WAS READ
                    //System.out.println("Received: " + data);
                //}
            }

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }

        return "URL indexado!";
    }

    // =========================================================
    public static void main(String args[]) {

        try {
            SearchModule h = new SearchModule();
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("port", h);
            System.out.println("SearchModule is ready!"); //FIXME: SearchModel iniciou

        } catch (RemoteException re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

}
