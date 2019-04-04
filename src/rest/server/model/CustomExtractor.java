package rest.server.model;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;
import bftsmart.tom.util.KeyLoader;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CustomExtractor implements Extractor {

    private ExtractorMessage lastRound;
    private KeyLoader kl;

    public CustomExtractor(KeyLoader keyLoader) {
        this.kl = keyLoader;
    }

    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {
        lastRound = new ExtractorMessage(tomMessages, sameContent, lastReceived);

        try {
            PublicKey pk = kl.loadPublicKey(0);
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);

            sig.update(tomMessages[0].serializedMessage);
            System.out.println(sig.verify(tomMessages[0].serializedMessageSignature));

            PublicKey pk1 = kl.loadPublicKey(1);
            Signature sig1 = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig1.initVerify(pk1);

            sig1.update(tomMessages[1].serializedMessage);
            System.out.println(sig1.verify(tomMessages[1].serializedMessageSignature));

        }catch (Exception e ){
            e.printStackTrace();
        }

        return tomMessages[lastReceived];
    }

    public ExtractorMessage getLastRound() {
        return lastRound;
    }
}