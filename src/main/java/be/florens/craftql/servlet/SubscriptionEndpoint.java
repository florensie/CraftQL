package be.florens.craftql.servlet;

import be.florens.craftql.CraftQL;
import graphql.kickstart.servlet.GraphQLWebsocketServlet;

public class SubscriptionEndpoint extends GraphQLWebsocketServlet {

    public SubscriptionEndpoint() {
        super(CraftQL.graphQLConfig);
    }
}
