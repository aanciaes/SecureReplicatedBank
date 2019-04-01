package rest.server.model;

import bftsmart.tom.core.messages.TOMMessage;

public class ExtractorMessage {

    private TOMMessage[] tomMessages;
    private int sameContent;
    private int lastReceived;

    public ExtractorMessage(TOMMessage[] tomMessages, int sameContent, int lastReceived) {
        this.tomMessages = tomMessages;
        this.sameContent = sameContent;
        this.lastReceived = lastReceived;
    }

    public TOMMessage[] getTomMessages() {
        return tomMessages;
    }

    public void setTomMessages(TOMMessage[] tomMessages) {
        this.tomMessages = tomMessages;
    }

    public int getSameContent() {
        return sameContent;
    }

    public void setSameContent(int sameContent) {
        this.sameContent = sameContent;
    }

    public int getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(int lastReceived) {
        this.lastReceived = lastReceived;
    }

    public int getNumberOfReplies () {
        return tomMessages.length;
    }
}
