import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String Sender;
    private String Reciever;
    private String Content;
    private LocalDateTime Time;

    Message(){

    }

    Message(String sender,String reciever,String content){
        this.Content = content;
        this.Reciever = reciever;
        this.Sender = sender;
        this.Time = LocalDateTime.now();
    }

    Message(ACLMessage aclMessage){
        this.Sender = aclMessage.getSender().getLocalName();
        this.Content = aclMessage.getContent();


    }
    public String PrintMessage(){
        return "Sender: "+this.Sender+"\nReciever: "+this.Reciever+"\nContent: "+this.Content;
    }


    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getReciever() {
        return Reciever;
    }

    public void setReciever(String reciever) {
        Reciever = reciever;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public LocalDateTime getTime() {
        return Time;
    }

    public void setTime(LocalDateTime time) {
        Time = time;
    }


    public DBObject toDBObject(){
        DBObject dbObject = new BasicDBObject().append("sender",this.Sender)
                .append("reciever",this.Reciever)
                .append("content",this.Content)
                .append("time", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(this.Time))
                ;


        return dbObject;
    }
}
