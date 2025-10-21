package com.deliverytech.delivery_api.config.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StrictBooleanDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JacksonException {
        if (parser.currentToken() == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }

        if (parser.currentToken() == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }

        // If currentToken is anything else (number, string, null, etc.) an exception will be thrown
        throw ctx.weirdStringException(parser.getText(), Boolean.class,
                "Invalid value. Expected literal 'true' or 'false', but received: " + parser.getText());
    }
}
