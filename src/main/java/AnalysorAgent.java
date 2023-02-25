import com.mongodb.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import weka.core.Instance;

import java.util.ArrayList;


public class AnalysorAgent extends Agent {


    //public boolean informed = false;
    Clsi DT,SVM,NN;

    int packets_counter = 0;

    @Override
    protected void setup() {





        ArrayList<Attack> attacks = ClassifAgent.attacks;




        DT = ClassifAgent.DT;
        SVM= ClassifAgent.SVM;
        NN = ClassifAgent.NN;

        String containerID = getMyID(getAID().getLocalName());




        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    AgentController agentController = null;
                    agentController = getContainerController().createNewAgent("SnifferAgent_Container"+containerID,"SnifferAgent",null);


                    agentController.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {




                ACLMessage recieve = receive();
                if(recieve!=null){

                    if(recieve.getContent().equals("Check")){
                        ACLMessage message_from_analysor_to_sniffer = new ACLMessage(ACLMessage.INFORM);
                        message_from_analysor_to_sniffer.setContent("Check");
                        AID dest = null;
                        dest = new AID("SnifferAgent_Container"+String.valueOf(containerID), AID.ISLOCALNAME);
                        message_from_analysor_to_sniffer.addReceiver(dest);
                        send(message_from_analysor_to_sniffer);



                        ACLMessage message_from_analysor_to_submanager = new ACLMessage(ACLMessage.INFORM);
                        message_from_analysor_to_submanager.setContent("A:Check_OK");
                        AID dest2 = null;
                        dest2 = new AID("SubManagerAgent_Container"+String.valueOf(containerID), AID.ISLOCALNAME);
                        message_from_analysor_to_submanager.addReceiver(dest2);
                        send(message_from_analysor_to_submanager);

                        try {
                            PlatformPara.messages.add(new Message(message_from_analysor_to_sniffer.getSender().getLocalName(),
                                    "SnifferAgent_Container"+String.valueOf(containerID),message_from_analysor_to_sniffer.getContent()));

                            PlatformPara.messages.add(new Message(message_from_analysor_to_submanager.getSender().getLocalName(),
                                    "SubManagerAgent_Container"+String.valueOf(containerID),message_from_analysor_to_submanager.getContent()));
                            Thread.sleep(ManagerAgent.treating_time);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(recieve.getContent().contains("S:Check_OK")){

                        ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setSniffer_working(true);

                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("S:Check_OK");
                        AID dest = null;
                        dest = new AID("SubManagerAgent_Container"+String.valueOf(containerID), AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);


                        try {
                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),
                                    "SubManagerAgent_Container"+String.valueOf(containerID),msg.getContent()));
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(recieve.getContent().equals("pause for update")){

                        ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setAnalysorworking(false);

                        ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
                        message1.setContent("pause for update");
                        AID aid = new AID("SnifferAgent_Container"+containerID,AID.ISLOCALNAME);
                        message1.addReceiver(aid);
                        send(message1);

                        try {
                            PlatformPara.messages.add((new Message(message1.getSender().getLocalName(),"SnifferAgent_Container"+containerID,message1.getContent())));
                            Thread.sleep(ManagerAgent.treating_time);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(recieve.getContent().equals("update done")){

                        ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setAnalysorworking(true);

                        DT = ClassifAgent.DT;
                        SVM= ClassifAgent.SVM;
                        NN = ClassifAgent.NN;

                        ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
                        message1.setContent("update done");
                        AID aid = new AID("SnifferAgent_Container"+containerID,AID.ISLOCALNAME);
                        message1.addReceiver(aid);
                        send(message1);

                        try {
                            PlatformPara.messages.add((new Message(message1.getSender().getLocalName(),"SnifferAgent_Container"+containerID,message1.getContent())));
                            Thread.sleep(ManagerAgent.treating_time);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }



                if(ManagerAgent.containers.get(Integer.parseInt(containerID)-1).getPacketClassified().size()%50==0 && !ManagerAgent.containers.get(Integer.parseInt(containerID)-1).isInformed()) {
                    if(!ManagerAgent.containers.get(Integer.parseInt(containerID)-1).isContainerInformed()){
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Check50_A" + containerID);
                        AID dest = null;
                        dest = new AID("SubManagerAgent_Container"+containerID, AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);
                        try {
                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"SubManagerAgent_Container"+containerID,msg.getContent()));
                            Thread.sleep(ManagerAgent.treating_time);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        try {
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setContainerInformed(true);

                    }

                    ACLMessage messageRcv = receive();
                    if (messageRcv!=null){


                        if(messageRcv.getContent().equals("net state")){
                            ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setCuurentstate(ManagerAgent.containers.get(Integer.parseInt(containerID)-1).getPacketClassified().size());
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent("netstate_updated");
                            AID dest = null;
                            dest = new AID("SubManagerAgent_Container"+containerID, AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);

                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"SubManagerAgent_Container"+containerID,msg.getContent()));
                            try {
                                Thread.sleep(ManagerAgent.treating_time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                            block();
                        }

                        if(messageRcv.getContent().contains("ADT_")){
                            ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setInformed(true);
                            int state = Integer.parseInt(messageRcv.getContent().replace("ADT_",""));

                            if(state==ManagerAgent.containers.size()){
                                //UNDER ATTACK
                            }
                            //System.out.println("Anomalie/attack sur "+messageRcv.getContent().replace("ADT_","")+"machines");


                        }
                        block(10);
                    }
                }
                if(!ManagerAgent.containers.get(Integer.parseInt(containerID)-1).getPacketsDetected().isEmpty()){

                    PacketSniffer packetSniffer = ManagerAgent.containers.get(Integer.parseInt(containerID)-1).getPacketsDetected().get(0);




                    if(ManagerAgent.containers.get(Integer.parseInt(containerID)-1).isAnalysorworking()){

                        try {
                            Solve(attacks,packetSniffer,DT,SVM,NN);

                            packets_counter+=1;


                            ManagerAgent.containers.get(Integer.parseInt(containerID)-1).getPacketsDetected().remove(packetSniffer);



                        } catch (Exception e) {
                            e.printStackTrace();
                        }


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

    public String getMyID(String AID){
        return AID.replace("AnalysorAgent_Container","");
    }





    public void Solve(ArrayList<Attack> attacks, PacketSniffer packetTest, Clsi DT, Clsi SVM, Clsi NN) throws Exception{

        String containerID = getMyID(getAID().getLocalName());

        double resultj48 = DT.getClassifier().classifyInstance(packetTest.getInstance());
        double resultjsvm = SVM.getClassifier().classifyInstance(packetTest.getInstance());
        double resultjnn = NN.getClassifier().classifyInstance(packetTest.getInstance());
        Attack attackj48 = attacks.get((int)resultj48);
        Attack attacksvm = attacks.get((int)resultjsvm);
        Attack attacknn = attacks.get((int)resultjnn);

        String printed="";


        
        printed+="\nPacket : "+"--:\t"+packetTest.getInstance()+"\nDetected as :";
        //System.out.println("\n\nPacket : "+"--:\t"+packetTest.getInstance()+"\nDetected as :\n----------------");

        printed+="\nDecesion tree: "+attacks.get((int) resultj48).getName()+"  ["+attacks.get((int)resultj48).getCategory()+"]";
        //System.out.println("Decesion tree: "+attacks.get((int) resultj48).getName()+"  ["+attacks.get((int)resultj48).getCategory()+"]");

        printed+="\nSVM: "+attacks.get((int) resultjsvm).getName()+"  ["+attacks.get((int)resultjsvm).getCategory()+"]";
        printed+="\nNN: "+attacks.get((int) resultjnn).getName()+"  ["+attacks.get((int)resultjnn).getCategory()+"]";


        //System.out.println("SVM: "+attacks.get((int) resultjsvm).getName()+"  ["+attacks.get((int)resultjsvm).getCategory()+"]");
        //System.out.println("NN: "+attacks.get((int) resultjnn).getName()+"  ["+attacks.get((int)resultjnn).getCategory()+"]\n\n");

        double finall= getFinaleClass(DT,SVM,NN,attackj48,attacksvm,attacknn);

        printed+="\nFinal decision : "+attacks.get((int)finall).getName();

        //System.out.println("Final decision : "+attacks.get((int)finall).getName());

        Instance instance = packetTest.getInstance();
        instance.setValue(26,finall);
        PacketDetected packetDetected = new PacketDetected(instance);
        packetDetected.setCategory(attacks.get((int)finall).getCategory());

        printed+="\nCATE:"+packetDetected.getCategory();
        //System.out.println("\n\nCATE:"+packetDetected.getCategory());

        packetDetected.setBywho(getByWho(attacks.get((int)finall).getName(),attackj48,attacksvm,attacknn));
        ManagerAgent.containers.get(Integer.parseInt(containerID)-1).getPacketClassified().add(packetDetected);
        //sendPackettoDB(packetDetected,printed);





    }

    public  double getFinaleClass(Clsi DT, Clsi SVM, Clsi NN, Attack j48, Attack svm, Attack nn){



        ArrayList<Attack> attacks = new ArrayList<>();
        attacks.add(j48);
        attacks.add(svm);
        attacks.add(nn);

        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Double> scores = new ArrayList<>();
        ArrayList<Clsi> clsis = new ArrayList<>();
        clsis.add(DT);
        clsis.add(SVM);
        clsis.add(NN);


        for(int i=0;i<attacks.size();i++){
            if(!strings.isEmpty() && strings.contains(attacks.get(i).getID())){
                double x = scores.get(strings.indexOf(attacks.get(i).getID()));

                scores.set(strings.indexOf(attacks.get(i).getID()),x+clsis.get(i).getEvaluation().fMeasure(1));
                x=0;

            }else{
                strings.add(attacks.get(i).getID());

                scores.add(clsis.get(i).getEvaluation().fMeasure(1));

            }
        }

        double max=-1;
        double index=-1;
        for(int i=0;i<scores.size();i++){
            if(scores.get(i)>max){
                max=scores.get(i);
                index=i;
            }
        }

        return Double.parseDouble(strings.get((int)index));
    }

    public String getByWho(String result, Attack j48, Attack svm, Attack nn){
        String x="";
        if(result.equals(j48.getName())){
            x+="DT,";
        }

        if(result.equals(svm.getName())){
            x+="SVM,";
        }

        if(result.equals(nn.getName())){
            x+="NN,";
        }

        System.out.println("\n\nBYWHO:"+x);
        return x;
    }

    /*public static void sendPackettoDB(PacketDetected packetDetected, String printed)throws Exception{
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("Test_Hierarchical");

        DBCollection collection = database.getCollection("PacketsDetected");


        DBObject dbObject = packetDetected.toDBObject();

        /*List<Integer> books = Arrays.asList(27464, 747854);

        DBObject person = new BasicDBObject("_id", "jo")
                .append("name", "Jo Bloggs")
                .append("address", new BasicDBObject("street", "123 Fake St")
                        .append("city", "Faketon")
                        .append("state", "MA")
                        .append("zip", 12345))
                .append("books", books);

        collection.insert(dbObject);

        printed+="\nPacket Detectd and saved as  "+dbObject.get("class");
        //System.out.println("Packet Detectd and saved as  "+dbObject.get("class"));

        System.out.println(printed+"\n------------------------------------------------");


    }*/


}
