import java.rmi.*;
import java.util.HashMap;
public interface InterfaceClient extends Remote 
{
   /*metodi esportati dal client utilizzati dal server per la notifica */
   public void notifyEvent(HashMap<String,Integer> h ) throws RemoteException;
   public void notifyInd(HashMap<String,String> ind) throws RemoteException;
   public void notifynewInd(String name,String ind) throws RemoteException;
    
}
