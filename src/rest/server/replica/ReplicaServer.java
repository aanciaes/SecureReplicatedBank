package rest.server.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientTransferRequest;
import rest.server.model.ReplicaResponse;
import rest.server.model.WalletOperationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaServer extends DefaultSingleRecoverable {

    private Map<String, Double> db = new ConcurrentHashMap<>();
    private Logger logger = Logger.getLogger(ReplicaServer.class.getName());

    public ReplicaServer(int id) {
        /*User u1 = new User(1, 0.0);
        User u2 = new User(2, 0.0);
        db.put(u1.getPublicKey(), u1);
        db.put(u2.getPublicKey(), u2);*/
        //db.put("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCpH3RUXrTw/VLJAZOJAJKYmwFEdzV7Jt57BkSGfVXn8//3zXr0QASDOYfKjqlx1A/+N2/jl0WGU77AMY7w3tVBWzqMYsHiCsk49om4MNkhNRdaSsokasq/6yGDxZXDq+J0Gsks8Mc+eYKxEAvtoWnnfN5WJ1M6HXeAK1Zn7n4rjQIDAQAB", 99999.9);

        new ServiceReplica(id, this, this);
        logger.info("Replica Server #" + id + " started");
    }

    @Override
    public void installSnapshot(byte[] bytes) {
        System.out.println("badjoraz");
    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        byte[] reply = null;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            WalletOperationType reqType = (WalletOperationType) objIn.readObject();
            ReplicaResponse appRes;

            switch (reqType) {
                case GENERATE_MONEY:
                    ClientAddMoneyRequest cliAddRequest = (ClientAddMoneyRequest) objIn.readObject();
                    long nonce = (Long) objIn.readObject();

                    appRes = addMoney(cliAddRequest, nonce);
                    objOut.writeObject(appRes);

                    break;

                case TRANSFER_MONEY:

                    ClientTransferRequest cliRequest = (ClientTransferRequest) objIn.readObject();
                    long nonceTansfer = (Long) objIn.readObject();

                    appRes = transferMoney(cliRequest, nonceTansfer);
                    objOut.writeObject(appRes);

                    break;

                default:
                    appRes = new ReplicaResponse(400, "Operation Unknown", null, 0L);
                    objOut.writeObject(appRes);
            }

            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during map operation execution", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return reply;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        byte[] reply = null;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            WalletOperationType reqType = (WalletOperationType) objIn.readObject();
            ReplicaResponse appRes;

            switch (reqType) {
                case GET_ALL:
                    long nonce = (Long) objIn.readObject();

                    appRes = listUsers(nonce);
                    objOut.writeObject(appRes);

                    break;
                default:
                    logger.log(Level.SEVERE, "Operation Unknown");
                    appRes = new ReplicaResponse(400, "Operation Unknown", null, 0L);
                    objOut.writeObject(appRes);
            }

            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during map operation execution", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return reply;
    }


    private ReplicaResponse listUsers(long nonce) {
        return new ReplicaResponse(200, "Sucess", db, (nonce + 1));
    }

    private ReplicaResponse addMoney(ClientAddMoneyRequest cliRequest, Long nonce) {

        // Creates new destination user, if not exists
        if (!db.containsKey(cliRequest.getToPubKey())) {
            db.put(cliRequest.getToPubKey(), 0.0);
        }
        if (cliRequest.getAmount() > 0) {
            db.put(cliRequest.getToPubKey(), db.get(cliRequest.getToPubKey()) + cliRequest.getAmount());

            logger.info(cliRequest.getAmount() + " generated to user " + cliRequest.getToPubKey());
            return new ReplicaResponse(200, "Success", cliRequest.getAmount(), nonce + 1);
        } else {
            logger.warning("No money generated. Amount must not be negative");
            return new ReplicaResponse(400, "Amount must not be negative", null, 0L);
        }
    }

    private ReplicaResponse transferMoney(ClientTransferRequest cliRequest, Long nonce) {

        if (!db.containsKey(cliRequest.getFromPubKey())) {
            logger.warning("No money transferred. User does not exist");
            return new ReplicaResponse(404, "User does not exist", null, 0L);
        } else {
            if (cliRequest.getAmount() > 0) {
                if (!db.containsKey(cliRequest.getToPubKey())) {
                    db.put(cliRequest.getToPubKey(), 0.0);
                }

                Double fromBalance = db.get(cliRequest.getFromPubKey());
                Double toBalance = db.get(cliRequest.getToPubKey());

                if (fromBalance - cliRequest.getAmount() >= 0) {
                    db.put(cliRequest.getFromPubKey(), fromBalance - cliRequest.getAmount());
                    db.put(cliRequest.getToPubKey(), toBalance + cliRequest.getAmount());

                    logger.info("Balance after transfer " + cliRequest.getAmount());
                    return new ReplicaResponse(200, "Success", db.get(cliRequest.getFromPubKey()), nonce + 1);
                } else {
                    logger.warning("No money transferred. No money available in account");
                    return new ReplicaResponse(400, "No money available in account", null, 0L);
                }
            } else {
                logger.warning("No money transferred. Amount must not be negative");
                return new ReplicaResponse(400, "Amount must not be negative", null, 0L);
            }
        }
    }
}
