package rest.server.model;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;

public class CustomExtractor implements Extractor {

    private ExtractorMessage lastRound;

    public CustomExtractor() {
    }

    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {
        //TODO
        lastRound = new ExtractorMessage(tomMessages, sameContent, lastReceived);
        return tomMessages[lastReceived];
    }

    public ExtractorMessage getLastRound() {
        return lastRound;
    }
}