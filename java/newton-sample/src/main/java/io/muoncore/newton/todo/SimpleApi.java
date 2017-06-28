package io.muoncore.newton.todo;

import io.muoncore.Muon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SimpleApi {

  @Autowired
  Muon muon;

  @PostConstruct
  public void start() {

  }



}
