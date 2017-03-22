package io.muoncore.newton;

import lombok.ToString;

import java.util.UUID;

@ToString
public class UUIDIdentifier implements NewtonIdentifier {
  private String id = UUID.randomUUID().toString();
}
