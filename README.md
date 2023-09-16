# Worth_Project
## Progetto Reti di Calcolatori 2021
Workk Together è un software per la gestione di progetti in modo **collaborativo**. Questi possono essere progetti professionali, o in
generale qualsiasi attività possa essere organizzata in una serie di compiti (es. to do list) che sono svolti da
membri di un gruppo: le applicazioni di interesse sono di diverso tipo, si pensi alla organizzazione di un
progetto di sviluppo software con i colleghi del team di sviluppo, ma anche all’organizzazione di una festa
con un gruppo di amici.
L'applicazione funziona tramite un interfaccia CLI.
### Utenti e Card
Gli utenti possono accedere a WORTH dopo registrazione e login.
In **WORTH**, un progetto, identificato da un nome univoco, è costituito da una serie di *card*, che
rappresentano i compiti da svolgere per portarlo a termine, e fornisce una serie di servizi. Ad ogni progetto
è associata una lista di membri, ovvero utenti che hanno i permessi per modificare le card e accedere ai
servizi associati al progetto (es. chat).
Una card è composta da un nome e una descrizione testuale. Il nome assegnato alla card deve essere
univoco nell’ambito di un progetto. Ogni progetto ha associate quattro liste che definiscono il flusso di
lavoro come passaggio delle card da una lista alla successiva: *TODO, INPROGRESS, TOBEREVISED, DONE*.
Qualsiasi membro del progetto può spostare la card da una lista all’altro.
## Implementazione
* La fase di Registrazione dell'utente è implementata tramite **RMI**.

* La fase di login viene effettuata come prima operazione dopo aver instaurato una
connessione TCP con il server. In risposta all’operazione di login, il server invia anche la lista degli
utenti registrati e il loro stato (online, offline).

* A seguito della login il client si registra ad un servizio
di notifica del server per ricevere aggiornamenti sullo stato degli utenti registrati, dopo previa login effettuata con successo, l’utente interagisce, secondo il modello **client-server**.

* La chat di progetto deve essere realizzata usando UDP **multicast**.
### Librerie Esterne

Viene utilizzata la ***libreria jackson*** per la serializzazione degli oggetti in un 
formato json, sia per la scrittura su file, sia per l’invio sulla Socket per restituire le risorse al client.
 
### How to Run?
 
Istruzioni di compilazione
Da linea di comando, accedendo alla directory “src” dove sono presenti i file del progetto:

Genera il **bytecode i file .class**:

• *javac -cp ../lib/jackson-core-2.9.7.jar:../lib/jackson-databind2.9.7.jar:../lib/jackson-annotations-2.9.7.jar 
MainServer.java Server.java Producer.java 
InterfaceRegister.java Progetto.java Card.java 
InterfaceClient.java MainClient.java Chat.java.*

Esegue il **Server**:

• *java -cp ../lib/jackson-core-2.9.7.jar:../lib/jackson-databind2.9.7.jar:../lib/jackson-annotations-2.9.7.jar: MainServer.*

Esegue il **Client**:

• *java -cp ../lib/jackson-core-2.9.7.jar:../lib/jackson-databind2.9.7.jar:../lib/jackson-annotations-2.9.7.jar: MainClient.*




