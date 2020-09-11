package org.wontfix.velocitywhitelist;

import java.io.*;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;
import net.kyori.text.TextComponent;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.ResultedEvent;


@Plugin(
        id = "velocitywhitelist",
        name = "VelocityWhitelist",
        version = "1.0-SNAPSHOT",
        description = "Whitelisting plugin for Velocity",
        url = "https://github.com/wontfix-org/VelocityWhitelist",
        authors = {"w0nd3rbr4"}
)
public class VelocityWhitelist {

    @Inject
    private final Logger logger;
    private final File triggerFile;

    @Inject
    public VelocityWhitelist(CommandManager commandManager, Logger logger, @DataDirectory final Path datadir) {
        this.triggerFile = new File(datadir.toFile(), "whitelist.enabled");
        this.logger = logger;
        logger.info("Registering command {}", "vwhitelist");
        commandManager.register(new WhitelistCommand(this, logger), "vwhitelistthub releases");
    }

    @Subscribe
    public void onUserLoginEvent(LoginEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        String username = player.getUsername();
        if (!player.hasPermission("vwhitelist.whitelisted")) {
            this.logger.warn("User not whitelisted, rejecting: {}", username);
            event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of("User " + username + " not whitelisted on proxy")));
        }
    }

    public boolean isEnabled() {
        return this.triggerFile.exists();
    }

    public boolean enable() throws IOException {
        return this.triggerFile.createNewFile();
    }

    public boolean disable() {
        return this.triggerFile.delete();
    }
}
