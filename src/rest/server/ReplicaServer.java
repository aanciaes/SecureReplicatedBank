package rest.server;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import rest.server.model.ApplicationResponse;
import rest.server.model.User;

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

    private Map<Long, User> db = new ConcurrentHashMap<>();

    private ServiceReplica serviceReplica;
    private Logger logger = Logger.getLogger(ReplicaServer.class.getName());

    ReplicaServer(int id) {
        db.put(1L, new User(1L, 0.0));
        db.put(2L, new User(2L, 0.0));

        serviceReplica = new ServiceReplica(id, this, this);
    }

    @Override
    public void installSnapshot(byte[] bytes) {

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

            BankServerResources.Operation reqType = (BankServerResources.Operation) objIn.readObject();
            ApplicationResponse appRes;

            switch (reqType) {
                case GENERATE_MONEY:
                    Long id;
                    Double amount;

                    id = (Long) objIn.readObject();
                    amount = (Double) objIn.readObject();

                    appRes = addMoney(id, amount);
                    objOut.writeObject(appRes);

                    break;

                case TRANSFER_MONEY:
                    Long idTransfer;
                    Double amountTransfer;
                    Long destination;

                    idTransfer = (Long) objIn.readObject();
                    amountTransfer = (Double) objIn.readObject();
                    destination = (Long) objIn.readObject();

                    appRes = transferMoney(idTransfer, amountTransfer, destination);
                    objOut.writeObject(appRes);

                    break;

                default:
                    appRes = new ApplicationResponse(400, "Operation Unknown");
                    objOut.writeObject(appRes);
            }

            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during map operation execution", e);
        }
        return reply;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        System.out.println("Executed Unordered");
        return new byte[0];
    }


    public User[] listUsers() {
        User[] list = new User[db.size()];

        return db.values().toArray(list);
    }

    private ApplicationResponse addMoney(Long id, Double amount) {

        if (!db.containsKey(id)) {
            System.err.println("No money generated. User does " + id + " not exist");
            return new ApplicationResponse(404, "User does" + id + " not exist");

        } else {
            if (amount != null) {

                if (amount > 0) {
                    User user = db.get(id);
                    user.addMoney(amount);

                    return new ApplicationResponse(200, "Success");
                } else {
                    return new ApplicationResponse(400, "Amount must not be negative");
                }
            } else {
                return new ApplicationResponse(400, "No amount exists");
            }
        }
    }

    private ApplicationResponse transferMoney(Long id, Double amount, Long destination) {
        if (!db.containsKey(id)) {
            System.err.println("No money transferred. User does " + id + " not exist");
            return new ApplicationResponse(404, "User does" + id + " not exist");
        } else {
            if (amount != null && destination != null) {
                if (db.containsKey(destination)) {
                    User from = db.get(id);

                    if (from.canTransfer(amount)) {
                        User to = db.get(destination);

                        to.addMoney(amount);
                        from.substractMoney(amount);

                        return new ApplicationResponse(200, "Success");
                    } else {
                        System.err.println("No money transferred. No money available in account");
                        return new ApplicationResponse(400, "No money available in account");
                    }

                } else {
                    System.err.println("No money transferred. User " + destination + " does not exist");
                    return new ApplicationResponse(404, "No money transferred. User " + destination + " does not exist");
                }
            } else {
                return new ApplicationResponse(400, "Failure");
            }
        }
    }
}
