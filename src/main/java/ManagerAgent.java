import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class ManagerAgent extends Agent  {
    public static int analysornumber = 1;
   
    public static ArrayList<Container> containers=new ArrayList<>();
    public static ArrayList<PacketSolved> packetSolveds=new ArrayList<>();
    public static int classREady=0;

    public static int numberOfContainers =100;
    public static int treating_time =1000;
    public static boolean end=false;


    @Override
    protected void setup() {
       

       
        
        addBehaviour(new TickerBehaviour(this,90000) {
            //behaviour: sending a check message every specific time
            @Override
            protected void onTick() {
                if(classREady==1){
                    InitialzeAllContainerInf();

                    for(int i=0;i<containers.size();i++){
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Check");
                        AID dest = new AID("SubManagerAgent_Container"+(i+1),AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);



                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {

                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"SubManagerAgent_Container"+(i+1),msg.getContent()));

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }

                    
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("Check");
                    AID dest = new AID("ClassifAgent",AID.ISLOCALNAME);
                    msg.addReceiver(dest);
                    send(msg);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {

                        PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"ClassifAgent",msg.getContent()));
                        Thread.sleep(ManagerAgent.treating_time);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    VerifyIfAgentsAreAlive();
                }

            }
        });

        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {


                ACLMessage message = receive();
                if (message != null) {

                    if(message.getContent().equals("need to update")){

                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("agreed to update");
                        AID dest = new AID("ClassifAgent",AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);
                        try {
                            PlatformPara.messages.add((new Message(msg.getSender().getLocalName(),"ClassifAgent",msg.getContent())));
                            Thread.sleep(ManagerAgent.treating_time);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        for(int i=0;i<ManagerAgent.containers.size();i++){
                            ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
                            message1.setContent("pause for update");
                            AID aid = new AID("SubManagerAgent_Container"+(i+1),AID.ISLOCALNAME);
                            message1.addReceiver(aid);
                            send(message1);

                            try {
                                PlatformPara.messages.add((new Message(message1.getSender().getLocalName(),"SubManagerAgent_Container"+(i+1),message1.getContent())));


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                    if(message.getContent().equals("update done")){

                        for(int i=0;i<ManagerAgent.containers.size();i++){
                            ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
                            message1.setContent("update done");
                            AID aid = new AID("SubManagerAgent_Container"+(i+1),AID.ISLOCALNAME);
                            message1.addReceiver(aid);
                            send(message1);

                            try {
                                PlatformPara.messages.add((new Message(message1.getSender().getLocalName(),"SubManagerAgent_Container"+(i+1),message1.getContent())));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(message.getContent().equals("ClassifReady")){

                        addBehaviour(new CreateContainers());
                    }

                    if(message.getContent().contains("Check50_A")){
                        //receiving message: that a container reached 50 anomaly packets
                        String cid = message.getContent().replace("Check50_A","");
                        updateNetworkState(Integer.parseInt(cid));
                        try {
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int state = CheckNetworkState();



                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("ADT_"+String.valueOf(state));
                        AID dest = null;
                        dest = new AID("SubManagerAgent_Container"+cid, AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);

                        PlatformPara.messages.add(new Message(msg.getSender().getLocalName(), "SubManagerAgent_Container"+cid, msg.getContent()));
                        block(10);
                        try {
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }

                    if(message.getContent().contains("B:Check_OK")){
                        String s = message.getSender().getLocalName().toString();
                        //SubManagerAgent_Container
                        ManagerAgent.containers.get(Integer.parseInt(s.replace("SubManagerAgent_Container",""))-1).setSubmanagerworking(true);
                        try {
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }



                }


            }
        });


    }

 

    public void updateNetworkState(int x){
        for(int i=0;i<containers.size();i++){
            if(x!=i+1){
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("net state");
                AID dest = null;
                dest = new AID("SubManagerAgent_Container"+String.valueOf(i+1), AID.ISLOCALNAME);
                msg.addReceiver(dest);
                send(msg);

                Message messageListe;
                messageListe = new Message(msg.getSender().getLocalName(), "SubManagerAgent_Container"+String.valueOf(i+1), msg.getContent());
                PlatformPara.messages.add(messageListe);
            }

        }


    }

    public int CheckNetworkState(){
        int count=0;
        for(int i=0;i<containers.size();i++){
            if(containers.get(i).getCuurentstate()>=5){
                count+=1;
            }
        }

        return count;
    }

    void InitialzeAllContainerInf(){
        for(int i=0;i<containers.size();i++){
            containers.get(i).setInformed(false);
        }
    }

    void VerifyIfAgentsAreAlive(){
        for(int i=0;i<containers.size();i++){
            /*System.out.println("----------------------------------------------\nSubamanager Agent of container:"+(i+1)+"is alive: "+containers.get(i).isInformed()+"\n" +
                    "------------------------------------------------------------");*/
            if(!containers.get(i).isInformed()){
                //DELETE IT
            }
        }
    }

    private void pauseAllWhileUpdating(){
        for(int i = 0;i<ManagerAgent.containers.size();i++){
            ManagerAgent.containers.get(i).setSniffer_working(false);
            ManagerAgent.containers.get(i).setAnalysorworking(false);
            ManagerAgent.containers.get(i).setSubmanagerworking(false);
        }
    }




}