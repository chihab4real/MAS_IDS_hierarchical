import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;


public class CreateSnifferBehaviour extends OneShotBehaviour {
    @Override
    public void action() {





        String containername = "";


        try {
            containername = getAgent().getContainerController().getContainerName();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        

        AgentController agentController = null;
        try {
            agentController = getAgent().getContainerController().createNewAgent("SnifferAgent_"+containername,"SnifferAgent",null);
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }






    }
}
