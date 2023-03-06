import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


public class SnifferAgent extends Agent {



    @Override
    protected void setup() {






        Instances TestData = null;
        try {
            TestData = new DataSource("KDDTest.arff").getDataSet();
            TestData.setClassIndex(TestData.numAttributes()-1);

        } catch (Exception e) {
            e.printStackTrace();
        }


        String containerID = getMyID(getAID().getLocalName());



        addBehaviour(new BehSniff(this,Integer.parseInt((containerID))-1,TestData));


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage message = receive();
                if(message!=null){
                    if(message.getContent().equals("Check")){
                        try {
                            Thread.sleep(ManagerAgent.treating_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("S:Check_OK");
                        AID dest = null;
                        dest = new AID("AnalysorAgent_Container"+String.valueOf(containerID), AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);
                        try {
                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),
                                    "AnalysorAgent_Container"+String.valueOf(containerID),msg.getContent()));
                            Thread.sleep(ManagerAgent.treating_time);
                            //PlatformPara.NotifyMessages(new Message(msg.getSender().getLocalName(),"SubManagerAgent_Container"+String.valueOf(containerID),msg.getContent()),0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(message.getContent().equals("pause for update")){

                        ManagerAgent.containers.get(Integer.parseInt(containerID)).setSniffer_working(false);

                    }
                    if(message.getContent().equals("update done")){
                        ManagerAgent.containers.get(Integer.parseInt(containerID)).setSniffer_working(true);
                    }
                }

            }
        });









}

    public String getMyID(String AID){
        return AID.replace("SnifferAgent_Container","");
    }

    public int Exist(PacketSniffer packetSniffer,String containerID){



        return 0;
    }







}

