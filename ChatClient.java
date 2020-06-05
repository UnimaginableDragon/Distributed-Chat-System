import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
// genera una clase remota ChatClient que implementa la interface IChatClient
public class ChatClient extends UnicastRemoteObject implements IChatClient {

	private KeyPair pair;
	private Cipher cipher;
	public String name;// se declara una varialble string que tenga el nombre del cliente
	IChatServer server;// se instancia un objeto remoto de la interface IChatServer
	String myTopic;//se declara un string que tendra el nombre del topico
	String serverURL;// se declara una variable string que tenga la direccion url

	public ChatClient( String name, String url ) throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException {
		this.name = name;//se instancia un objeto Chatclient quien define el nombre y el url ademas de invocar el metodo asktopic (esta mas abajo)
		serverURL = url;
		
		KeyPairGenerator kg = KeyPairGenerator.getInstance(IChatServer.ALG);
		kg.initialize(IChatServer.KEYLENGTH);
		pair = kg.generateKeyPair();
		cipher = Cipher.getInstance(IChatServer.ALG);
		
		myTopic = askTopic();
		System.out.println("1) El topico ingresado es: " + myTopic);// se imprime un texto con el nombre dle topico
		connect(myTopic);// se invoca el metodo connect y se le pasa la variable myTopic
		
	}
	public PublicKey getPublicKey() {
		return pair.getPublic();
	}

	private void connect(String myTopic) {
		try {// metodo conect genera un objeto IChatServer con los valores del url
			server=(IChatServer) Naming.lookup("rmi://"+serverURL+"/ChatServer"); // Connect to the defined host/ChatServer
			server.login(getCMsg(name), getCMsg(myTopic), this); // se invoca de la clase remota chatserver su metodo login y se le pasa el nombre del usuario y el topico
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}


	public static String askTopic() {// metodo ask topic solicita el nombre del topico, lo le y lo regresa (en este caso se guarda en la variable MyTopic del metodo ChatCient)
		System.out.println("Ingrese el topico al que desea acceder \n");
		String input = System.console().readLine();
		return  input;

	}

	private void disconnect() {
		try {
			server.logout(getCMsg(name), getCMsg(myTopic), this); // Invoca el metodo logout de la clase remota ChatServer
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private void salirTema() {
		try {
			server.logout(getCMsg(name), getCMsg(myTopic), this); //  Invoca el metodo logout de la clase remota ChatServer
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		myTopic = askTopic();
		System.out.println("1) Nuevo tema ingresado: " + myTopic);
		connect(myTopic);
	}

	private void sendTextToChat(String text ) {
		try {
			server.send(new Message((name), (text), (myTopic))); //Se invoca el metodo enviar de la clase remota ChatServer y se le pasa un objeto de la clase Message instanciado con el nombre el texto escrito y el nombre del topico
		}
		catch( RemoteException e ) {
			e.printStackTrace();
		}
	}

	public void receiveEnter( String name,  Boolean newTopic ) { // Este metodo se invoca cuando otros clientes accedana lchat
		System.out.println("\nLog in "+name+"\n"+this.name+" -- Cadena a enviar: ");
	}

	public void receiveCreation( String name )
	{
		{System.out.println("\nConfirmacion: El usuario " + name  +" !a creado un nuevo tema");}
	}

	public void receiveExit( String name ) { // este metodo es invocado por el ChatServer cuando otros usuarios se slaen
		System.out.println("\nLog out " + name + "\n");
		if ( name.equals(this.name) )
			System.exit(0);
		else
			System.out.println(this.name + " -- Cadena a enviar: " );
	}

	public void receiveMessage( Message message ) { // este metodo que recive un objeto de la clase remota message se utilizara cuando alguien mas escriba en el chat
			System.out.println
("\nTema:" + message.topic +"=> " +message.name+":\n"+message.text+"\n"+name+" -- Cadena a enviar: ");
	}
	public SealedObject getCMsg(String msg) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		cipher.init(Cipher.ENCRYPT_MODE, pair.getPrivate());
		return new SealedObject(msg, cipher);  
	}
	public static String pideCadena(String letrero) { //esta funcion permite al usuario ingresar texto
		StringBuffer strDato = new StringBuffer();
		String strCadena = "";
		try {
			System.out.print(letrero);
			BufferedInputStream bin = new BufferedInputStream(System.in);
 			byte bArray[] = new byte[256];
 			int numCaracteres = bin.read(bArray);
			while (numCaracteres==1 && bArray[0]<32)
				numCaracteres = bin.read(bArray);
			for(int i=0;bArray[i]!=13 && bArray[i]!=10 && bArray[i]!=0; i++) {
				strDato.append((char) bArray[i]);
			}
			strCadena = new String( strDato );
		}
		catch( IOException ioe ) {
			System.out.println(ioe.toString());
		}
		return strCadena;
	}

	public static void main( String[] args ) throws Exception{
		String strCad;
		try {
			// Se instancia un objeto de la clase ChatClient se connecta y logea al mismo
			ChatClient clte = new ChatClient( args[0],args[1] );
			//Aqui se permite al usuario entrar texto, en caso de que se escriba quit  o salir tema el chat se cerrara o el usuario abandonara el tema
			strCad = pideCadena(args[0] + " -- Cadena a enviar: ");
			while( !strCad.equals("quit")){
				clte.sendTextToChat(strCad);
				strCad = pideCadena(args[0]+" -- Cadena a enviar: ");
				if(strCad.equals("salirTema") )
				{
					System.out.println("El cliente "+clte.name+", ha abandonado el grupo ");
					clte.salirTema();
					clte.sendTextToChat(strCad);
					strCad = pideCadena(args[0]+" -- Cadena a enviar: ");

				}
			}
				System.out.println("Local console "+clte.name+", going down");
				clte.disconnect();

		}
		catch( RemoteException e ) {
			e.printStackTrace();
		}
	}

}
