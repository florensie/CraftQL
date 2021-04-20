package be.florens.craftql.servlet;

import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLWebsocketServlet;

public class SubscriptionEndpoint extends GraphQLWebsocketServlet {

    public SubscriptionEndpoint(GraphQLConfiguration config) {
        super(config);
    }
}
