package org.wontfix.velocitywhitelist;

import java.io.*;
import java.nio.file.Path;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.slf4j.Logger;
import net.kyori.text.TextComponent;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
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

    /* During normal operations, the users are checked against the HashSet of UUIDs
       and persisted into a `whitelist.json` file in the plugins' datadir.

       We use the `enabled` attribute to check if whitelisting is on/off and persist
       to a `whitelist.enabled` file that is either created or removed.

       Technically we could probably just seek through the whitelist.json every time
       and also check for the triggerFile each and every time, they are almost certainly
       fs-cached anyway, but why waste all those systemcalls for such a simple case.
     */
    private HashSet<UUID> whitelist;
    private final File wlFile;
    private final File triggerFile;
    private boolean enabled = true;

    @Inject
    public VelocityWhitelist(CommandManager commandManager, Logger logger, @DataDirectory final Path datadir) {
        String cmdName = "vwhitelist";
        this.wlFile = new File(datadir.toFile(), "whitelist.json");
        this.triggerFile = new File(datadir.toFile(), "whitelist.enabled");
        this.logger = logger;
        logger.info("Registering command {}", cmdName);
        commandManager.register(new WhitelistCommand(this, logger), cmdName);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.reload();
    }

    @Subscribe
    public void onUserLoginEvent(LoginEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        String username = event.getPlayer().getUsername();
        UUID uuid = event.getPlayer().getUniqueId();
        this.logger.info("VelocityWhitelist.login {}", event.getPlayer().getUsername());
        if (!this.whitelist.contains(uuid)) {
            this.logger.warn("User is missing a whitelist entry, rejecting: {} ({})", username, uuid);
            event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of("User " + username + " not whitelisted on proxy")));
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean enable() throws IOException {
        try {
            return this.triggerFile.createNewFile();
        } finally {
            this.enabled = true;
        }
    }

    public boolean disable() {
        try {
            return this.triggerFile.delete();
        } finally {
            this.enabled = false;
        }
    }

    public void add(String username) throws IOException {
        this.logger.info("Adding whitelist entry for {}", username);
        Gson gson = new Gson();
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
        URLConnection request = url.openConnection();
        request.connect();

        UuidResolverRecord record = gson.fromJson(
            new InputStreamReader((InputStream) request.getContent()),
            UuidResolverRecord.class
        );

        ArrayList<WhitelistRecord> records = this.readWhitelist();
        records.add(new WhitelistRecord(record.getFullUuid(), record.name));
        this.writeWhitelist(records);
        this.reload();
    }

    public void remove(String username) throws IOException {
        this.logger.info("Removing whitelist entry for {}", username);
        ArrayList<WhitelistRecord> records = this.readWhitelist();
        this.writeWhitelist(
                records.stream().filter(item -> !item.name.equals(username)).collect(Collectors.toList())
        );
        this.reload();
    }

    public void reload() {
        this.whitelist = this.indexWhitelist(this.readWhitelist());
        this.enabled = this.triggerFile.exists();
    }

    public List<WhitelistRecord> list() {
        return this.readWhitelist();
    }

    private ArrayList<WhitelistRecord> readWhitelist() {
        Gson gson = new Gson();
        try {
            return new ArrayList<>(
                Arrays.asList(
                    gson.fromJson(
                        new FileReader(this.wlFile),
                        WhitelistRecord[].class
                    )
                )
            );
        } catch (FileNotFoundException e) {
            this.logger.warn("Could not read whitelist from {}", this.wlFile);
            return new ArrayList<WhitelistRecord>() {};
        }
    }

    private HashSet<UUID> indexWhitelist(List<WhitelistRecord> records) {
        HashSet<UUID> whitelist = new HashSet<>();
        try {
            for (WhitelistRecord user: records) {
                whitelist.add(UUID.fromString(user.uuid));
                this.logger.info("Adding user to whitelist: {} ({})", user.name, user.uuid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return whitelist;
    }

    private void writeWhitelist(List<WhitelistRecord> records) throws IOException {
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(this.wlFile);
        this.logger.debug("Writing new whitelist: {}", records);
        writer.write(gson.toJson(records));
        writer.close();
    }
}
