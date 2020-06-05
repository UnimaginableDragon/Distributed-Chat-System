import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

// se implementa un interface IChatClient con 4 metodos remotos recieveEnter receiveCreation recieveExit y receiveMessage
public interface IChatClient extends java.rmi.Remote {
  PublicKey getPublicKey() throws RemoteException;
  void receiveEnter(String name, Boolean newTopic) throws RemoteException;// este metodo es el que recibe el texto del usuario
  void receiveCreation(String name) throws RemoteException;// este crea un nuevo topico
  void receiveExit(String name)throws RemoteException;// salir
  void receiveMessage(Message message) throws RemoteException;// recive mensajes de otros usuarios dentro del topico
}
