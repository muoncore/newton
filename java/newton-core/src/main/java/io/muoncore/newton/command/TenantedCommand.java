package io.muoncore.newton.command;


import lombok.Getter;
import lombok.Setter;

public abstract class TenantedCommand implements Command {

    @Getter
    @Setter
    private String tenantId;

}
