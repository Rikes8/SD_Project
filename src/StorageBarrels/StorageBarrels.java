package src.StorageBarrels;

import src.SearchModule.ServerInterface;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

//TODO:=====================ALERT!!!===============================
//downloader avisa que vai enviar pacote comn bytes, barrelsavisam quer receberem e depois acks

public class StorageBarrels extends  UnicastRemoteObject implements BarrelsInterface {

    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;

    StorageBarrels() throws RemoteException {
        super();
    }

    //FIXME: RMI Callback -> INPUT QUE VEM DO CLIENTE E QUE REQUERE IR BUSCAR INFO AO BARREL
    public String ShareInfoToBarrel(String s) throws RemoteException {
        //System.out.println(">> " + s);

        //FIXME:SO PARA TESTAR
        String[] teste = {"teste" , "http://uc.pt" , "teste2", "http://sapo.pt"};
        if (s.equals(teste[0])){
            //System.out.println("in");
            return teste[1];
        }
        if (s.equals(teste[2])){
            //System.out.println("in");
            return teste[3];
        }

        return "RECEBER INFO DO BARREL";
    }

    public static void main(String args[]) throws IOException, NotBoundException {
        // usage: java HelloClient username
        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        //FIXME: Multicast thread

        String nome = "localhost";
        ServerInterface h = (ServerInterface) LocateRegistry.getRegistry(8000).lookup("BARREL");
        StorageBarrels b = new StorageBarrels();
        h.subscribe_barrel(nome, (BarrelsInterface) b);
        System.out.println("Barrel sent subscription to server");


        MulticastSocket multicast_socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        multicast_socket.joinGroup(group);
        Thread multicast = new Thread(() -> {
            try {
                while (true) {
                    //recieve handshake
                    byte[] buffer = new byte[1024]; //FIXME: PROBLEMA AQUI!!!! tamanho
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        multicast_socket.receive(packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("Received handshake from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    String size = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(size);


                    byte[] sms_buffer = new byte[Integer.parseInt(size)];
                    DatagramPacket sms_packet = new DatagramPacket(sms_buffer, sms_buffer.length);
                    try {
                        multicast_socket.receive(sms_packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Received message packet from " + sms_packet.getAddress().getHostAddress() + ":" + sms_packet.getPort() + " with message:");
                    String sms = new String(sms_packet.getData(), 0, sms_packet.getLength());
                    System.out.println(sms);

                    /*
                    //send ack
                    byte[] ack = new byte[1];
                    ack[0] = 1;
                    DatagramPacket ack_packet = new DatagramPacket(ack, ack.length, group, PORT);
                    try {
                        System.out.println("entrei!!!");
                        multicast_socket.send(ack_packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/


                    //FIXME: Tratar da mensagem e meter para o hashmap
                }

            } finally {
                multicast_socket.close();
            }
        });
        multicast.start();



        //FIXME: RMI com SearchModule
        String ClientInput;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true){
            try {

                System.out.print("> ");
                ClientInput = reader.readLine();
                h.ShareInfoToServer(nome, ClientInput);
            } catch (Exception e) {
                System.out.println("Exception in main: " + e);
            }
        }



    }

}

