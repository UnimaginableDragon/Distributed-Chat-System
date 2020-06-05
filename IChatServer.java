import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;

// se genera una interface remota con tres metodos (que pueden se accedidos d emanera remota) login logout y send
public interface IChatServer extends java.rmi.Remote {
  public static final String ALG = "RSA";
  public static final String ENCODE = "UTF-8";
  public static final int KEYLENGTH = 2048;
  void login(SealedObject name, SealedObject myTopic, IChatClient newClient) throws Exception;
  void logout(SealedObject name, SealedObject topic, IChatClient newClient)throws Exception;
  void send(Message message) throws RemoteException;
}
