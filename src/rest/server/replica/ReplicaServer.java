package rest.server.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import rest.server.model.ClientTransferRequest;
import rest.server.model.ReplicaResponse;
import rest.server.model.User;
import rest.server.model.WalletOperationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
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
                    Long id;
                    Double amount;

                    id = (Long) objIn.readObject();
                    amount = (Double) objIn.readObject();
                    long nonce = (Long) objIn.readObject();

                    appRes = addMoney(id, amount, nonce);
                    objOut.writeObject(appRes);

                    break;

                case TRANSFER_MONEY:
                    Long idTransfer;
                    Double amountTransfer;
                    Long destination;

                    idTransfer = (Long) objIn.readObject();
                    amountTransfer = (Double) objIn.readObject();
                    destination = (Long) objIn.readObject();
                    long nonceTansfer = (Long) objIn.readObject();

                    ClientTransferRequest cliRequest = new ClientTransferRequest();

                    appRes = transferMoney(cliRequest);
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

    private ReplicaResponse addMoney(Long id, Double amount, long nonce) {
/*
        if (!db.containsKey(id)) {
            logger.warning("No money generated. User does " + id + " not exist");
            return new ReplicaResponse(404, "User does" + id + " not exist", null, 0L);

        } else {
            if (amount != null) {
                if (amount > 0) {
                    User user = db.get(id);
                    user.addMoney(amount);

                    logger.info(amount + " generated to user " + user);
                    return new ReplicaResponse(200, "Success", null, (nonce + 1));
                } else {
                    logger.warning("No money generated. Amount must not be negative");
                    return new ReplicaResponse(400, "Amount must not be negative", null, 0L);
                }
            } else {
                logger.warning("Amount parameter not present");
                return new ReplicaResponse(400, "Amount parameter not present", null, 0L);
            }
        }
         */
        return null;
    }

    private ReplicaResponse transferMoney(ClientTransferRequest cliRequest) {

        /*
        if (!db.containsKey(id)) {
            logger.warning("No money transferred. User does " + id + " not exist");
            return new ReplicaResponse(404, "User does" + id + " not exist", null, 0L);
        } else {
            if (amount != null && destination != null) {
                if (db.containsKey(destination)) {
                    User from = db.get(id);

                    if (from.canTransfer(amount)) {
                        User to = db.get(destination);

                        to.addMoney(amount);
                        from.substractMoney(amount);

                        logger.info(amount + " transferred from " + from + "to " + destination);
                        return new ReplicaResponse(200, "Success", null, (nonce + 1));
                    } else {
                        logger.warning("No money transferred. No money available in account");
                        return new ReplicaResponse(400, "No money available in account", null, 0L);
                    }
                } else {
                    logger.warning("No money transferred. User " + destination + " does not exist");
                    return new ReplicaResponse(404, "No money transferred. User " + destination + " does not exist", null, 0L);
                }
            } else {
                logger.warning("Bad request. Some arameters are missing");
                return new ReplicaResponse(400, "Bad request. Some arameters are missing", null, 0L);
            }
        }*/
        return null;
    }
}
