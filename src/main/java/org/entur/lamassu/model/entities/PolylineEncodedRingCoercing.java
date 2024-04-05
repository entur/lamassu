package org.entur.lamassu.model.entities;

import graphql.schema.Coercing;

//import graphql.schema.GraphQLScalarType;

public class PolylineEncodedRingCoercing implements Coercing<String, String> {

  @Override
  public String serialize(Object dataFetcherResult) {
    if (dataFetcherResult instanceof String) {
      return (String) dataFetcherResult;
    }
    throw new IllegalArgumentException(
      "Serialization of PolylineEncodedRing is expected to be a String"
    );
  }

  @Override
  public String parseValue(Object input) {
    if (input instanceof String) {
      return (String) input;
    }
    throw new IllegalArgumentException(
      "Parsing of PolylineEncodedRing is expected from a String"
    );
  }

  @Override
  public String parseLiteral(Object input) {
    if (input instanceof String) {
      return (String) input;
    }
    throw new IllegalArgumentException(
      "Parsing of PolylineEncodedRing literal is expected from a String"
    );
  }
}
