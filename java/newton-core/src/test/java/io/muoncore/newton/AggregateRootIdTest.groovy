package io.muoncore.newton

import com.google.gson.Gson

class AggregateRootIdTest extends GroovyTestCase {

  void testSerializeDeserialize() {
    AggregateRootId aggregateRootId = new AggregateRootId("123")
    Gson gson = new Gson()

    String sResult = gson.toJson(aggregateRootId)
    assertEquals """{"value":"123"}""", sResult
    println sResult

    def rootId = gson.fromJson(sResult, AggregateRootId)
    assertEquals aggregateRootId, rootId

  }
}
