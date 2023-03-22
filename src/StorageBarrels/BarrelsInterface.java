package src.StorageBarrels;

import java.rmi.*;

public interface BarrelsInterface extends Remote{
    public String ShareInfoToBarrel(String s) throws java.rmi.RemoteException;
}
