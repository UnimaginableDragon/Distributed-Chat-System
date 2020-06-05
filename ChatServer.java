import java.rmi.*;
// Importa rebind
import java.rmi.server.*;
import java.util.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.InvalidKeyException;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.security.PublicKey;

/*
Chats server da un espacio en memoria  a  las clases de servidor y usaurio . Esta infromacion
puede ser accedida desde el registro (rmi registry).
1.Cuando el server se inicializa se genera un objeto de la clase ChatServer que se almacena en el registro.
2.Cada vez que un usuario se conecta se crea la clase del objeto usando la direccion del cliente como identificador.
3.El servidor se mantiene activo siempre que existan clientes conectandos que deseen transmitir mensajes.

REGISTRO (rmiregistry):
El registro se carga en el servidor y provee nombres globalesa cada uno de los objetos creados en las maquinas cliente.
*/
//Se genera una clase remota ChatServer que implementa la interface (remota) IChatServer
public class ChatServer extends UnicastRemoteObject implements IChatServer {

	private Cipher cipher;
	
	Hashtable< String, Hashtable<String, IChatClient>> chatters = new Hashtable< String, Hashtable<String, IChatClient>>();
	public ChatServer() throws Exception  {
		cipher = Cipher.getInstance(IChatServer.ALG);
		

	}// se instancia un objeto ChatServer
	public synchronized void login(SealedObject name1, SealedObject myTopic1, IChatClient nc) throws Exception {// se genera un metodo login se le pide al usuario un nobre y un topico sdicionalmente se pide un objeto de la interface IChatClient
		//decryption started..
		String name = getDMsg((SealedObject) name1, nc.getPublicKey());
		String myTopic = getDMsg((SealedObject) myTopic1, nc.getPublicKey());
		// decryption finished...
		
		System.out.println("Topico Ingresado: " + myTopic+ "\nBuscando topico");
		Hashtable<String, IChatClient> temaHash = chatters.get(myTopic);
		Hashtable<String, IChatClient> userHash = new Hashtable<String, IChatClient>();
		Boolean newTopic = false;
		if(temaHash == null)
		{
			System.out.println("El tema: " + myTopic+ " NO existe");
			chatters.put(myTopic , userHash);
			temaHash = chatters.get(myTopic);
			System.out.println("El tema ha sido creado: " + temaHash );
			temaHash.put(name, nc);

			Enumeration entChater = temaHash.elements();
			while( entChater.hasMoreElements() ){
			((IChatClient) entChater.nextElement()).receiveCreation(name );

		}

			newTopic = true;
		}
		else
		{
			System.out.println("El tema: " + myTopic+ " YA existe");
			temaHash.put(name, nc);
			newTopic = false;

		}



		Enumeration entChater = temaHash.elements();
		while( entChater.hasMoreElements() ){
			//Aqui el servidor escucha a los usuarios y aplica el metodo remoto recieveEnter
			((IChatClient) entChater.nextElement()).receiveEnter(name, newTopic );
		}
		System.out.println("Client " + name + " has logged in to TOPIC " + myTopic );// confirma el exito del emtodo login
	}

	public synchronized void logout(SealedObject name1, SealedObject tema1, IChatClient nc) throws Exception {
		
		String name = getDMsg((SealedObject) name1, nc.getPublicKey());
		String tema = getDMsg((SealedObject) tema1, nc.getPublicKey());
		
		
		System.out.println("Client " + name + " has logged out");
		Hashtable<String, IChatClient>  temaHash = chatters.get( tema );
		temaHash.remove(name);

		Enumeration entChater = temaHash.elements();
		while( entChater.hasMoreElements()) {
			((IChatClient) entChater.nextElement()).receiveExit(name);
		}
  }

	public synchronized void send(Message message) throws RemoteException {// el metodo envia requiere un objeto remoto del tipo Message
		String tema = message.topic;// toma del mensaje el topico
		Hashtable<String, IChatClient>  temaHash = chatters.get( tema );
		Enumeration entChater = temaHash.elements();
		while( entChater.hasMoreElements() ) {
			((IChatClient) entChater.nextElement()).receiveMessage(message);
		}
		System.out.println("Tema: " + tema + " => Message from client "+message.name+":\n"+message.text);// se pubca el tema y el nombre del usuario que escribio el mensaje
	}
	public boolean authenticateMsg(String msg, SealedObject cmsg, PublicKey k) throws Exception,InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException {
		cipher.init(Cipher.DECRYPT_MODE, k);
		return ((String) cmsg.getObject(cipher)).equals(msg);
	}
	public String getDMsg(SealedObject cmsg, PublicKey k)throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException{
		cipher.init(Cipher.DECRYPT_MODE, k);
		return (String) cmsg.getObject(cipher);
	}

	public static void main( String[] args ) throws Exception{
		String serverURL = new String("///ChatServer");
		try {
			ChatServer server = new ChatServer();
			Naming.rebind(serverURL, server);
			System.out.println("Chat server ready");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
