package rest.server.model;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;

public class CustomExtractor implements Extractor {

    private ExtractorMessage lastRound;

    public CustomExtractor() {
    }

    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {
        lastRound = new ExtractorMessage(tomMessages, sameContent, lastReceived);

        /*try {
            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            PublicKey pk = keyLoader.loadPublicKey(tomMessages[0].getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);

            sig.update(tomMessages[0].serializedMessage);
            System.out.println(sig.verify(tomMessages[0].serializedMessageSignature));

            PublicKey pk1 = keyLoader.loadPublicKey(tomMessages[1].getSender());
            Signature sig1 = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig1.initVerify(pk1);

            sig1.update(tomMessages[1].serializedMessage);
            System.out.println(sig1.verify(tomMessages[1].serializedMessageSignature));

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return tomMessages[lastReceived];
    }

    public ExtractorMessage getLastRound() {
        return lastRound;
    }
}