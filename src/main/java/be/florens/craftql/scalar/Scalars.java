package be.florens.craftql.scalar;

import graphql.schema.GraphQLScalarType;

public class Scalars {

    public static final GraphQLScalarType TEXT = GraphQLScalarType.newScalar().name("Text")
            .coercing(new TextCoercing()).build();
}
