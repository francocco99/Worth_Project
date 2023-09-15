import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;



public class MainClient extends RemoteObject implements InterfaceClient
{
   
   HashMap<String,Integer> listUsers=new HashMap<String,Integer>(); //LISTA DEGLI UTENTI 
   HashMap<String,Integer> listOnlineusers=new HashMap<String,Integer>(); //LISTA DEGLI UTENTI ONLINE
   HashMap<String,String> ProjectsInd=new HashMap<String,String>(); //HASHMAP NOMEPROGETTO E INDIRIZZO MULTICAST
   Vector<Chat> chats=new Vector<Chat>(); 
   ThreadPoolExecutor pool=(ThreadPoolExecutor)Executors.newCachedThreadPool();//threadPool per chat
   Socket client;
   String username;
   int out;
   
     
   public static void main(String[] args) 
   {
      MainClient client=new MainClient();
      client.Start();
      System.exit(0);
     
   }

   public void Start() 
   {

      int portTCP=50000;
      boolean log=false;
      InterfaceRegister s=null;
      Remote RemoteObject;
      ObjectMapper objectMapper=new ObjectMapper();
      username=null;
      InterfaceClient stub=null;
      boolean chiuso=true;
       
      try(Scanner in=new Scanner(System.in);)
      {
         InetAddress local=InetAddress.getLocalHost();
         client= new Socket(local,portTCP);

         //STREAM PER SCRIVERE SULLA SOCKET
         InputStream is=client.getInputStream();
         OutputStream os=client.getOutputStream();        

         ///SETUP RMI PER REGISTER
         Registry r=LocateRegistry.getRegistry(40000);
         RemoteObject=r.lookup("SERVER"); 
         s=(InterfaceRegister)RemoteObject;
         
         ///CALLBACK
         InterfaceClient callbackObj= this;
         stub=(InterfaceClient)UnicastRemoteObject.exportObject(callbackObj,0);
        
         System.out.println("UTENTE CONNESSO\n help-> informazioni sui comandi\n exit-> Per uscire dal programma\n----------------------------------------------------\n ");
         while(chiuso)
         {
           
            System.out.print("> ");
            String sin=in.nextLine();
            String[] function=sin.split(" ");
            
            switch(function[0].toLowerCase())
            {
               case "register":
               if(function.length!=3)
               {
                  System.out.println(" Error:Argument");                           
               }
               else
               {
                  int k=s.register(function[1], function[2].trim());

                  System.out.print("< ");
                  if(k==2)
                  {
                     System.out.println("Register ok");
                  }
                  if(k==0)
                  {
                     System.out.println("Register failed, username già esistente");
                  }
                  if(k==1)
                  {
                     System.out.println("Register failed");
                  }
               }        
               break;

               case "login":
                  System.out.print("< ");
                  if(log==false)
                  {
                     if(function.length!=3)
                     {
                        System.out.println(" Error:Argument");                           
                     }
                     else
                     {         
                        os.write(sin.getBytes()); 
                        out=is.read();
                        if(out==-1){throw new IOException();}// caso chiusura del server
                        if(out==0)
                        {      
                           s.registerForCallback(stub,function[1]);
                           username=function[1];                
                           System.out.println("login effettuato");
                           log=true;
                        }
                        if(out==2 || out==3)
                        {
                           System.out.println("password o username errati");
                        }
                        if(out==4)
                        {
                           System.out.println("Utente già Online");
                        }
                     }
                  }
                  else
                  {
                     System.out.println("Error: effettuare prima il logout");
                  }   
               break;

               case "logout":
                  System.out.print("< ");
                  if(log==true)
                  {
                     if(function.length!=1)
                     {
                        System.out.println("Error:Argument");                           
                     }
                     else
                     {
                        os.write(sin.getBytes());
                        out=is.read();
                        if(out==-1){throw new IOException();}// caso chiusura del server
                        if(out==0)
                        {
                           log=false;
                           System.out.println("logout effettuato");
                           //TERMINO I THREAD DELLE CHAT
                           for(Chat c:chats)
                           {
                              c.exit();
                           }
                           chats.clear();
                        }
                        else
                        {
                           System.out.printf("logout fallito");
                        }
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");  
                  }
               break;

               case "listusers":
                  if(log==true)
                  {
                     if(function.length!=1)
                     {
                        System.out.println("< Error:Argument");   
                     }
                     else
                     {
                        listusers();  
                     }
                  }
                  else
                  {
                     System.out.println("< Error:devi fare prima il login");
                  } 
               break;

               case "listonlineusers":
               
                  if(log==true)
                  {
                     if(function.length!=1)
                     {
                        System.out.println("< Error:Argument");     
                     }
                     else
                     {
                        listonlineusers();
                     }
                  }
                  else
                  {
                     System.out.println("< Error:devi fare prima il login");
                  } 
               break;

               case "listprojects":
                  if(log==true)
                  {
                     if(function.length!=1)
                     {
                        System.out.println("< Error:Argument");
                     }
                     else
                     {
                        os.write(sin.getBytes());
                        byte b[]=new byte[1024*8];
                        out=is.read(b);
                        if(out==-1){throw new IOException();}// caso chiusura del server
                        Vector<String> projects=objectMapper.readValue(b, new TypeReference<Vector<String>>(){});
                        if(projects.isEmpty())
                        {
                            System.out.println("< L'Utente non appartiene a nessun progetto");
                        }
                        else
                        {
                           System.out.println("----------------------PROGETTI----------------------");                
                           for(String p: projects)
                           {
                              System.out.println(p);
                           }
                           System.out.println("----------------------------------------------------");
                        }
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  } 
               break;

               case "createproject":
                  System.out.print("< ");
                  if(log==true)
                  {
                     if(function.length!=2)
                     {
                        System.out.println("Error:Argument");
                     }
                     else{
                        os.write(sin.getBytes());
                        out=is.read();  
                        if(out==-1){throw new IOException();}// caso chiusura del server
                        if(out==0)
                        {
                           System.out.println("Progetto creato correttamente");
                        }
                        if(out==1)
                        {
                           System.out.println("Nome progetto già esistente");
                        }
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  } 
               break;

               case "addmember":
                  System.out.print("< ");
                  if(log==true)
                  {
                     if(function.length!=3)
                     {
                        System.out.println("Error Argument");
                     }
                     else
                     {  
                        os.write(sin.getBytes());
                        out=is.read();
                        if(out==-1){throw new IOException();}// caso chiusura del server
                        if(out==0)
                        {
                           System.out.println("Utente aggiunto correttamente");
                        }
                        if(out==1)
                        {
                           System.out.println("Progetto non esistente");
                        }
                        if(out==3)
                        {
                           System.out.println("L'utente "+username+" non è membro del progetto");;
                        }
                        if(out==2)
                        {
                           System.out.println(function[2]+" non è registrato alla piattaforma");
                        }
                        if(out==4)
                        {
                           System.out.println("L'utente è già membro");
                        }
                     }
                  }
                  else 
                  {
                     System.out.println("Error:devi fare prima il login");    
                  }
               break;

               case "showmembers":
                  if(function.length!=2)
                  {
                     System.out.println(" Error:Argument");  
                  }
                  else
                  {                         
                     os.write(sin.getBytes());
                     byte b[]=new byte[1024*4];
                     out= is.read(b);

                     if(out==-1){throw new IOException();}// caso chiusura del server
                     Vector<String> members=objectMapper.readValue(b, new TypeReference<Vector<String>>(){});
                     if(members!=null)
                     {
                        System.out.println("-----------------------MEMBRI-----------------------");
                        
                        for(String m: members)
                        {
                           System.out.println(m);
                        }
                        System.out.println("----------------------------------------------------");    
                     }
                     else
                     {
                        System.out.println("< Progetto non esistente o utente non membro");
                     }
                  }
               break;

               case "showcards":
                  if(function.length!=2)
                  {
                     System.out.println("Error:Argument");     
                  }
                  else
                  {       
                     os.write(sin.getBytes());
                     byte b[]=new byte[1024*4];
                     out=is.read(b);        
                     if(out==-1){throw new IOException();}// caso chiusura del server
                    
                     Vector<Card> cards=objectMapper.readValue(b, new TypeReference<Vector<Card>>(){});
                     if(cards!=null)
                     {
                        if(cards.isEmpty())
                        {
                           System.out.println("< Non ci sono card all'interno del progetto");
                        }
                        else
                        {
                           System.out.println("-------------------------CARD-----------------------");
                           
                           for(Card c: cards)
                           {
                              System.out.println("NAME: "+c.getName()+", DESCRIPTION: "+c.getDescription()+", STATO:"+c.getLista());
                           }
                           System.out.println("----------------------------------------------------");
                        }
                        
                     }
                     else
                     {
                        System.out.println("< Progetto non esistente o utente non membro");
                     }
                     //LETTURA
                  }
               break;

               case "addcard":          
                  if(log==true)
                  {
                     if(function.length!=3)
                     {
                        System.out.println("< Error:Argument");                      
                     }
                     else{
                        System.out.println(" INSERIRE DESCRIZIONE Card:");
                        String des=in.nextLine();
                        os.write((sin+" /"+des).getBytes());
                        out=is.read();
                        if(out==-1){throw new IOException();}
                        System.out.print("< ");
                        if(out==0)
                        {
                           System.out.println("Card aggiunta correttamente");
                        }
                        if(out==1)
                        {
                           System.out.println("Nome Card già esistente, inserire un altro nome");
                        }
                        if(out==2)
                        {
                           System.out.println("L'utente "+username+" non è membro del progetto");
                        } 
                        if(out==3)
                        {
                           System.out.println("Progetto non esistente");
                        }
                        
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  }    
               break;

               case "showcard":
                  
                  if(log==true)
                  {
                     if(function.length!=3)
                     {
                        System.out.println("< Error:Argument");
                        
                     }
                     else{                        
                        os.write(sin.getBytes());
                        byte b[]=new byte[1024*4];
                        out=is.read(b);
                        if(out==-1){throw new IOException();}// caso chiusura del server
                       
                        Card card=objectMapper.readValue(b, new TypeReference<Card>(){});
                        if(card==null)
                        {
                           System.out.println("< Error:card non esistente o utente non membro");
                        }
                        else
                        {
                           System.out.println("-------------------------CARD-----------------------");
                           System.out.println("NAME:"+card.getName()+", DESCRIPTION:"+card.getDescription()+", STATO:"+card.getLista());
                           System.out.println("----------------------------------------------------");
                        }
                     }
                  }
               break;

               case "movecard":
                  System.out.print("< ");
                  if(log==true)
                  {
                     if(function.length!=5)
                     {
                        System.out.println("Error:Argument");                
                     }
                     else
                     {
                        os.write(sin.getBytes());
                        out=is.read();
                        if(out==-1){throw new IOException();}// caso chiusura del server
                         
                        if(out==0)
                        {
                           System.out.println("Card spostata corettamente");
                        }
                        if(out==1)
                        {
                           System.out.println("Progetto non esistente");
                        }
                        if(out==2)
                        {
                           System.out.println("L'utente "+username+" non è membro del progetto");
                        }
                        if(out==3)
                        {
                           System.out.println("Impossibile spostare nella lista di destinazione specificata");
                        }
                        if(out==4)
                        {
                           System.out.println("Card non presente nella lista");
                        }
                        
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  }
               break;

               case "getcardhistory":
                  if(log==true)
                  {
                     if(function.length!=3)
                     {
                        System.out.println("< Error:Argument");
                     }
                     else{
                        os.write(sin.getBytes());
                        byte b[]=new byte[1024*4];
                        out=is.read(b);
                        if(out==-1){throw new IOException();} // caso chiusura del server
                       Vector<String> hist=objectMapper.readValue(b, new TypeReference<Vector<String>>(){});
                        if(hist==null){System.out.println("< Error:card non esistente o utente non membro ");}
                        else
                        {  
                           System.out.println("--------------------HISTORYCARD---------------------");
                           for(String h: hist)
                           {
                              System.out.println(h);
                           }
                           System.out.println("----------------------------------------------------");
                        }   
                     }
                  }
                  else 
                  {
                     System.out.println("Error:devi fare prima il login");
                  }
               break;

               case "readchat":
                  if(log==true)
                  {
                     boolean ok=false;
                     if(function.length!=2)
                     {
                        System.out.println("< Error:Argument");
                     }
                     else
                     {
                        for(Chat c: chats)
                        {
                           if(c.getName().equals(function[1]))
                           {
                              ok=true;
                              c.readmessage();      
                           }
                        }
                        if(ok==false)
                        {
                           System.out.println("< Progetto non trovato");
                        }
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  }
               break;

               case "sendchatmsg":
                  if(log==true)
                  {
                     boolean ok;
                     if(function.length!=2)
                     {
                        System.out.println("< Error:Argument");
                     }
                     else
                     {
                        ok=false;
                        String message;
                        System.out.println("Inserisci il messaggio:");
                        message=in.nextLine();
                        message=username+":"+message;
                        for(Chat c: chats)
                        {
                           if(c.getName().equals(function[1]))
                           {
                              ok=true;
                              c.sendmessage(message);      
                           }
                        }
                        System.out.print("< ");
                        if(ok==false)
                        {
                           System.out.println("Progetto non trovato");
                        }
                        else
                        {
                           System.out.println("Messaggio inviato");
                        }
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  }   
               break;

               case "cancelproject":
                  if(log==true)
                  {
                     System.out.print("< ");
                     if(function.length!=2)
                     {
                        System.out.println("Error:Argument");
                     }
                     else
                     {
                        os.write(sin.getBytes());
                        out=is.read();
                        if(out==-1){throw new IOException();}
                        //LETTURA  
                        if(out==0)
                        {
                           System.out.println("Progetto eliminato corettamente");

                        }
                        if(out==1)
                        {
                           System.out.println("Progetto non esistente");
                        }
                        if(out==2)
                        {
                           System.out.println("L'utente "+username+" non è membro del progetto");
                        }
                        if(out==3)
                        {
                           System.out.println("il progetto non può essere eliminato");
                        }
                        
                     }
                  }
                  else
                  {
                     System.out.println("Error:devi fare prima il login");
                  }
               break;

               case "help":
                  System.out.println("----------------------------------------------------");
                  System.out.print("COMANDI: \n\r register 'nomeutente' 'password'->registra un nuovo utente alla piattaforma\n\r login 'nomeutente 'password'-> effettua il login con quel nome utente \n\r logout \n\r createproject 'nomeprogetto'-> crea un nuovo progetto \n\r addcard 'nomeprogetto' 'nomecard'->aggiunge una nuova card al progetto\n\r addmember 'nomeprogetto' 'nomemembro'-> aggiunge un nuovo membro al progetto\n\r movecard 'nomeprogetto' 'nomecard' 'listadipartenza' 'listadiarrivo'->sposta la card del progetto nella lista di arrivo specificata \n\r cancelproject 'nomeprogetto'-> cancella il progetto indicato \n\r showmembers 'nomeprogetto'-> mostra i membri di quel progetto \n\r showcards 'nomeprogetto'-> mostra le card di quel progetto \n\r showcard 'nomeprogetto' 'nomecard'-> mostra le informazioni di una card appartenente a quel progetto \n\r getcardhistory 'nomeprogetto' 'nomecard'-> restituisce la history della card specificata \n\r listusers-> restituisce la lista degli utenti registrati alla piattaforma \n\r listonlineusers-> restituisce la lista degli utenti online \n\n\r COMANDI CHAT:\n sendchatmsg 'nomeprogetto'-> manda un messaggio sulla chat di progetto specificata \n\r readchat 'nomeprogetto'-> permette di leggere i messaggi relativi alla chat di progetto specificata\n ");
                  System.out.println("----------------------------------------------------");   
               break;

               case "exit":
                  chiuso=false;
                  System.out.println("CHIUSO");
               break;

               default:
                  System.out.println("< Comando non riconosciuto");

            }
            
         }
      }
      catch(NoSuchElementException e)
      {
         System.out.println("Client Terminato");
      }
      catch(ConnectException e)
      {
         System.out.println("Il server non risponde");
      }
      catch(RemoteException e)
      {
         System.out.println("Il server non risponde");
      }
      catch(IOException e)
      {
         System.out.println("Il server non risponde + I/O");
      }
      catch(Exception e)
      {
        
      }
   }

/////METODO PER ASCOLTO DELLE CHAT
   public  void listen()
   {
      for(String s: ProjectsInd.keySet())
      {
         Chat chat=new Chat(s,ProjectsInd.get(s),this);
         pool.execute(chat);
         chats.add(chat);
      }
   }
   ////METODO PER NOTIFICARE GLI INDIRIZZI MULTICAST
   public void notifyInd(HashMap<String,String> s) throws RemoteException
   {
         ProjectsInd=s;
         this.listen();
   }
   ////METODO PER NOTIFICARE UN NUOVO INDIRIZZO MULTICAST ASSOCIATO AD UN PROGETTO
   public void notifynewInd(String name,String ind) throws RemoteException
   {
      ProjectsInd.put(name, ind);
      Chat chat=new Chat(name,ind,this);
      pool.execute(chat);
      chats.add(chat);
         
   }
   
   ///METODO PER AGGIORNARE LA LISTA DI UTENTI
   public void notifyEvent(HashMap<String,Integer> h) throws RemoteException
   {         
      listUsers=h;
   }
   //METODO PER CHIUDERE LE CHAT IN CASO DI LOGOUT CHIAMATAO DALLA CLASSE CHAT
   public synchronized void closechat(String projectname)
   {  
      Chat temp=null;
      ProjectsInd.remove(projectname);
      for(Chat c:chats)
      {
         if(c.getName().equals(projectname))
         {
            temp=c;
            break;
         }
      }
      chats.remove(temp);

   }
   ////  LIST USERS  
   private void listusers()
   {
      Iterator it=listUsers.entrySet().iterator();
      System.out.println("-----------------------UTENTI-----------------------");
      
      while(it.hasNext())
      {
         Map.Entry entry=(Map.Entry)it.next();
         
         System.out.println("Utente: "+entry.getKey());
         if(entry.getValue().equals(1))
         {
            System.out.println("Stato: online\n");
         }
         else
         {
            System.out.println("Stato: offline\n");
         }
         
      }
      System.out.println("----------------------------------------------------");
   }
//// LIST ONLINE USERS  

   private void listonlineusers()
   {
      Iterator it=listUsers.entrySet().iterator();
      System.out.println("-------------------UTENTI-ONLINE--------------------");
      while(it.hasNext())
      {
         Map.Entry entry=(Map.Entry)it.next();
         
         if(entry.getValue().equals(1))
         {
            System.out.println("Utente: "+entry.getKey());
            System.out.println("Stato: online");
         }
      }
      System.out.println("----------------------------------------------------");

   }

   public String getName()
   {
      return username;
   }

}
