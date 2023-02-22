import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class SubManagerAgent extends Agent {
    @Override
    protected void setup() {
        String containerID = getMyID(getAID().getLocalName());

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {


                AgentController agentController = null;
                try {
                    agentController = getAgent().getContainerController().createNewAgent("SnifferAgent_Container"+containerID,"SnifferAgent",null);
                    ManagerAgent.containers.get(Integer.parseInt(containerID)-1).setSnifferAID("SnifferAgent_Container"+containerID);
                    agentController.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        });


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                if(ManagerAgent.containers.get(Integer.parseInt(containerID)).isSubmanagerworking()) {


                    ACLMessage message = receive();
                    if (message != null) {
                        if (message.getContent().equals("Check")) {

                            ManagerAgent.containers.get(Integer.parseInt(containerID) - 1).setInformed(true);

                            addBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {

                                    ACLMessage msg_subM_to_Analysor = new ACLMessage(ACLMessage.INFORM);
                                    msg_subM_to_Analysor.setContent("Check");
                                    AID dest = null;
                                    dest = new AID("AnalysorAgent_Container" + String.valueOf(containerID), AID.ISLOCALNAME);
                                    msg_subM_to_Analysor.addReceiver(dest);
                                    send(msg_subM_to_Analysor);

                                    ACLMessage msg_subM_to_Manager = new ACLMessage(ACLMessage.INFORM);
                                    msg_subM_to_Manager.setContent("B:Check_OK");
                                    AID dest2 = null;
                                    dest2 = new AID("ManagerAgent", AID.ISLOCALNAME);
                                    msg_subM_to_Manager.addReceiver(dest2);
                                    send(msg_subM_to_Manager);


                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        PlatformPara.messages.add(new Message(msg_subM_to_Analysor.getSender().getLocalName(),
                                                "AnalysorAgent_Container" + String.valueOf(containerID), msg_subM_to_Analysor.getContent()));


                                        PlatformPara.messages.add(new Message(msg_subM_to_Manager.getSender().getLocalName(),
                                                "ManagerAgent", msg_subM_to_Manager.getContent()));
                                        //PlatformPara.NotifyMessages(new Message(msg.getSender().getLocalName(),"AnalysorAgent_Container"+String.valueOf(containerID),msg.getContent()),0);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    ACLMessage rcv = receive();
                                    if (rcv != null) {
                                        if (rcv.getContent().equals("Check_OK")) {

                                            if (rcv.getSender().getLocalName().equals("AnalysorAgent_Container" + containerID)) {
                                                /*System.out.println("----------------------------------------\nAnalysorAgent_Container" + containerID + "\t is ALIVE\n" +
                                                        "-------------------------------------------------");*/
                                            }

                                        }
                                    } else {
                                        //DELETE IT Agent
                                    }
                                }
                            });

                            addBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                                    msg.setContent("Check");
                                    AID dest = null;
                                    dest = new AID("SnifferAgent_Container" + String.valueOf(containerID), AID.ISLOCALNAME);
                                    msg.addReceiver(dest);
                                    send(msg);
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        PlatformPara.messages.add(new Message(msg.getSender().getLocalName(), "SnifferAgent_Container" + String.valueOf(containerID), msg.getContent()));
                                        //PlatformPara.NotifyMessages(new Message(msg.getSender().getLocalName(),"SnifferAgent_Container"+String.valueOf(containerID),msg.getContent()),0);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    ACLMessage rcv = receive();
                                    if (rcv != null) {
                                        if (rcv.getContent().equals("Check_OK")) {

                                            if (rcv.getSender().getLocalName().equals("SnifferAgent_Container" + containerID)) {

                                                /*System.out.println("----------------------------------------\nSnifferAgent_Container" + containerID + "\t is ALIVE\n" +
                                                        "-------------------------------------------------");*/
                                            }

                                        }
                                    } else {
                                        //DELETE IT AGnet
                                    }
                                }
                            });

                        }

                        if (message.getContent().contains("S:Check_OK")) {


                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent("S:Check_OK");
                            AID dest = null;
                            dest = new AID("ManagerAgent", AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);


                            try {
                                PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),
                                        "ManagerAgent", msg.getContent()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


                        if (message.getContent().contains("A:Check_OK")) {


                            ManagerAgent.containers.get(Integer.parseInt(containerID)).setAnalysorworking(true);
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent("A:Check_OK");
                            AID dest = null;
                            dest = new AID("ManagerAgent", AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);


                            try {
                                PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),
                                        "ManagerAgent", msg.getContent()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        if(message.getContent().equals("pause for update")){
                            ManagerAgent.containers.get(Integer.parseInt(containerID)).setSubmanagerworking(false);

                            ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
                            message1.setContent("pause for update");
                            AID aid = new AID("AnalysorAgent_Container"+containerID,AID.ISLOCALNAME);
                            message1.addReceiver(aid);
                            send(message1);

                            try {
                                PlatformPara.messages.add((new Message(message1.getSender().getLocalName(),"AnalysorAgent_Container"+containerID,message1.getContent())));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if(message.getContent().equals("update done")){

                            ManagerAgent.containers.get(Integer.parseInt(containerID)).setSubmanagerworking(true);

                            ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
                            message1.setContent("update done");
                            AID aid = new AID("AnalysorAgent_Container"+containerID,AID.ISLOCALNAME);
                            message1.addReceiver(aid);
                            send(message1);

                            try {
                                PlatformPara.messages.add((new Message(message1.getSender().getLocalName(),"AnalysorAgent_Container"+containerID,message1.getContent())));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if(message.getContent().contains("Check50_A")){

                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent(message.getContent());
                            AID dest = null;
                            dest = new AID("ManagerAgent", AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);
                            try {
                                PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"ManagerAgent",msg.getContent()));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                        if(message.getContent().contains("ADT_")){

                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent(message.getContent());
                            AID dest = null;
                            dest = new AID("AanalysorAgent_Container"+containerID, AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);

                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(), "AanalysorAgent_Container"+containerID, msg.getContent()));
                        }


                        if(message.getContent().equals("net state")){
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent("net state");
                            AID dest = null;
                            dest = new AID("AnalysorAgentAgent_Container"+containerID, AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);

                            Message messageListe;
                            messageListe = new Message(msg.getSender().getLocalName(), "AnalysorAgentAgent_Container"+containerID, msg.getContent());
                            PlatformPara.messages.add(messageListe);
                        }


                    }
                }
            }
        });

    }

    public String getMyID(String AID){
        return AID.replace("SubManagerAgent_Container","");
    }
}
