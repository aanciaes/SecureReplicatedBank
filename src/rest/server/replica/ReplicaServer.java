package rest.server.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import hlib.hj.mlib.HomoAdd;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientConditionalUpd;
import rest.server.model.ClientCreateRequest;
import rest.server.model.ClientSumRequest;
import rest.server.model.ClientTransferRequest;
import rest.server.model.DataType;
import rest.server.model.ReplicaResponse;
import rest.server.model.TypedValue;
import rest.server.model.WalletOperationType;
import rest.sgx.model.GetBetweenResponse;
import rest.sgx.model.SGXApplyUpdateRequest;
import rest.sgx.model.SGXClientSumRequest;
import rest.sgx.model.SGXConditionalUpdateRequest;
import rest.sgx.model.SGXGetBetweenRequest;
import rest.sgx.model.SGXResponse;
import rest.sgx.model.TypedKey;
import rest.utils.Update;
import rest.utils.Utils;

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
                case CONDITIONAL_UPD:
                    ClientConditionalUpd clientConditionalUpd = (ClientConditionalUpd) objIn.readObject();
                    long nonceCond = (Long) objIn.readObject();

                    appRes = conditionalUpd(clientConditionalUpd, nonceCond, reqType);
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
        //Wont change the keys associated with that value

        TypedValue tv = db.get(cliSetRequest.getToPubKey());
        tv.setAmount(cliSetRequest.getTypedValue().getAmount());

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
                    Long lowest = (Long) objIn.readObject();
                    Long highest = (Long) objIn.readObject();
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

    private ReplicaResponse getBetween(DataType dataType, String keyPrefix, Long lowest, Long highest, long nonce, WalletOperationType operationType) throws JsonProcessingException {
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

            return new ReplicaResponse(200, "Success", new ObjectMapper().writer().writeValueAsString(new GetBetweenResponse(rst)), (nonce + 1), operationType);
        } else {
            SGXResponse sgxResponse = sgxGetBetween(keyPrefix, BigInteger.valueOf(lowest), BigInteger.valueOf(highest));
            if (sgxResponse.getStatusCode() == 200) {
                return new ReplicaResponse(sgxResponse.getStatusCode(), "Success", sgxResponse.getBody().toString(), (nonce + 1), operationType);
            }
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
            db.put(cliRequest.getToPubKey(), cliRequest.getTypedValue());

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
            db.put(cliRequest.getToPubKey(), cliRequest.getTypedValue());

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
            db.put(cliRequest.getToPubKey(), cliRequest.getTypedValue());
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
                        db.put(cliRequest.getToPubKey(), cliRequest.getTypedValue());
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

                        SGXResponse sgxResponse = sgxSum(sumRequest);
                        if (sgxResponse.getStatusCode() != 200) {
                            return new ReplicaResponse(sgxResponse.getStatusCode(), sgxResponse.getBody().toString(), null, 0L, null);
                        }
                        String newAmount = new ObjectMapper().convertValue(sgxResponse.getBody(), String.class);
                        storedTv.setAmount(newAmount);

                        return new ReplicaResponse(200, "Success - SGX", storedTv, nonce + 1, operationType);

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

    private ReplicaResponse conditionalUpd(ClientConditionalUpd clientConditionalUpd, long nonce, WalletOperationType operationType) {
        if (db.containsKey(clientConditionalUpd.getCondKey())) {

            if (checkCondition(db.get(clientConditionalUpd.getCondKey()), clientConditionalUpd.getCondValue(), clientConditionalUpd.getCondition())) {
                applyUpdates(clientConditionalUpd.getUpdatesList());
                return new ReplicaResponse(200, "Condition holds, Updates Performed", "Condition holds, Updates Performed", (nonce + 1), operationType);
            } else {
                return new ReplicaResponse(412, "Pre condition failed", null, 0L, null);
            }
        } else {
            return new ReplicaResponse(404, "Account does not exist: " + clientConditionalUpd.getCondKey(), null, 0L, null);
        }
    }

    private boolean checkCondition(TypedValue typedValue, Double condValue, int condition) {
        switch (typedValue.getType()) {
            case WALLET:
                return checkConditionLocally(typedValue.getAmountAsDouble(), condValue, condition);
            case HOMO_ADD:
            case HOMO_OPE_INT:
                return checkConditionOnSecureSgx(typedValue, condValue, condition);
            default:
                return false;
        }
    }

    private boolean checkConditionLocally(Double balance, Double condValue, int condition) {
        switch (condition) {
            case 0:
                return balance.equals(condValue);
            case 1:
                return !balance.equals(condValue);
            case 2:
                return balance > condValue;
            case 3:
                return balance >= condValue;
            case 4:
                return balance < condValue;
            case 5:
                return balance <= condValue;
            default:
                return false;
        }
    }

    private boolean checkConditionOnSecureSgx(TypedValue typedValue, Double condValue, int condition) {

        SGXConditionalUpdateRequest sgxRequest = new SGXConditionalUpdateRequest(typedValue, condValue, condition);

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:6699/sgx").build();
        WebTarget target = client.target(baseURI);
        Gson gson = new Gson();
        String json = gson.toJson(sgxRequest);

        Response response = target.path("/check_condition").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        return response.readEntity(Boolean.class);
    }

    private void applyUpdates(List<Update> updates) {
        for (Update update : updates) {
            TypedValue tv = db.get(update.getUpdKey());

            switch (update.getOp()) {
                case 0:
                    tv.setAmount(update.getValue());
                    break;
                case 1:
                    ClientSumRequest clientSumRequest = new ClientSumRequest();
                    break;
                default:
                    break;
            }
        }
    }

    private void applyUpdateSumOnSecureSgx(Update update, TypedValue typedValue) {
        SGXApplyUpdateRequest updateRequest = new SGXApplyUpdateRequest(typedValue, update.getValue(), update.getOp());

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:6699/sgx").build();
        WebTarget target = client.target(baseURI);
        Gson gson = new Gson();
        String json = gson.toJson(updateRequest);

        Response response = target.path("/applyConditionUpdate").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        SGXResponse sgxResponse = response.readEntity(SGXResponse.class);
        String newBalance = (String) sgxResponse.getBody();
        System.out.println("here: " + newBalance);

        db.get(update.getUpdKey()).setAmount(newBalance);
        System.out.println(db.get(update.getUpdKey()).getAmount());
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

    private SGXResponse sgxGetBetween(String keyPrefix, BigInteger lowest, BigInteger highest) {
        Map<String, TypedValue> toSgx = new HashMap<>();

        db.forEach((String key, TypedValue typedValue) -> {
            if (typedValue.getType() == DataType.HOMO_ADD && (keyPrefix == null || key.startsWith(keyPrefix))) {
                toSgx.put(key, typedValue);
            }
        });

        SGXGetBetweenRequest sgxRequest = new SGXGetBetweenRequest(toSgx, lowest, highest);

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:6699/sgx").build();
        WebTarget target = client.target(baseURI);
        Gson gson = new Gson();
        String json = gson.toJson(sgxRequest);

        Response response = target.path("/getBetween").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        return response.readEntity(SGXResponse.class);
    }


    private SGXResponse sgxSum(ClientSumRequest cliRequest) {
        TypedKey typedKey = new TypedKey(cliRequest.getTypedValue().getType(), db.get(cliRequest.getUserIdentifier()).getEncodedHomoKey());

        long balance = db.get(cliRequest.getUserIdentifier()).getAmountAsLong();
        SGXClientSumRequest sgxClientRequest = new SGXClientSumRequest(typedKey, balance, Long.parseLong(cliRequest.getTypedValue().getAmount()));

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:6699/sgx").build();
        WebTarget target = client.target(baseURI);
        Gson gson = new Gson();
        String json = gson.toJson(sgxClientRequest);

        Response response = target.path("/sum").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        return response.readEntity(SGXResponse.class);
    }
}
