import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import com.fasterxml.jackson.databind.ObjectMapper;



public class Producer implements Runnable {
   private Socket socket;
   private Server server;
   public Producer(Socket socket,Server s)
   {
      this.socket=socket;
      this.server=s;
   }
   public void run()
   {
      int result;
      ObjectMapper objectmapper= new ObjectMapper(); 
      System.out.print(socket.getLocalAddress()+" ");
      System.out.println(socket.getLocalPort());
      
      boolean online=true;
      String username=null;
      
      
      while(online)
      {
         try
         { 
            byte[] data=new byte[1024];
            InputStream is=socket.getInputStream();
            OutputStream os=socket.getOutputStream();
            int k=is.read(data); 
            String out= new String(data);
            if(k==-1){socket.close();}// -1 se il client è chiuso forzatamente
            
             System.out.println("COMANDO ARRIVATO AL SERVER:"+out);
            String function[]=out.split(" ");
            switch (function[0].trim().toLowerCase()) 
            {
            
               case "login":
                  result=server.login(function[1], function[2].trim());
                  os.write(result);
                  if(result==0)
                  {
                     username=function[1]; // MEMORIZZO IL NOME UTENTE PER IL CONTROLLO DEI PERMESSI
                  }
               break;

               case "logout":
                  result=server.logout(username);
                  os.write(result);
                  if(result==0)
                  {
                     username=null; 
                  }
                  else
                  {
                     System.out.println("logout non riuscita");
                  }
                  
               break;

               case "listprojects":                
                  Vector<String> Projects=server.listProjects(username);
                  os.write(objectmapper.writeValueAsBytes(Projects));
               break;

               case "createproject":
                  result=server.createProject(function[1].trim(),username);
                  os.write(result);     
               break;

               case "addmember":
                  result=server.addMember(function[1], function[2].trim(), username);
                  os.write(result);    
               break;

               case "showmembers":
                  Vector<String> Members= server.showMembers(function[1].trim(),username);
                  os.write(objectmapper.writeValueAsBytes(Members));
               break;

               case "showcards":
                  Vector<Card> cards=server.showCards(function[1].trim(),username);
                  os.write(objectmapper.writeValueAsBytes(cards));
               break;

               case "addcard":
                     String des[]=out.split("/");//GESTIONE DELLA DESCRIPTION
                     result=server.addCard(function[1].trim(), function[2].trim(),des[1].trim() ,username);
                     os.write(result);
               break;

               case "showcard":
                  Card card=server.showCard(function[1], function[2].trim(),username);    
                  os.write(objectmapper.writeValueAsBytes(card));
               break;

               case "movecard":
                  result=server.moveCard(function[1], function[2], function[3], function[4].trim(),username);
                  os.write(result);    
               break;

               case "getcardhistory":
                  Vector<String> History=server.getCardHistory(function[1], function[2].trim(),username);
                  os.write(objectmapper.writeValueAsBytes(History));
               break;

               case "cancelproject":
                  result=server.cancelProject(function[1].trim(),username);
                  os.write(result);
               break;
               
               case "exit":
                  socket.close();
                  online=false;
               break;
               
               default:
                     System.out.println("Comando non riconosciuto");
               break;

            }               
         }
         catch(SocketException e)
         {
            
            online=false;
            System.out.println("Connection close");
         }
         catch(Exception e)
         {
            e.printStackTrace();
         } 
      }
      
      System.out.println("Connection close----------------------------------");
      // IN CASO DI CHIUSURA FORZATA DEL CLIENT LOGOUT UTENTE
      if(username!=null)
      {
         System.out.println("UTENTE OFFLINE");
         server.logout(username);
      }
      System.out.println("PRODUCER TERMINATO");
   }  
}
