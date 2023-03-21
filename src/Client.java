package src;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Client {

    public static void main(String args[]) {

		/* This might be necessary if you ever need to download classes:
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		*/

        try {
            ClientServer h = (ClientServer) LocateRegistry.getRegistry(7000).lookup("benfica");

            String input = "url";
            h.sayHello(input);
            System.out.println("Mensagem enviada: " + input);
            //FIXME: receber input do terminal

        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }

    }

}