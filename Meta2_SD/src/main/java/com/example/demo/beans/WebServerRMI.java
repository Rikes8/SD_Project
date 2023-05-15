package com.example.demo.beans;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

import src.SearchModule.ServerInterface;

public class WebServerRMI{

    private ServerInterface w;

    public WebServerRMI() throws RemoteException {
        connection();
    }

    public void connection() throws RemoteException {
        try {
            this.w = (ServerInterface) LocateRegistry.getRegistry(4000).lookup("WEBSERVER");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getlinks() throws RemoteException {
        ArrayList<String> aux = new ArrayList<>();

        aux = w.ShareInfoToServer(0,"search");

        return aux;
    }

    public void index(String string) throws RemoteException {
        String aux = "";
        aux = aux + "index " + string;

        w.ShareInfoToServer(0,aux);

    }

}
