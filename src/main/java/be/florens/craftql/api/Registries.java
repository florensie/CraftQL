package be.florens.craftql.api;

import be.florens.craftql.CraftQL;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.GraphQLScalarType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.registry.Registry;

public class Registries {

    public static final Registry<GraphQLScalarType> SCALARS = FabricRegistryBuilder.createSimple(GraphQLScalarType.class,
            CraftQL.id("scalars")).buildAndRegister();
    @SuppressWarnings("unchecked")
    public static final Registry<GraphQLResolver<?>> RESOLVERS = FabricRegistryBuilder.createDefaulted(
            (Class<GraphQLResolver<?>>) (Class<?>) GraphQLResolver.class, CraftQL.id("resolvers"),
            CraftQL.id("root_query")).buildAndRegister();
    // TODO: directives
}
