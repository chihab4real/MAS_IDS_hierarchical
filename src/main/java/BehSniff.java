import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.Random;


public class BehSniff extends TickerBehaviour {

    int index;
    Instances TestData;

    public BehSniff(Agent a,int index,Instances testData) {


        super(a, new Random().nextInt((900 - 100) + 1) + 100);

        this.index =index;
        this.TestData = testData;

    }

    @Override
    protected void onTick() {



        if(ManagerAgent.containers.get(index).isSniffer_working()){


        PacketSniffer packetSniffer = new PacketSniffer(TestData.get(new Random().nextInt(TestData.size()-1)),false);




        ManagerAgent.containers.get(index).getPacketsDetected().add(packetSniffer);
        ManagerAgent.containers.get(index).getAll().add(packetSniffer);




            int rand = new Random().nextInt((900 - 100) + 1) + 100;
            reset(rand);

        }

    }
}
