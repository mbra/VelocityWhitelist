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
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand implements Command {

    private final Logger logger;
    private final VelocityWhitelist whitelist;

    public WhitelistCommand(VelocityWhitelist whitelist, Logger logger) {
        this.whitelist = whitelist;
        this.logger = logger;
    }

    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        if (args.length == 0) {
            this.error(source, "Invalid usage!");
            return;
        }

        switch (args[0]) {
            case "reload":
                this.whitelist.reload();
                break;
            case "add":
                try {
                    this.whitelist.add(args[1]);
                } catch (IOException e) {
                    this.error(source, "Error adding the whitelist entry", e);
                }
                break;
            case "remove":
                try {
                    this.whitelist.remove(args[1]);
                } catch (IOException e) {
                    this.error(source, "Error removing whitelist entry", e);
                }
                break;
            case "list":
                List<WhitelistRecord> whitelist = this.whitelist.list();
                String msg = whitelist.stream().map(item -> item.name).collect(Collectors.joining(", "));
                this.info(source, "There are " + whitelist.size() + " whitelisted players: " + msg);
                break;
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
        String[] foo = {"blah"};
        this.logger.info("My Foo {}", new Object[] { foo  });
        this.logger.info("Suggesting on {} with length {}", new Object[] { currentArgs, currentArgs.length });
        if (currentArgs.length == 0) {
            this.logger.info("Returning commands");
            return new ArrayList<String>(Arrays.asList("list", "add", "remove", "reload", "on", "off"));
        }

        this.logger.info("Returning empty");
        return Arrays.asList();
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
