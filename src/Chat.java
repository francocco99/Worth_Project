
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Vector;



public class Chat implements Runnable
{
   private String name;
   private String multicast;
   private MulticastSocket ms;
   private DatagramSocket ds;
   private Integer port=6789;
   private InetAddress ia;
   private boolean ok=true;
   private MainClient c1;
   private Vector<String> message=new Vector<String>();

   public Chat(String namString,String multicast,MainClient c)
   {
      name=namString;
      this.multicast=multicast;
      c1=c;
      try
      {
         ms=new MulticastSocket(port);
         ia=InetAddress.getByName(this.multicast);
         ms.joinGroup(ia);
         ds=new DatagramSocket();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   public String getName()
   {
      return name;
   }

   // METODO PER LA LETTURA DEI MESSAGGI
   public void readmessage()
   {
      if(message.isEmpty())
      {
         System.out.println("< Non ci sono messaggi");
      }
      else
      {
         System.out.println("----------------------MESSAGE-----------------------");
         for(String s:message)
         {
            String[] function=s.split(":");     
            if(!function[0].equals(c1.getName())) //Il client non rilegge i messaggi che ha già inviato
            {
               System.out.println(s);
            }
            
         }
         message.clear();
         System.out.println("----------------------------------------------------");
      }
   }

   // METODO PER LA SCRITTURA
   public void sendmessage(String message)
   {
      byte [ ] buffer = new byte[1024*message.length()];
      buffer=message.getBytes();
      DatagramPacket dp=new DatagramPacket(buffer, buffer.length,ia,port);
      try
      {
         ds.send(dp);
      }
      catch(Exception e)
      {
        
      }
   }
   //METODO INVOCATO DAL CLIENT IN CASO DI LOGOUT O DI CHIUSURA FORZATA
   public void exit()
   {
      ok=false;
      ms.close();   
   }
   ///IL THREAD STA IN ASCOLTO SU QUEL PROGETTO
   public void run()
   {
      String s=null;
      while(ok)
      {
         try{
            byte [ ] buffer = new byte[8192];
            DatagramPacket dp=new DatagramPacket(buffer, buffer.length);
            ms.receive(dp);
            s=new String(dp.getData());
            String func[]=s.split(" ");
            // CASO IN CUI VIENE CANCELLATO IL PROGETTO E QUINDI IL CLIENT DEVE TERMINARE IL THREAD 
            if(func[0].equals("Server:Progetto"))
            {
               ok=false;//CANCEL PROJECT
               c1.closechat(name);
               
            }
            message.add(s);
         }
         catch(Exception e)
         {
            

         }
      }
      
   }
}
   

