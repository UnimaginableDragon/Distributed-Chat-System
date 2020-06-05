import java.io.*;

//se genera un classe remota del tipo serializable
public class Message implements Serializable {
   public String name;// se instancian 3 string (que son serializables)
   public String text;
   public String topic;

  public Message(String name, String text, String myTopic) {// se declara un objeto del tipo Mesage que recivira los imputs del usuario
      this.name = name;
      this.text = text;
      this.topic = myTopic;
    }

}
