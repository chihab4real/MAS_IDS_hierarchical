import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class TestAgent extends Agent {
    @Override
    protected void setup() {


        addBehaviour(new TickerBehaviour(this,2000) {
            @Override
            protected void onTick() {
                System.out.println("Test");
            }
        });

    }
}
