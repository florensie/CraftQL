package be.florens.craftql.servlet;

import graphql.kickstart.servlet.GraphQLConfiguration;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class SubscriptionEndpointConfigurer extends ServerEndpointConfig.Configurator {

    private final SubscriptionEndpoint endpoint;

    public SubscriptionEndpointConfigurer(GraphQLConfiguration config) {
        endpoint = new SubscriptionEndpoint(config);
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        endpoint.modifyHandshake(sec, request, response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) {
        return (T) this.endpoint;
    }
}
