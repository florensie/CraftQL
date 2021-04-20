package be.florens.craftql.servlet;

import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;

import java.util.function.Supplier;

public class GraphQLServlet extends GraphQLHttpServlet {

    @Override
    protected GraphQLConfiguration getConfiguration() {
        //noinspection unchecked
        Supplier<GraphQLConfiguration> configSupplier = (Supplier<GraphQLConfiguration>) this.getServletContext().getAttribute("graphql-config-supplier");
        return configSupplier.get();
    }
}
