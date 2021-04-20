package be.florens.craftql.resolver;

import be.florens.craftql.CraftQL;
import be.florens.craftql.api.Registries;
import graphql.kickstart.tools.GraphQLResolver;
import net.minecraft.util.registry.Registry;

public class Resolvers {

    public static final GraphQLResolver<?> ROOT_QUERY = new RootQueryResolver();
    public static final GraphQLResolver<?> MUTATION = new MutationResolver();
    public static final GraphQLResolver<?> SUBSCRIPTION = new SubscriptionResolver();
    public static final GraphQLResolver<?> INVENTORY = new InventoryResolver();

    public static void registerAll() {
        registerResolver("root_query", ROOT_QUERY);
        registerResolver("mutation", MUTATION);
        registerResolver("subscription", SUBSCRIPTION);
        registerResolver("inventory", INVENTORY);
    }

    private static void registerResolver(String path, GraphQLResolver<?> resolver) {
        Registry.register(Registries.RESOLVERS, CraftQL.id(path), resolver);
    }
}
