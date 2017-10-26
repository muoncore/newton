package io.muoncore.newton.utils.muon

import io.muoncore.newton.NewtonEventWithMeta
import io.muoncore.protocol.event.Event
import spock.lang.Specification

class MuonLookupUtilsSpec extends Specification {

  def "decorates correctly"() {

    def ev = new MetaEvent()
    def meta = new Event("", "", "", "", "", "", "", 123l, 321l, Collections.EMPTY_MAP, null)

    when:
    def decorated = MuonLookupUtils.decorateMeta(ev, meta)

    then:
    decorated.meta != null

  }

  class MetaEvent extends NewtonEventWithMeta {
    String id = "hello-world";
  }

}
