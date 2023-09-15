import java.io.File;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

public class Progetto {
	private Vector<Card> TODO;
	private Vector<Card> INPROGRESS;
	private Vector<Card> TOBEREVISED;
	private Vector<Card> DONE;
   private Vector<String> Member; 
  
   ObjectMapper obj;
   private File memb; //MEMBRI DEL PROGETTO
   private File j; //FILE PER SERIALIZZARE LE CARD
   private String Name;
	public  Progetto(String Name)
	{
      this.Name=Name;
      TODO=new Vector<>();
      INPROGRESS=new Vector<>();
      TOBEREVISED=new Vector<>();
      DONE=new Vector<>();
      Member=new Vector<String>();
      obj=new ObjectMapper();
      
   }
   //RICOSTRUZIONE DELLO STATO DELLA LISTA DEI MEMBRI E DELLE LISTE DELLE CARD
   public void Restore()
   {  
      try {
         File dir = new File("Worthrsc/"+this.Name);
         memb=new File("Worthrsc/"+Name+"/membri.json");
         if(memb.exists())
         {
            Member=obj.readValue(memb,new TypeReference<Vector<String>>(){});
            
         }
         File[]files=dir.listFiles();
         for(File f:files)
         {
            if(!f.equals(memb))
            {
               Card c=new Card();
               c=obj.readValue(f,new TypeReference<Card>(){});
               switch(c.getLista())
               {
                  case "TODO":
                     TODO.add(c);
                  break;

                  case "INPROGRESS":
                     INPROGRESS.add(c);
                  break;
                  
                  case "TOBEREVISED":
                     TOBEREVISED.add(c);
                  break;

                  case "DONE":
                     DONE.add(c);
                  break;

                  default:
                     System.out.println("RESTORE FALLITA");
                  break;
               }
            }
         } 
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   //RESTITUISCE IL NOME DEL PROGETTO
   public String getName()
   {
      return Name;
   }

	//METODO CHE AGGIUNGE UN NUOVO MEMBRO AL PROGETTO
	public void  addMember(String Utente)
	{
      if(!Member.contains(Utente))
      {
         Member.add(Utente);
         try
         {
            obj.writeValue(memb,Member);
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }       
      }
	}
	//METODO CHE RESTITUISCE I MEMBRI
	public Vector<String>getMember()
	{
		return Member;
	}
	//RESTITUISCE LA CARD
	public Card getCard(String cardName)
	{
      for (Card card : TODO) 
      {
         if(card.getName().equals(cardName))
         {
            return card;
         }
         
      }
      for (Card card :INPROGRESS) 
      {
         if(card.getName().equals(cardName))
         {
            return card;
         }
         
      }
      for (Card card : TOBEREVISED) 
      {
         if(card.getName().equals(cardName))
         {
            return card;
         }
         
      }
      for (Card card : DONE) 
      {
         if(card.getName().equals(cardName))
         {
            return card;
         }
         
      }
      return null;
     
   }
   
	//RESTITUISCE TUTTE LE CARD DI UN PROGETTO
	public  Vector<Card> getCards() {
       Vector<Card> All = new Vector<Card>();
       All.addAll(TODO);
       All.addAll(INPROGRESS);
       All.addAll(TOBEREVISED);
       All.addAll(DONE);
       return All;

    }

    // AGGIUNGE LE CARD
    public  int addCard(String cardName, String Description) 
    {
         for(Card c: this.getCards())
         {
            if(c.getName().equals(cardName))
            {
               return 1; // card già presente
            }
            
         }
         Card newcard = new Card();
         newcard.SetHistory("Card Creata e Inserita in TODO"); /// MODIFICA STORIA DELLA CARD
         newcard.setDescription(Description);
         newcard.setName(cardName);
         TODO.add(newcard);
         try{
               //SERIAL
            j=new File("Worthrsc/"+Name+"/"+newcard.getName()+".json");
            j.createNewFile();
            obj.writeValue(j,newcard);
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
         return 0;
   }
        
   

    // SPOSTA LE CARD DA UNA LISTA ALL'ALTRA
    public  int moveCard(String cardName, String listPartenza, String listDestinazione) {
      //CONTROLLO CONDIZIONI DI SPOSTAMENTO
      if(listPartenza.equals("TODO") && (listDestinazione.equals("TOBEREVISED") || listDestinazione.equals("DONE")))
      {
         return 3;
      }
      if((listPartenza.equals("INPROGRESS") && listDestinazione.equals("TODO")) || (listPartenza.equals("TOBEREVISED") && listDestinazione.equals("TODO")))
      {
         return 3;
      }
      if(listPartenza.equals("DONE"))
      {
         return 3;
      }
      Vector<Card> temp1 = getList(listPartenza);
      Vector<Card> temp2 = getList(listDestinazione);
      if (temp1 != null && temp2 != null) 
      {
          Card moveCard = getCardspec(cardName, temp1);
          if(moveCard==null)
          {
             return 4; // card non presente in quella lista
          }
          else
          {
            moveCard.SetHistory("Card Spostata da " + listPartenza + " a " + listDestinazione); /// MODIFICA STORIA DELLA CARD
            moveCard.setLista(listDestinazione);
            temp1.remove(moveCard);
            temp2.add(moveCard);
            try{
               //SERIAL
               j=new File("Worthrsc/"+Name+"/"+moveCard.getName()+".json");
               obj.writeValue(j,moveCard);
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }
            return 0;
          }
      }
      return 4; //lista vuota

    }

    /// RESTITUISCE LA STORIA DELLA CARD
    public Vector<String> getCardHistory(String Name) {
       Card temp = getCard(Name);
       return temp.getHistory();
    }

    /// CERCA LA CARD ALL'INTERNO DELLA CODA SPECIFICATA
    private  Card getCardspec(String cardName, Vector<Card> lis) 
    {
       for (Card card : lis) {
          if (card.getName().equals(cardName)) {
             return card;
          }

       }
       return null;
    }

    /// METODO PRIVATE CHE RESITUISCE LA LISTA CORRETTA DATO IL NOME
    private Vector<Card> getList(String listName) {
       if (listName.equals("TODO")) {
          return TODO;
       }
       if (listName.equals("INPROGRESS")) {
          return INPROGRESS;
       }
       if (listName.equals("TOBEREVISED")) {
          return TOBEREVISED;
       }
       if (listName.equals("DONE")) {
          return DONE;
       }
       return null;
    }

    // CONTROLLO PER L'ELIMINAZIONE DEL PROGETTO
   public boolean Control() 
   {
      if (TODO.isEmpty() && TOBEREVISED.isEmpty() && INPROGRESS.isEmpty()) {       
         
         return true;

      } else {
        
         return false;
         
      }
   }
   //METODO CHE CONTROLLA SE UN UTENTE é MEMBRO DEL PROGETTO
   public boolean Ismember(String name)
   {
      return getMember().contains(name);

   }
   //METODO PER CREARE LA CARTELLA REALTIVA AL PROGETTO
   public void serial() 
   {
      File dir = new File("Worthrsc/"+this.Name);
      memb=new File(dir.getAbsolutePath()+"/membri.json");
      dir.mkdir();
   }

    
	
}
