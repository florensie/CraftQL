package be.florens.craftql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class TextCoercing implements Coercing<Text, String> {

    @Override
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
        if (!(dataFetcherResult instanceof Text)) {
            throw new CoercingSerializeException("Expected a Text object");
        }

        return ((Text) dataFetcherResult).getString();
    }

    @Override
    public Text parseValue(Object input) throws CoercingParseValueException {
        if (!(input instanceof String)) {
            throw new CoercingParseValueException("Expected a String object");
        }

        return new LiteralText((String) input);
    }

    @Override
    public Text parseLiteral(Object input) throws CoercingParseLiteralException {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException("Expected a StringValue object");
        }

        return new LiteralText(((StringValue) input).getValue());
    }
}
