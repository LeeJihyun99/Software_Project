package server.protocol.aktionen;

import server.protocol.Message;

public class CheckpointMoved extends Message {
    private String messageType;
    private MessageBody messageBody;

    public CheckpointMoved(int checkpointID, int x, int y){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(checkpointID, x, y);
    }

    private class MessageBody{
        int checkpointID;
        int x;
        int y;

        public MessageBody(int checkpointID, int x, int y) {
            this.checkpointID = checkpointID;
            this.x = x;
            this.y = y;
        }

        public int getCheckpointID() {
            return checkpointID;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public int getCheckpointID() {
        return messageBody.getCheckpointID();
    }

    public int getX() {
        return messageBody.getX();
    }

    public int getY() {
        return messageBody.getY();
    }
}
