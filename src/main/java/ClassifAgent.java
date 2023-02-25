import com.mongodb.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ClassifAgent extends Agent {

    public static ArrayList<Attack> attacks = new ArrayList<>();

    public static Clsi DT;
    public static Clsi SVM;
    public static Clsi NN;

    private int okDT = 0;
    private int okSVM = 0;
    private int okNN = 0;


    @Override
    protected void setup() {

        try {
            Classify();
        } catch (Exception e) {
            e.printStackTrace();
        }

        addBehaviour(new TickerBehaviour(this,3600000) {



            //auto Upadte classifiers behaviour
            @Override
            protected void onTick() {


                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("need to update");
                AID dest = new AID("ManagerAgent",AID.ISLOCALNAME);
                msg.addReceiver(dest);
                send(msg);
                try {
                    PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"ManagerAgent",msg.getContent()));
                    Thread.sleep(50);
                    //PlatformPara.NotifyMessages(new Message(msg.getSender().getLocalName(),"AnalysorAgent_Container"+(i+1),msg.getContent()),0);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                //manual update by message
                ACLMessage aclMessage = receive();
                if(aclMessage!=null){

                    if (aclMessage.getContent().equals("update")){
                        try {
                            Classify();
                            for(int i = 0; i< ManagerAgent.containers.size(); i++){
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                                msg.setContent("need to update");
                                AID dest = new AID("ManagerAgent"+(i+1),AID.ISLOCALNAME);
                                msg.addReceiver(dest);
                                send(msg);
                                try {
                                    PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"ManagerAgent",msg.getContent()));
                                    Thread.sleep(50);
                                    //PlatformPara.NotifyMessages(new Message(msg.getSender().getLocalName(),"AnalysorAgent_Container"+(i+1),msg.getContent()),0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(aclMessage.getContent().equals("Check")){

                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("C:Check_OK");
                        AID dest = null;
                        dest = new AID("ManagerAgent", AID.ISLOCALNAME);
                        msg.addReceiver(dest);
                        send(msg);


                        try {
                            PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),
                                    "ManagerAgent",msg.getContent()));
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(aclMessage.getContent().equals("agreed to update")){



                        int isClassifiersUpdated=0;
                        try {
                            isClassifiersUpdated =UpdateClassifiers();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                        if(isClassifiersUpdated==1){

                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent("update done");
                            AID dest = null;
                            dest = new AID("ManagerAgent", AID.ISLOCALNAME);
                            msg.addReceiver(dest);
                            send(msg);


                            try {
                                PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),
                                        "ManagerAgent",msg.getContent()));
                                Thread.sleep(50);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


                    }
                }
            }
        });

    }

    //training methods for classifiers
    public void CallDT(Instances TrainDataDT) throws Exception {


        J48 j48 = new J48();
        j48.buildClassifier(TrainDataDT);

        DT = new Clsi("DT", j48, TrainDataDT);

        System.out.println("DT DONE-----");
        System.out.println("F-Measure :" + DT.getEvaluation().fMeasure(1));
        /*System.out.println("Precision : "+DT.getEvaluation().precision(1));
        System.out.println("Recall: "+DT.getEvaluation().recall(1));
        System.out.println("Error rate: "+DT.getEvaluation().errorRate());
        System.out.println("% Correct: "+DT.getEvaluation().pctCorrect());
        System.out.println("% Incorrect:  "+DT.getEvaluation().pctIncorrect());*/
        System.out.println("----------------------\n");


        //sendMSG("DT,TR"+TrainDataDT.size()+",FM"+DT.getEvaluation().fMeasure(1)+",PR"+DT.getEvaluation().precision(1));
        okDT = 1;

    }

    void CallNN(Instances TrainDataSVM) throws Exception {


        MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();

        multilayerPerceptron.setLearningRate(0.1);
        multilayerPerceptron.setMomentum(0.2);
        multilayerPerceptron.setTrainingTime(2000);
        multilayerPerceptron.setHiddenLayers("3");
        multilayerPerceptron.buildClassifier(TrainDataSVM);
        NN = new Clsi("NN", multilayerPerceptron, TrainDataSVM);
        System.out.println("NN done");
        System.out.println("F-Measure :" + NN.getEvaluation().fMeasure(1));
        /*System.out.println("Precision : "+DT.getEvaluation().precision(1));
        System.out.println("Recall: "+DT.getEvaluation().recall(1));
        System.out.println("Error rate: "+DT.getEvaluation().errorRate());
        System.out.println("% Correct: "+DT.getEvaluation().pctCorrect());
        System.out.println("% Incorrect:  "+DT.getEvaluation().pctIncorrect());*/
        System.out.println("----------------------\n");

        //sendMSG("NN,TR"+TrainDataSVM.size()+",FM"+NN.getEvaluation().fMeasure(1)+",PR"+NN.getEvaluation().precision(1));

        okSVM = 1;

    }

    void CallSVM(Instances TrainDataSVM) throws Exception {


        SMO smo = new SMO();
        smo.buildClassifier(TrainDataSVM);
        SVM = new Clsi("SVM", smo, TrainDataSVM);
        System.out.println("SVM done");
        System.out.println("F-Measure :" + SVM.getEvaluation().fMeasure(1));
        /*System.out.println("Precision : "+DT.getEvaluation().precision(1));
        System.out.println("Recall: "+DT.getEvaluation().recall(1));
        System.out.println("Error rate: "+DT.getEvaluation().errorRate());
        System.out.println("% Correct: "+DT.getEvaluation().pctCorrect());
        System.out.println("% Incorrect:  "+DT.getEvaluation().pctIncorrect());*/
        System.out.println("----------------------\n");

        //sendMSG("SVM,TR"+TrainDataSVM.size()+",FM"+SVM.getEvaluation().fMeasure(1)+",PR"+SVM.getEvaluation().precision(1));


        okNN = 1;


    }

    /////////////////////////////////////////////////////////////////

    //get attacks from database
    public static ArrayList<Attack> getAttcks() throws Exception {

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("Test");

        DBCollection collection = database.getCollection("Attacks");
        DBCursor cursor = collection.find();
        ArrayList<DBObject> arrayList = (ArrayList<DBObject>) cursor.toArray();


        ArrayList<Attack> attacks = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            attacks.add(new Attack(String.valueOf(arrayList.get(i).get("id")), String.valueOf(arrayList.get(i).get("name")), String.valueOf(arrayList.get(i).get("category"))));
        }
        return attacks;
    }



    //get training datasets from database
    public static Instances getTrainDataset(String CollectionName) throws Exception {

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("Test");

        DBCollection collection = database.getCollection(CollectionName);
        DBCursor cursor = collection.find();
        ArrayList<DBObject> arrayList = (ArrayList<DBObject>) cursor.toArray();

        Instances instances = new DataSource("trainsmpl.arff").getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);
        instances.clear();

        for (int i = 0; i < arrayList.size(); i++) {
            PacketDT packetDT = new PacketDT(arrayList.get(i));
            instances.add(packetDT.toInstance(instances));
        }
        return instances;
    }

    public void Classify() throws Exception {

        Instances TrainDataDT = getTrainDataset("PacketsTrainDT");
        Instances TrainDataSVM = getTrainDataset("PacketsTrainSVMNN");
        okDT=0;
        okSVM=0;
        okNN=0;

        attacks = getAttcks();

        CallDT(TrainDataDT);
        CallSVM(TrainDataSVM);
        CallNN(TrainDataSVM);

        if (okDT == 1 && okSVM == 1 && okNN == 1) {



            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("ClassifReady");
            AID dest = null;
            dest = new AID("ManagerAgent", AID.ISLOCALNAME);
            msg.addReceiver(dest);
            send(msg);

            try {
                PlatformPara.messages.add(new Message(msg.getSender().getLocalName(),"ManagerAgent",msg.getContent()));
                Thread.sleep(50);
                //PlatformPara.NotifyMessages(new Message(msg.getSender().getLocalName(),"ManagerAgent",msg.getContent()),0);
            } catch (Exception e) {
                e.printStackTrace();
            }



        }


    }


    public int UpdateClassifiers() throws Exception {

        Instances TrainDataDT = getTrainDataset("PacketsTrainDT");
        Instances TrainDataSVM = getTrainDataset("PacketsTrainSVMNN");
        okDT=0;
        okSVM=0;
        okNN=0;

        attacks = getAttcks();

        CallDT(TrainDataDT);
        CallSVM(TrainDataSVM);
        CallNN(TrainDataSVM);

        if (okDT == 1 && okSVM == 1 && okNN == 1) {

            return 1;
        }


        return 0;
    }



    public static String methode(){
        LocalDateTime localDateTime = LocalDateTime.now();
        if(String.valueOf(localDateTime.getMinute()).length()==1){
            return localDateTime.getYear()+"_"+localDateTime.getMonthValue()+"_"+localDateTime.getDayOfMonth()+
                    "_"+localDateTime.getHour()+"_O"+localDateTime.getMinute()+"_"+localDateTime.getSecond();
        }

        return localDateTime.getYear()+"_"+localDateTime.getMonthValue()+"_"+localDateTime.getDayOfMonth()+
                "_"+localDateTime.getHour()+"_"+localDateTime.getMinute()+"_"+localDateTime.getSecond();
    }


}