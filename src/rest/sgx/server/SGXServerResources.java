package rest.sgx.server;


import rest.server.model.TypedValue;
import rest.sgx.model.SGXClientRequest;
import rest.sgx.model.TypedKey;

import javax.ws.rs.core.HttpHeaders;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SGXServerResources implements SGXServerInterface{
    private Map<String, TypedKey> sgxDb = new HashMap<String, TypedKey>();

    @Override
    public Map<String, TypedKey> listUsers() {
        return sgxDb;
    }

    @Override
    public void create(HttpHeaders headers, SGXClientRequest sgxClientRequest) {
        TypedKey typedKey = new TypedKey(sgxClientRequest.getTypedValue().getType(), sgxClientRequest.getClientKey());
        sgxDb.put(sgxClientRequest.getClientID(), typedKey);
        System.out.println("received");
    }

    @Override
    public void sum(HttpHeaders headers, SGXClientRequest sgxClientRequest) {

    }

    @Override
    public void compare(HttpHeaders headers, SGXClientRequest sgxClientRequest) {

    }

    @Override
    public void set_conditional(HttpHeaders headers, SGXClientRequest sgxClientRequest) {

    }

    @Override
    public void add_conditional(HttpHeaders headers, SGXClientRequest sgxClientRequest) {

    }
}
