package src.StorageBarrels;

import java.rmi.*;
import java.util.ArrayList;

public interface BarrelsInterface extends Remote{
    public ArrayList<String> ShareInfoToBarrel(String s) throws java.rmi.RemoteException;
}
