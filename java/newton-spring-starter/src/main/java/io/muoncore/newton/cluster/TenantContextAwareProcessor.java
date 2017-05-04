package io.muoncore.newton.cluster;

import io.muoncore.newton.NewtonEvent;
import io.muoncore.newton.eventsource.TenantEvent;
import io.muoncore.newton.utils.muon.MuonLookupUtils;
import io.muoncore.protocol.event.Event;

public abstract class TenantContextAwareProcessor {

  public void process(Event event, Runnable runnable){
    Class<? extends NewtonEvent> eventType = MuonLookupUtils.getDomainClass(event);
    final NewtonEvent newtonEvent = event.getPayload(eventType);
    if (newtonEvent instanceof TenantEvent){
      doProcess(((TenantEvent)newtonEvent).getTenantId(), runnable);
    }
    else{
      runnable.run();
    }
  }

  protected abstract void doProcess(String tenantId, Runnable runnable);


}
