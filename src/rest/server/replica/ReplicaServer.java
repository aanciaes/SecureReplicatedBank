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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientCreateRequest;
import rest.server.model.ClientSumRequest;
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
                case CREATE_ACCOUNT:
                    ClientCreateRequest cliRequestCreate = (ClientCreateRequest) objIn.readObject();
                    long nonce = (Long) objIn.readObject();

                    appRes = createAccount(cliRequestCreate, nonce, reqType);
                    objOut.writeObject(appRes);

                    break;

                case TRANSFER_MONEY:

                    ClientTransferRequest cliRequestTransfer = (ClientTransferRequest) objIn.readObject();
                    long nonceTransfer = (Long) objIn.readObject();

                    appRes = transferMoney(cliRequestTransfer, nonceTransfer, reqType);
                    objOut.writeObject(appRes);

                    break;

                case SUM:
                    ClientSumRequest cliRequestSum = (ClientSumRequest) objIn.readObject();
                    long nonceSum = (Long) objIn.readObject();

                    appRes = sum(cliRequestSum, nonceSum, reqType);
                    objOut.writeObject(appRes);

                    break;
                case SET_BALANCE:
                    ClientCreateRequest cliSetRequest = (ClientCreateRequest) objIn.readObject();
                    long nonceSet = (Long) objIn.readObject();

                    appRes = setBalance(cliSetRequest, nonceSet, reqType);
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

    private ReplicaResponse setBalance(ClientCreateRequest cliSetRequest, long nonce, WalletOperationType operationType) {
        db.put(cliSetRequest.getToPubKey(), new TypedValue(cliSetRequest.getTypedValue().getAmount(), cliSetRequest.getTypedValue().getType()));
        return new ReplicaResponse(200, "Success", forceError(db.get(cliSetRequest.getToPubKey())), (nonce + 1), operationType);
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
                    long nonceGetBalance = (Long) objIn.readObject();

                    appRes = getBalance(userPublicKey, nonceGetBalance, reqType);
                    objOut.writeObject(appRes);

                    break;

                case GET_BETWEEN:
                    DataType dataType = (DataType) objIn.readObject();
                    long lowest = (long) objIn.readObject();
                    long highest = (long) objIn.readObject();
                    boolean hasKeyPrefix = (boolean) objIn.readObject();
                    String keyPrefix = null;

                    if (hasKeyPrefix) {
                        keyPrefix = (String) objIn.readObject();
                    }

                    long nonceGetBetween = (Long) objIn.readObject();

                    appRes = getBetween(dataType, keyPrefix, lowest, highest, nonceGetBetween, reqType);
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
            logger.error("Occurred during map operation execution", e);
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

    private ReplicaResponse getBetween(DataType dataType, String keyPrefix, Long lowest, Long highest, long nonce, WalletOperationType operationType) {
        List<String> rst = new ArrayList();

        if (dataType != DataType.HOMO_ADD) {
            db.forEach((String key, TypedValue typedValue) -> {
                if (keyPrefix != null) {
                    if (typedValue.getType() == dataType && key.startsWith(keyPrefix)) {
                        if (typedValue.getAmountAsLong() <= highest && typedValue.getAmountAsLong() >= lowest) {
                            rst.add(key);
                        }
                    }
                } else {
                    if (typedValue.getType() == dataType) {
                        if (typedValue.getAmountAsLong() <= highest && typedValue.getAmountAsLong() >= lowest) {
                            rst.add(key);
                        }
                    }
                }
            });

            return new ReplicaResponse(200, "Success", rst, (nonce + 1), operationType);
        } else {
            //TODO: Implement with SGX

            return new ReplicaResponse(400, "Not supported yet", null, (nonce + 1), operationType);
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
    private ReplicaResponse createAccount(ClientCreateRequest cliRequest, Long nonce, WalletOperationType operationType) {
        TypedValue requestTv = cliRequest.getTypedValue();

        switch (requestTv.getType()) {
            case WALLET:
                return createWallet(cliRequest, nonce, operationType);
            case HOMO_ADD:
                return createHomoAdd(cliRequest, nonce, operationType);
            case HOMO_OPE_INT:
                return createHomoOpeInt(cliRequest, nonce, operationType);
            default:
                return new ReplicaResponse(400, "Invalid DataType: " + requestTv.getType(), null, 0L, null);
        }
    }

    @SuppressWarnings("Duplicates")
    private ReplicaResponse createWallet(ClientCreateRequest cliRequest, Long nonce, WalletOperationType operationType) {
        // Creates new destination user, if not exists
        if (!db.containsKey(cliRequest.getToPubKey())) {
            db.put(cliRequest.getToPubKey(), new TypedValue(cliRequest.getTypedValue().getAmount(), DataType.WALLET));

            return new ReplicaResponse(200, "Success", cliRequest.getTypedValue().getAmount(), nonce + 1, operationType);
        }

        double amount = cliRequest.getTypedValue().getAmountAsDouble();

        if (amount > 0) {
            cliRequest.setTypedValue(forceError(cliRequest.getTypedValue()));

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
    private ReplicaResponse createHomoAdd(ClientCreateRequest cliRequest, Long nonce, WalletOperationType operationType) {
        // Creates new destination user, if not exists
        if (!db.containsKey(cliRequest.getToPubKey())) {
            db.put(cliRequest.getToPubKey(), new TypedValue(cliRequest.getTypedValue().getAmount(), DataType.HOMO_ADD));

            return new ReplicaResponse(200, "Success", cliRequest.getTypedValue().getAmount(), nonce + 1, operationType);
        } else {
            BigInteger amount = cliRequest.getTypedValue().getAmountAsBigInteger();

            cliRequest.setTypedValue(forceError(cliRequest.getTypedValue()));
            TypedValue clientTv = db.get(cliRequest.getToPubKey());
            BigInteger balance = clientTv.getAmountAsBigInteger();
            clientTv.setAmount((HomoAdd.sum(balance, amount, new BigInteger(cliRequest.getToPubKey()))).toString());

            db.put(cliRequest.getToPubKey(), clientTv);
            logger.debug(amount + " generated to user " + cliRequest.getToPubKey());
            return new ReplicaResponse(200, "Success", amount, nonce + 1, operationType);
        }
    }

    private ReplicaResponse createHomoOpeInt(ClientCreateRequest cliRequest, Long nonce, WalletOperationType operationType) {
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

    private ReplicaResponse sum(ClientSumRequest sumRequest, Long nonce, WalletOperationType operationType) {
        if (db.containsKey(sumRequest.getUserIdentifier())) {
            TypedValue storedTv = db.get(sumRequest.getUserIdentifier());

            if (storedTv.getType() == sumRequest.getTypedValue().getType()) {

                switch (sumRequest.getTypedValue().getType()) {
                    case HOMO_ADD:
                        BigInteger homoAddResult = HomoAdd.sum(storedTv.getAmountAsBigInteger(), sumRequest.getTypedValue().getAmountAsBigInteger(), new BigInteger(sumRequest.getNsquare()));
                        storedTv.setAmountAsBigInteger(homoAddResult);
                        return new ReplicaResponse(200, "Success", storedTv, nonce + 1, operationType);

                    case WALLET:
                        Double walletResult = sumRequest.getTypedValue().getAmountAsDouble() + storedTv.getAmountAsDouble();
                        storedTv.setAmountAsDouble(walletResult);
                        return new ReplicaResponse(200, "Success", storedTv, nonce + 1, operationType);

                    case HOMO_OPE_INT:
                        //TODO: Implement sum for ope int
                        return new ReplicaResponse(500, "Not implemented yet - SGX", null, 0L, null);

                    default:
                        return new ReplicaResponse(400, "Unknown data type", null, 0L, null);
                }
            } else {
                return new ReplicaResponse(400, "Account is not of type expected type", null, 0L, null);
            }
        } else {
            return new ReplicaResponse(400, "Account does not exist: " + sumRequest.getUserIdentifier(), null, 0L, null);
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
