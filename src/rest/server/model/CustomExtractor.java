package rest.server.model;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;

import javax.ws.rs.WebApplicationException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;

public class CustomExtractor implements Extractor {

    private ExtractorMessage lastRound;

    public CustomExtractor() {
    }

    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {
        lastRound = new ExtractorMessage(tomMessages, sameContent, lastReceived);

        return tomMessages[lastReceived];
    }

    public ExtractorMessage getLastRound() {
        return lastRound;
    }
}