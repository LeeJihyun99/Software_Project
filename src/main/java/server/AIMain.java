package server;

public class AIMain {

    public static void main(String[] args){
        AIHandler aiHandler= new AIHandler();
        aiHandler.setUpConnection();
        aiHandler.handleIncomingMessages();
    }
}
