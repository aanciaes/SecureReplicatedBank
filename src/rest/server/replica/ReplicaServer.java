package rest.server.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import hlib.hj.mlib.HomoAdd;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientTransferRequest;
import rest.server.model.DataType;
import rest.server.model.ReplicaResponse;
import rest.server.model.TypedValue;
import rest.server.model.WalletOperationType;

/**
 * Represents the replica. This is the class that holds all the data of the system, currently saved in memory
 */
public class ReplicaServer extends DefaultSingleRecoverable {

    private static Logger logger = LogManager.getLogger(ReplicaServer.class.getName());

    private Map<String, TypedValue> db = new ConcurrentHashMap<>();
    private boolean unpredictable;

    public ReplicaServer(int id, boolean unpredictable) {
        this.unpredictable = unpredictable;
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

                    appRes = addMoney(cliAddRequest, nonce, reqType);
                    objOut.writeObject(appRes);

                    break;

                case TRANSFER_MONEY:

                    ClientTransferRequest cliRequest = (ClientTransferRequest) objIn.readObject();
                    long nonceTransfer = (Long) objIn.readObject();

                    appRes = transferMoney(cliRequest, nonceTransfer, reqType);
                    objOut.writeObject(appRes);

                    break;

                default:
                    appRes = new ReplicaResponse(400, "Operation Unknown", null, 0L, null);
                    objOut.writeObject(appRes);
                    logger.error("Operation Unknown for Ordered op: " + reqType);
            }

            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Ocurred during map operation execution", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
                case GET_BALANCE:
                    String userPublicKey = (String) objIn.readObject();
                    long nonce = (Long) objIn.readObject();

                    appRes = getBalance(userPublicKey, nonce, reqType);
                    objOut.writeObject(appRes);

                    break;
                default:
                    logger.error("Operation Unknown for Unordered op: " + reqType);
                    appRes = new ReplicaResponse(400, "Operation Unknown", null, 0L, null);
                    objOut.writeObject(appRes);
            }

            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Ocurred during map operation execution", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

        return reply;
    }

    /**
     * Returns the balance of a user
     *
     * @param userPublicKey User
     * @param nonce         Nonce of the operation
     * @param operationType Type of the operation
     * @return Replica response containing the balance of the user
     */
    private ReplicaResponse getBalance(String userPublicKey, Long nonce, WalletOperationType operationType) {
        if (db.containsKey(userPublicKey)) {
            return new ReplicaResponse(200, "Success", forceError(db.get(userPublicKey)), (nonce + 1), operationType);
        } else {
            return new ReplicaResponse(404, "User does not exist", null, 0L, null);
        }
    }

    /**
     * Generates some money to a user
     *
     * @param cliRequest    Client Request containing the destination user and the amount among other information
     * @param nonce         Nonce of the operation
     * @param operationType Type of the operation
     * @return Replica response containing the new balance of the user
     */
    private ReplicaResponse addMoney(ClientAddMoneyRequest cliRequest, Long nonce, WalletOperationType operationType) {
        double amount = 0.0;
        TypedValue requestTv = cliRequest.getTypedValue();

        switch (requestTv.getType()) {
            case WALLET:
                return walletAddMoney(cliRequest, nonce, operationType);
            case HOMO_ADD:
                return homoAddSum(cliRequest, nonce, operationType);
            case HOMO_OPE_INT:
                return homoOpeIntCreate(cliRequest, nonce, operationType);
            default:
                return new ReplicaResponse(400, "Invalid DataType: " + requestTv.getType(), null, 0L, null);
        }
    }

    @SuppressWarnings("Duplicates")
    private ReplicaResponse walletAddMoney(ClientAddMoneyRequest cliRequest, Long nonce, WalletOperationType operationType) {
        // Creates new destination user, if not exists
        if (!db.containsKey(cliRequest.getToPubKey())) {
            db.put(cliRequest.getToPubKey(), new TypedValue(cliRequest.getTypedValue().getAmount(), DataType.WALLET));
        }

        double amount = cliRequest.getTypedValue().getAmountAsDouble();

        if (amount > 0) {
            cliRequest.setAmount(forceError(cliRequest.getTypedValue()));

            TypedValue clientTv = db.get(cliRequest.getToPubKey());
            double balance = clientTv.getAmountAsDouble();
            clientTv.setAmount(((Double) (balance + amount)).toString());

            db.put(cliRequest.getToPubKey(), clientTv);

            logger.debug(amount + " generated to user " + cliRequest.getToPubKey());
            return new ReplicaResponse(200, "Success", amount, nonce + 1, operationType);
        } else {
            logger.warn("No money generated. Amount must not be negative");
            return new ReplicaResponse(400, "Amount must not be negative", null, 0L, null);
        }
    }

    @SuppressWarnings("Duplicates")
    private ReplicaResponse homoAddSum(ClientAddMoneyRequest cliRequest, Long nonce, WalletOperationType operationType) {
        // Creates new destination user, if not exists
        if (!db.containsKey(cliRequest.getToPubKey())) {
            db.put(cliRequest.getToPubKey(), new TypedValue(cliRequest.getTypedValue().getAmount(), DataType.HOMO_ADD));

            return new ReplicaResponse(200, "Success", cliRequest.getTypedValue().getAmount(), nonce + 1, operationType);
        } else {
            BigInteger amount = cliRequest.getTypedValue().getAmountAsBigInteger();

            cliRequest.setAmount(forceError(cliRequest.getTypedValue()));
            TypedValue clientTv = db.get(cliRequest.getToPubKey());
            BigInteger balance = clientTv.getAmountAsBigInteger();
            clientTv.setAmount((HomoAdd.sum(balance, amount, new BigInteger(cliRequest.getToPubKey()))).toString());

            db.put(cliRequest.getToPubKey(), clientTv);
            logger.debug(amount + " generated to user " + cliRequest.getToPubKey());
            return new ReplicaResponse(200, "Success", amount, nonce + 1, operationType);
        }
    }

    private ReplicaResponse homoOpeIntCreate(ClientAddMoneyRequest cliRequest, Long nonce, WalletOperationType operationType) {
        // Creates new destination user, if not exists
        if (!db.containsKey(cliRequest.getToPubKey())) {
            db.put(cliRequest.getToPubKey(), new TypedValue(cliRequest.getTypedValue().getAmount(), DataType.HOMO_OPE_INT));
            return new ReplicaResponse(200, "Success", cliRequest.getTypedValue().getAmount(), nonce + 1, operationType);
        } else {
            //Homo Ope Int type cannot add money to an existing account
            return new ReplicaResponse(400, "Operation not supported", null, nonce + 1, operationType);
        }
    }

    /**
     * Transfers some money from a source user to a destination user
     *
     * @param cliRequest    Client Request containing the source and destination user and the amount among other information
     * @param nonce
     * @param operationType
     * @return
     */
    private ReplicaResponse transferMoney(ClientTransferRequest cliRequest, Long nonce, WalletOperationType operationType) {
        if (cliRequest.getTypedValue().getType() != DataType.WALLET) {
            // Transfers are not supported for data types Homo Add and Homo Ope Int
            return new ReplicaResponse(400, "Operation not supported", null, 0L, null);
        }
        if (!db.containsKey(cliRequest.getFromPubKey())) {
            logger.warn("No money transferred. User does not exist");
            return new ReplicaResponse(404, "User does not exist", null, 0L, null);
        } else {
            double amount = cliRequest.getTypedValue().getAmountAsDouble();

            if (amount > 0) {
                if (db.get(cliRequest.getToPubKey()).getAmountAsDouble() - amount >= 0) {
                    if (!db.containsKey(cliRequest.getToPubKey())) {
                        db.put(cliRequest.getToPubKey(), new TypedValue(cliRequest.getAmount(), DataType.WALLET));
                        return new ReplicaResponse(200, "Success", db.get(cliRequest.getFromPubKey()), nonce + 1, operationType);
                    } else {
                        // Force error
                        cliRequest.setAmount(forceError(cliRequest.getTypedValue()));
                        performAtomicTransfer(cliRequest.getFromPubKey(), cliRequest.getToPubKey(), amount);

                        logger.debug("Balance after transfer " + db.get(cliRequest.getFromPubKey()));
                        return new ReplicaResponse(200, "Success", db.get(cliRequest.getFromPubKey()), nonce + 1, operationType);
                    }
                } else {
                    logger.warn("No money transferred. No money available in account");
                    return new ReplicaResponse(400, "No money available in account", null, 0L, null);
                }
            } else {
                logger.warn("No money transferred. Amount must not be negative");
                return new ReplicaResponse(400, "Amount must not be negative", null, 0L, null);
            }
        }
    }

    // For debug purposes only. Return all users of the current server directly
    public Map<String, TypedValue> getAllNoConsensus() {
        return db;
    }

    /**
     * Performs the actual money transferring in an "atomic" way
     *
     * @param from   Public key of the source user
     * @param to     Public key of the destination user
     * @param amount Amount to transfer
     */
    private synchronized void performAtomicTransfer(String from, String to, Double amount) {
        db.get(from).setAmountAsDouble(db.get(from).getAmountAsDouble() - amount);
        db.get(to).setAmountAsDouble(db.get(from).getAmountAsDouble() + amount);
    }

    /**
     * Forces an error of an amount if in unpredictable mode.
     *
     * @param tv Amount to be changed or not depending on unpredictable mode and probability
     * @return Wrong amount or right amount depending on unpredictable mode and probability
     */
    private TypedValue forceError(TypedValue tv) {

        if (unpredictable) {
            Random r = new Random();
            int low = 0;
            int high = 4;
            int result = r.nextInt(high - low) + low;

            // 20% of probability of error
            if (result == 0) {
                tv.setAmount(tv.getAmount() + "2");
                return tv;
            } else {
                return tv;
            }
        } else {
            return tv;
        }
    }
}
