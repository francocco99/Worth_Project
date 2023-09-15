import java.rmi.*;
public interface InterfaceRegister extends Remote 
{
   int register(String nick,String psw)throws RemoteException;
   public  void registerForCallback(InterfaceClient ClientInterface,String name) throws RemoteException; 
}
