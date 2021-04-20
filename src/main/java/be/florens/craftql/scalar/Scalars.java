package be.florens.craftql.scalar;

import be.florens.craftql.CraftQL;
import be.florens.craftql.api.Registries;
import graphql.schema.GraphQLScalarType;
import net.minecraft.util.registry.Registry;

public class Scalars {

    public static final GraphQLScalarType TEXT = GraphQLScalarType.newScalar().name("Text").coercing(new TextCoercing()).build();

    public static void registerAll() {
        registerScalar("text", TEXT);
    }

    private static void registerScalar(String path, GraphQLScalarType scalar) {
        Registry.register(Registries.SCALARS, CraftQL.id(path), scalar);
    }
}
