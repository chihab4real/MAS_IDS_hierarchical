import jade.core.Profile;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    static int index1=0;

    public static void main(String[] args) {


        int time_ex=240;

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(index1>=time_ex){


                    try {
                        PlatformPara.containerController.getPlatformController().kill();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }


                    //System.out.println(ManagerAgent.allPackets.size());
                    try {
                        //sendPackettoDB(ManagerAgent.packetsClassified);
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //System.exit(-1);

                    System.exit(0);

                    //ManagerAgent.stop = true;



                }

                index1++;

            }
        },58000,1000);


        PlatformPara.profile.setParameter(Profile.MAIN_HOST, "localhost");
        PlatformPara.profile.setParameter(Profile.GUI, "true");
        PlatformPara.containerController = PlatformPara.runtime.createMainContainer(PlatformPara.profile);
        PlatformPara.startTime = PlatformPara.methode();




        AgentController agentController = null;
        try {

            agentController = PlatformPara.containerController.createNewAgent("ManagerAgent", "ManagerAgent", null);
            agentController.start();

            agentController = PlatformPara.containerController.createNewAgent("ClassifAgent", "ClassifAgent", null);
            agentController.start();


        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<Integer> howMuchNormal(){


        ArrayList<Integer> arrayList1 = new ArrayList<>();
        arrayList1.add(0);
        arrayList1.add(0);
        for(int i=0;i<ManagerAgent.containers.size();i++){
            ArrayList<PacketDetected> arrayList = ManagerAgent.containers.get(i).getPacketClassified();

            for(int j=0;j<arrayList.size();j++){
                if(arrayList.get(j).getCategory().equals("Normal")){
                    arrayList1.set(0,arrayList1.get(0)+1);
                }else{
                    arrayList1.set(1,arrayList1.get(1)+1);
                }
            }
        }



        return arrayList1;
    }

    public static ArrayList<Integer> howMuchNormal2(Container container){


        ArrayList<Integer> arrayList1 = new ArrayList<>();
        arrayList1.add(0);
        arrayList1.add(0);

        for(int i=0;i<container.getPacketClassified().size();i++){
            if(container.getPacketClassified().get(i).getCategory().equals("Normal")){
                arrayList1.set(0,arrayList1.get(0)+1);
            }else{
                arrayList1.set(1,arrayList1.get(1)+1);
            }
        }


        return arrayList1;
    }




    public static void getSummary() throws Exception{

        String fileName="C:\\Users\\pc\\Desktop\\3IDS_TEST\\H\\STATES"+PlatformPara.startTime+".txt";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        String time_work="Started: "+PlatformPara.startTime+"\nEnded: "+dtf.format(LocalDateTime.now());
        String container="Number of containers: "+ManagerAgent.containers.size();
        String details_of_containers="";

        for(int i=0;i<ManagerAgent.containers.size();i++){
            String de="Container "+i+": ";
            ArrayList<Integer> arrayList = howMuchNormal2(ManagerAgent.containers.get(i));
            int x= arrayList.get(0)+arrayList.get(1);
            de+="\nTotale packets: "+(x);
            de+="\nNormal: "+arrayList.get(0)+" ("+(arrayList.get(0)*100/x)+"%)";
            de+="\nAnomalie: "+arrayList.get(1)+" ("+(arrayList.get(1)*100/x)+"%)";

            details_of_containers+="\n"+de+"\n--------------------------------------";

        }

        String str = "\n"+time_work+"\n"+container+"\n"+details_of_containers;



        try {

            File myObj = new File(fileName);
            if (myObj.createNewFile()) {
                //System.out.println("File created: " + myObj.getName());
            } else {
                //System.out.println("File already exists.");
            }

            FileWriter fileWritter = new FileWriter(fileName,true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            bw.write("\n"+str);
            bw.close();

            //System.out.println("Successfully wrote to the file.");


        } catch (IOException e) {
            //System.out.println("An error occurred.");
            e.printStackTrace();
        }




    }

    public static void printAllMessages(){
        String fileName="C:\\Users\\pc\\Desktop\\3IDS_TEST\\H\\Messages"+PlatformPara.startTime+".txt";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        //DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        String str ="\n\n";
        for(int i=0;i<ManagerAgent.containers.size();i++){
            Message message = PlatformPara.messages.get(i);
            str +="-----------Message: "+i+" -----------"+"\nSender-: "+message.getSender()+"\nReciever-:"+message.getReciever()+
                    "\nContent-:"+message.getContent()+"\nTime and Date-:"+dtf.format(message.getTime())+"\n\n\n";

        }



        try {

            File myObj = new File(fileName);
            if (myObj.createNewFile()) {
                //System.out.println("File created: " + myObj.getName());
            } else {
                //System.out.println("File already exists.");
            }

            FileWriter fileWritter = new FileWriter(fileName,true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            bw.write("\n"+str);
            bw.close();

            //System.out.println("Successfully wrote to the file.");


        } catch (IOException e) {
            //System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}
