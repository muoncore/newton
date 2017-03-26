package io.muoncore.newton;

import java.util.UUID;

public class UUIDIdentifier implements NewtonIdentifier {
  private String id = UUID.randomUUID().toString();

  public static UUIDIdentifier from(String value) {
    UUIDIdentifier ident = new UUIDIdentifier();
    ident.id = UUID.fromString(value).toString();
    return ident;
  }

  @Override
  public String toString() {
    return id;
  }
}
