package com.example.demo.beans;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

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
            throw new RuntimeException(e);
        }
    }

}
