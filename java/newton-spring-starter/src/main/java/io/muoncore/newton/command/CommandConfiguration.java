package io.muoncore.newton.command;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfiguration {

    @Bean
    public CommandBus commandBus(CommandFactory commandFactory) {
        return new SimpleCommandBus(commandFactory);
    }

    @Bean
    public CommandFactory commandFactory() {
        return new CommandFactory();
    }


}
