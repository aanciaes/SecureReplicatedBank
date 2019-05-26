package rest.server.model;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the bft smart extractor in order to save all responses from all the replicas
 * All extractor messages will be save with a long as a key. This long identifies a request, so that multiple and concurrent requests all have a different set of messages
 */
public class CustomExtractor implements Extractor {

    private Map<Long, ExtractorMessage> rounds;

    public CustomExtractor() {
        this.rounds = new HashMap();
    }

    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {
        TOMMessage lastMessage = tomMessages[lastReceived];
        Long nonce = extractNonceFromTomMessage(lastMessage);

        rounds.put(nonce, new ExtractorMessage(tomMessages, sameContent, lastReceived));

        return lastMessage;
    }

    public ExtractorMessage getRound(Long id) {
        //Removes entry when the server access it. An entry is only supposed to be used once.
        // This way, it prevents this list to grow to infinity
        return rounds.remove(id);
    }

    private long extractNonceFromTomMessage(TOMMessage message) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(message.getContent());
             ObjectInput objIn = new ObjectInputStream(byteIn)) {

            ReplicaResponse replicaResponse = (ReplicaResponse) objIn.readObject();
            return replicaResponse.getNonce();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}