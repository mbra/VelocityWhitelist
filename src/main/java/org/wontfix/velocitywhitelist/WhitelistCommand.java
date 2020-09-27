package org.wontfix.velocitywhitelist;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class WhitelistCommand implements Command {

    private final Logger logger;
    private final VelocityWhitelist whitelist;
    private final String togglePermission = "vwhitelist.toggle";

    public WhitelistCommand(VelocityWhitelist whitelist, Logger logger) {
        this.whitelist = whitelist;
        this.logger = logger;
    }

    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        if (!source.hasPermission(this.togglePermission)) {
            this.error(source, "You do not have permission to manage whitelist whitelist");
            return;
        }

        if (args.length == 0) {
            this.error(source, "Invalid usage!");
            return;
        }

        switch (args[0]) {
            case "on":
                try {
                    if (this.whitelist.enable()) {
                        this.info(source, "Whitelist is now turned on");
                    } else {
                        this.error(source, "Whitelist is already turned on");
                    };
                } catch (IOException e) {
                    this.error(source, "Could not enable whitelist checking", e);
                }
                break;
            case "off":
                if (this.whitelist.disable()) {
                    this.info(source, "Whitelist is now turned off");
                } else {
                    this.error(source, "Whitelist is already turned off");
                };
                break;
        }
    }

    @Override
    public List<String> suggest(@NonNull CommandSource source, String @NonNull [] currentArgs) {
        if (!source.hasPermission(this.togglePermission) || currentArgs.length != 0) {
            return Collections.emptyList();
        }
        return new ArrayList<String>(Arrays.asList("on", "off"));
    }

    private void info(CommandSource source, String msg) {
        source.sendMessage(TextComponent.of(msg));
    }

    private void error(CommandSource source, String msg) {
        source.sendMessage(TextComponent.of(msg).color(TextColor.RED));
    }

    private void error(CommandSource source, String msg, Throwable t) {
        this.error(source, msg);
        this.logger.error(msg, t);
    }
}
