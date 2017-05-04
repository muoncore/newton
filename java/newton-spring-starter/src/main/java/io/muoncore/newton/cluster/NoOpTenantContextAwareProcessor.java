package io.muoncore.newton.cluster;

public class NoOpTenantContextAwareProcessor extends TenantContextAwareProcessor {

  @Override
  protected void doProcess(String tenantId, Runnable runnable) {
      runnable.run();
  }
}
