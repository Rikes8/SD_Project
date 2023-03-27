package src.Downloader;
import java.rmi.*;
public interface DownloaderInterface extends Remote {
    public String UpdateActiveDownloaders(String s) throws java.rmi.RemoteException;
}
