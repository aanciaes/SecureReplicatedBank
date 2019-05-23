package rest.serverController.server;

import rest.server.model.*;
import rest.serverController.client.AdminServerRequest;

import javax.ws.rs.core.HttpHeaders;

/**
 * Restful resources of admin server
 */
public class ManagerHTTPServerResources implements ManagerServer {

    @Override
    public ClientResponse upServer(HttpHeaders headers, AdminServerRequest adminServerRequest) {
        return new ClientResponse("ola", null);
    }

    @Override
    public ClientResponse downServer(HttpHeaders headers, AdminServerRequest adminServerRequest) {
        return new ClientResponse("ola", null);
    }
}
