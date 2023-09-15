
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class MainServer {
   public static void main(String[] args) {
      ////REGISTER
      
      int port1=40000;
      int port2=50000;
      
      Server statsService=new Server();
      try{
         InterfaceRegister stub=(InterfaceRegister) UnicastRemoteObject.exportObject(statsService,0);
         LocateRegistry.createRegistry(port1);
         Registry r=LocateRegistry.getRegistry(port1);
         r.rebind("SERVER", stub);
      }
      catch(Exception e)
      {
         System.out.println("Error:impossibile avviare il Server");
      }
       
      //// APERTURA TCP
      try(ServerSocket socket=new ServerSocket(port2))
      {
         ThreadPoolExecutor pool=(ThreadPoolExecutor)Executors.newCachedThreadPool();
         System.out.println("SERVER IN ATTESA DI COMANDI");
         while(true)
         {   
            pool.execute(new Producer(socket.accept(),statsService));   
            //PER IL MONITORAGGIO
            System.out.println("NUOVA CONNESSIONE:");
            System.out.printf("Server:Pool Size:%d\n",pool.getPoolSize());
            System.out.printf("Server:Active Count:%d\n",pool.getActiveCount());
            System.out.printf("Server:Completed Tasks:%d\n",pool.getCompletedTaskCount());  
         }
      }
      catch(Exception e)
      {
         System.out.println("Error:impossibile avviare il server");
      }
   }
   
}
