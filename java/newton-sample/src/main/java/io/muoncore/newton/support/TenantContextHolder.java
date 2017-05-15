package io.muoncore.newton.support;

public class TenantContextHolder {

  private static final ThreadLocal<String> contextHolder = new ThreadLocal();

  public static String getTenantId(){
    return contextHolder.get();
  }

  public static void setTenantId(String tenantId){
    contextHolder.set(tenantId);
  }

}
