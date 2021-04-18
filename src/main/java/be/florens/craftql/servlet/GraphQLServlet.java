package be.florens.craftql.servlet;

import be.florens.craftql.CraftQL;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;

public class GraphQLServlet extends GraphQLHttpServlet {

    @Override
    protected GraphQLConfiguration getConfiguration() {
        return CraftQL.graphQLConfig;
    }
}
