import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class Server extends RemoteServer implements  InterfaceRegister {
  
   
   private HashMap<String,String> Utenti; // UTENTI + PASSWD
   private HashMap<String,Integer>UtentiStato; // UTENTI + STATO
   private HashMap<String,String>ProgettiInd; //PROGETTI + INDIRIZZI MULTICAST
   private HashMap<String,InterfaceClient> Stubs; // UTENTI + STUBS PER RMI
   private Vector<Progetto> Progetti; // LISTA DEI PROGETTI
   int udp=6789;
   // FILE PER LA SERIALIZZAZIONE
   File j;
   File ind;
   File temp;

   ObjectMapper obj;
   String lastMulti="224.0.0.0"; //INDIRIZZO ULTICAST INIZIALE
   public Server()
   {
      Progetti=new Vector<Progetto>(); 
      ProgettiInd=new HashMap<String,String>(); 
      Utenti=new HashMap<String,String>();
      UtentiStato=new HashMap<String,Integer>();
      Stubs=new HashMap<String,InterfaceClient>();

      obj=new ObjectMapper();
      File dir = new File("Worthrsc"); //CARTELLA DOVE CI SARà TUTTO
      j=new File(dir.getName()+"/Register.json");
      ind=new File(dir.getName()+"/MulticastInd.json");

      temp=new File(".");////FILE UTILIZZATO PER SCORRERE LA DIRECTORY
      try
      {
         if(!dir.exists()) 
         {
            dir.mkdirs();
         }
         else
         {
            /// DESERIALIZZAZIONE PER UTENTI E INDIRIZZI MULTICAST
            if(j.exists()){Utenti=obj.readValue(j, new TypeReference<HashMap<String,String>>(){});}
            if(ind.exists()){ProgettiInd=obj.readValue(ind,new TypeReference<HashMap<String,String>>(){} );}
            for(String s: Utenti.keySet())
            {
               UtentiStato.put(s, 0);
            }
            File[] files=temp.listFiles();
            for(File file: files)
            {
               if(file.getName().equals("Worthrsc") && file.isDirectory())
               {
                  for(File file2 : file.listFiles())
                  {
                     if(file2.isDirectory())
                     {
                        Progetto NewProg=new Progetto(file2.getName());
                        NewProg.Restore();
                        Progetti.add(NewProg);
                     }
                  }
               }       
            }
            //SI RECUPERA L'ULTIMO INDIRIZZO MULTICAST ASSOCIATO AD UN PROGETTO
            if(!ProgettiInd.isEmpty())
            {
               Iterator<Map.Entry<String,String>> iter =ProgettiInd.entrySet().iterator();
               Map.Entry<String,String> entry = null;
               while(iter.hasNext()) 
               {
                  entry = iter.next();
               }
                  lastMulti=entry.getValue();
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   ///REGISTRAZIONE DEGLI UTENTI
   public synchronized int register(String nick,String psw)
   {
      
      if(psw.equals("") || nick.equals("")){return 1;}
      if(Utenti.containsKey(nick)){return 0;} // utente non registrto
      else
      {
         try
         {                   
            Utenti.put(nick,psw);
            UtentiStato.put(nick,0);
            obj.writeValue(j,Utenti);
            System.out.println("UTENTE: "+nick+" REGISTRATO\n");
         }
         catch(Exception e)
         {
            return 0;
         }
         return 2;

      }
   }

   // REGISTER FOR CALLBACK 
   public synchronized void registerForCallback(InterfaceClient ClientInterface,String name) throws RemoteException
   {
      if(!Stubs.containsValue(ClientInterface))
      {
         Stubs.put(name,ClientInterface);
      }
      HashMap<String,String> temp=control(name);
      ClientInterface.notifyInd(temp); //NOTIFICA INDIRZZI CHAT
      doCallbacks();
          
   }

  
   //LOGIN
   public synchronized int login(String nick,String psw)
   {
      if(psw.equals("") || nick.equals("")){return 2;}
      if(!Utenti.containsKey(nick)){return 3;} // utente non registrato
      if(UtentiStato.get(nick)==1){return 4;} // utente già online
      String psw2=Utenti.get(nick);

      if(psw2.equals(psw.trim()))
      {
         UtentiStato.replace(nick,1);
         System.out.println("STATO UTENTI REGISTRATI:"+UtentiStato);
         return 0;
      }
      return 3; //psw errata
      
   }

   //AGGIORNAMENTO LISTA LOCALE  DEL CLIENT 
   private synchronized void doCallbacks( )throws RemoteException
   { 
     for(String s: Stubs.keySet())
      {
         InterfaceClient client =(InterfaceClient) Stubs.get(s);      
         client.notifyEvent(UtentiStato);
      }
   }

   


   ///LOGOUT UTENTI
   public synchronized int logout(String nick)
   {
      if(UtentiStato.containsKey(nick))
      {
         UtentiStato.replace(nick,0);
         try
         {
            Stubs.remove(nick);
            doCallbacks();
            return 0;
         }
         catch(Exception e)
         {
            return 1;
         }
      }
      return 1;
   }

   ////LIST USERS
   public HashMap<String,Integer> listUsers()
   {
      return UtentiStato;
   }

   ////RESTITUISCE I PROGETTI  DI CUI L'UTENTE è MEMBRO
   public Vector<String> listProjects(String name)
   {
      Vector<String> Utprogetti=new Vector<String>();
      for(Progetto p: Progetti)
      { 
         if(p.Ismember(name))
         {
            Utprogetti.add(p.getName());
         }
      }
      System.out.println("PROGETTI:"+Utprogetti);
      return Utprogetti;
   }

   ///CREA UN PROGETTO
   public synchronized int createProject(String ProjectName,String name)
   { 
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(ProjectName))
         {
            return 1;
         }
      }
         try{
            Progetto NewProg=new Progetto(ProjectName);
            genera();//CAMBIA IL VALORE DI LASTMULTI 
            ProgettiInd.put(ProjectName, lastMulti);//ASSEGNO UN INDIRIZZO MULTICAST
            
            obj.writeValue(ind,ProgettiInd);
            NewProg.serial();//SERIALIZZO
            NewProg.addMember(name);//AGGIUNGO IL CREATORE DEL PROGETTO COME MEMBRO
            Progetti.add(NewProg);
            
            Stubs.get(name).notifynewInd(ProjectName,ProgettiInd.get(ProjectName));//NOTIFICO AL CLIENT DI COLLEGARSI ALLA CHAT 
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
         return 0;
   }
   //RESTITUISCE I MEMBRI
   public Vector<String> showMembers(String ProjectName,String name)
   {
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(ProjectName))
         {
            if(p.Ismember(name))
            {
               return p.getMember();
            }
            else
            {
               return null;
            }
            
         }
      }
      return null;
   }

   //RESTITUISCE LE CARD
   public Vector<Card> showCards(String ProjectName,String name)
   {
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(ProjectName))
         {
            if(p.Ismember(name))
            {  
               return p.getCards();
            }
            else
            {
               return null;
            }
         }
      }
      return null;
   }

   //RESTITUISCE UNA SPECIFICA CARD
   public Card showCard(String ProjectName,String cardName,String name)
   {
     Vector<Card> tempCard=showCards(ProjectName,name);
      if(tempCard!=null)
      {
         for(Card c: tempCard)
         {
            if(c.getName().equals(cardName))
            {

               return c;
            }
         }
      }
      return null;
   }

   //AGGIUNGE LA CARD
   public int addCard(String ProjectName,String cardName,String Description,String name)
   {
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(ProjectName))
         {
            if(p.Ismember(name))
            {
               int k=p.addCard(cardName, Description);
               if(k==0) 
                  this.Messagechat(name+" ha aggiunto la card "+cardName, ProgettiInd.get(ProjectName));
               return k;
            }
            else
            {
               return 2; // utente non membro
            }
         }
      }
      return 3; // progetto non esistente
   }

   //MUOVE LE CARD
   public int moveCard(String ProjectName,String cardName,String listaPartenza,String listaDestinazione,String name)
   {
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(ProjectName))
         {
            if(p.Ismember(name))
            {  
               int r=p.moveCard(cardName,listaPartenza.toUpperCase(),listaDestinazione.toUpperCase());
               if(r==0) this.Messagechat(name+" ha spostato la card "+cardName+" nella lista "+listaDestinazione, ProgettiInd.get(ProjectName));
               
               return r;
            }
            else
            {
               return 2;
            }
         }
      }
      return 1;
   }
   
   //RESTITUISCE LA STORIA DI UNA CARD
   public Vector<String> getCardHistory(String projectName, String cardName,String name)
   {  
      Vector<Card> tempCard=showCards(projectName,name);
      if(tempCard!=null)
      {
         for(Card c: tempCard)
         {
            if(c.getName().equals(cardName))
            {
               return c.getHistory();
            }
         }
      }
      return null;
   }
   //RESTITUSICE UN HASHMAP DI TUTTE LE COPPIE PROGETTO INDIRIZZO DI CUI L' UTENTE é MEMEBRO
   public synchronized HashMap<String,String>  control(String name)
   {
      HashMap<String,String> temp= new HashMap<String,String>(); 
      for(Progetto p: Progetti)
      {
         if(p.Ismember(name))
         {  
            temp.put(p.getName(),ProgettiInd.get(p.getName()));
         }
      }
      return temp;
   }
   //ElIMINA UN PROGETTO
   public  synchronized int cancelProject( String projectName,String name)
   {
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(projectName)  ) 
         {
            if(p.Ismember(name)) 
            {
               if(p.Control())// CONTROLLA ANCHE LA CLASUSOLA DELLE CARD PER LA CANCELLAZIONE
               {  
                  String inds=ProgettiInd.get(projectName);
                  Progetti.remove(p);  
                  ProgettiInd.remove(projectName);
                  File temp=new File("Worthrsc/"+projectName);
                  //ELIMINAZIONE FILE PER LA PERSISTENZA (CARTELLA) 
                  for(File f: temp.listFiles())
                  {
                     f.delete();
                  }
                  temp.delete();
                  try{obj.writeValue(ind,ProgettiInd);} // AGGIORNO IL FILE DEGLI INDIRIZZI MULTICAST
                  catch(Exception e){}
                  Messagechat("Progetto "+projectName+" eliminato da "+name,inds);
                  return 0;
               }
               else
               {
                  return 3;      //clausola eliminazione non verificata      
               }
            }
            else
            {
               return 2;
            }
         }
        
      }
      return 1;
   }

   //AGGIUNGE UN MEMBRO
   public synchronized int addMember(String projectName,String NickUtente,String name)
   {
      for(Progetto p: Progetti)
      {
         if(p.getName().equals(projectName))
         {
            if(Utenti.containsKey(NickUtente))
            {
               if(p.Ismember(name))
               {
                  if(p.getMember().contains(NickUtente)){return 4;} //UTENTE già aggiunto
                  p.addMember(NickUtente.trim());
                  try
                  {
                     if(UtentiStato.get(NickUtente)==1) //CONTROLLO SE L'UTENTE AGGIUNTO é ONLINE
                     {
                        InterfaceClient c=Stubs.get(NickUtente);
                        c.notifynewInd(projectName,ProgettiInd.get(projectName));//COMMUNICO ALL'UTENTE APPENA AGGIUNTO DI COLLEGARSI ALLA CHAT ONLINE
                     }
                  }
                  catch(Exception e)
                  {
                     e.printStackTrace();
                  }
                  this.Messagechat(name+" ha aggiunto "+NickUtente+" come membro", ProgettiInd.get(projectName));
                  return 0;
               }
               else
               {
                  return 3; //utente non membro
               }
            }
            else
            {
               return 2; //nuovoutente non registrato alla piattaforma
            }
         }
      }
      return 1; //progetto non trovato
      
   }

   public synchronized void Messagechat(String msg,String ind)//METODO PER INVIARE AGGIORNAMENTI ALLA CHAT 
   { 
      try(DatagramSocket ds=new DatagramSocket())
      {
         System.out.println(ind);
         byte[] buffer=new byte[8192];
         buffer=("Server:"+msg).getBytes();
        
         InetAddress ia=InetAddress.getByName(ind);
         DatagramPacket dp=new DatagramPacket(buffer,buffer.length,ia,udp);
         ds.send(dp);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   //GENERA IL NUOVO INDIRIZZO MULTICAST
   public void genera()
   {
      int int1,int2,int3,int4;
      String temp[]=lastMulti.split("\\.");
      
      int1=Integer.parseInt(temp[0]);
      int2=Integer.parseInt(temp[1]);
      int3=Integer.parseInt(temp[2]);
      int4=Integer.parseInt(temp[3]);
      if(int4<255)
      {
         int4++;
      }
      else 
      if(int3<255)
      {
         int4=0;
         int3++;
      }
      else 
      if(int2<255)
      {
         int4=0;
         int3=0;
         int2++;
      }
      else 
      if(int1<239)
      {
         int4=0;
         int3=0;
         int2=0;
         int1=224;
      }
      lastMulti=int1+"."+int2+"."+int3+"."+int4;
      if(ProgettiInd.containsValue(lastMulti))
      {
         genera();
      }
   }
   
}
