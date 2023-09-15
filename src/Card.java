import java.util.*;



public class Card {

	private String Name;
	private String Description;
   private String Lista;
	private Vector<String> History;

	public Card()
	{
      History=new Vector<String>();
      Lista="TODO";
   }

   public void setLista(String  l)
   {
      Lista=l;
   }

   public String getLista()
   {
      return Lista;
   }

   public String getName()
   {
      return Name;
   }

   public String getDescription()
   {
      return Description;
   }
   
   ///AGGIUNGE LA DESCRIZIONE ALLA CARD
   public void setDescription(String Description)
   {
      this.Description=Description;
   }
   ///AGGIUNGE I NOME ALLA CARD
   public void setName(String Name)
   {
      this.Name=Name;
   }
   ///RESTITUISCE LA HISTORY DI UNA CARD
   public Vector<String> getHistory()
   {
      return History;
   }
   ///METODO CHE  MODIFICA LA STORIA DELLA CARD
   public void SetHistory(String Movement)
   {
      History.add(Movement);
   }
}
