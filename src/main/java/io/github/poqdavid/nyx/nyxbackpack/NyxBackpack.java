/*
 *     This file is part of NyxBackpack.
 *
 *     NyxBackpack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     NyxBackpack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with NyxBackpack.  If not, see <https://www.gnu.org/licenses/>.
 *
 *     Copyright (c) POQDavid <https://github.com/poqdavid/NyxBackpack>
 *     Copyright (c) contributors
 */

package io.github.poqdavid.nyx.nyxbackpack;

import com.google.inject.Inject;
import io.github.poqdavid.nyx.nyxbackpack.Commands.CommandManager;
import io.github.poqdavid.nyx.nyxcore.NyxCore;
import io.github.poqdavid.nyx.nyxcore.Utils.CText;
import io.github.poqdavid.nyx.nyxcore.Utils.NCLogger;
import io.github.poqdavid.nyx.nyxcore.Utils.Tools;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;


@Plugin(id = PluginData.id, name = PluginData.name, version = PluginData.version, description = PluginData.description, url = PluginData.url, authors = {PluginData.author1}, dependencies = {@Dependency(id = "nyxcore", version = "1.1", optional = false)})
public class NyxBackpack {

    private static NyxBackpack nyxbackpack;
    private final NCLogger logger;
    private final PluginContainer pluginContainer;
    private final Metrics metrics;
    public PermissionService permService;

    @Inject
    private Game game;
    private CommandManager cmdManager;

    @Inject
    public NyxBackpack(Metrics.Factory metricsFactory, @ConfigDir(sharedRoot = true) Path path, Logger logger, PluginContainer container) {
        nyxbackpack = this;
        this.pluginContainer = container;
        this.logger = NyxCore.getInstance().getLogger(CText.get(CText.Colors.BLUE, 1, "Nyx") + CText.get(CText.Colors.MAGENTA, 0, "Backpack"));

        this.logger.info(" ");
        this.logger.info(CText.get(CText.Colors.MAGENTA, 0, "NyxBackpack") + CText.get(CText.Colors.YELLOW, 0, " v" + io.github.poqdavid.nyx.nyxbackpack.PluginData.version));
        this.logger.info("Starting...");
        this.logger.info(" ");

        metrics = metricsFactory.make(12559);
    }


    @Nonnull
    public static NyxBackpack getInstance() {
        return nyxbackpack;
    }

    @Nonnull
    public Path getConfigPath() {
        return NyxCore.getInstance().getBackpackPath();
    }

    @Nonnull
    public Path getBackpackPath() {
        return NyxCore.getInstance().getBackpacksPath();
    }

    @Nonnull
    public PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    @Nonnull
    public String getVersion() {
        return PluginData.version;
    }

    @Nonnull
    public NCLogger getLogger() {
        return logger;
    }

    @Nonnull
    public Game getGame() {
        return game;
    }

    @Inject
    public void setGame(Game game) {
        this.game = game;
    }

    @Listener
    public void onGamePreInit(@Nullable final GamePreInitializationEvent event) {
        this.logger.info(" ");
        this.logger.info(CText.get(CText.Colors.MAGENTA, 0, "NyxBackpack") + CText.get(CText.Colors.YELLOW, 0, " v" + io.github.poqdavid.nyx.nyxbackpack.PluginData.version));
        this.logger.info("Initializing...");
        this.logger.info(" ");

        Tools.backpackUnlockAll();
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(PermissionService.class)) {
            this.permService = (PermissionService) event.getNewProviderRegistration().getProvider();
        }
    }

    @Listener
    public void onGameInit(@Nullable final GameInitializationEvent event) {
        if (Sponge.getServiceManager().getRegistration(PermissionService.class).get().getPlugin().getId().equalsIgnoreCase("sponge")) {
            this.logger.error("Unable to initialize plugin. NyxBackpack requires a PermissionService like  LuckPerms, PEX, PermissionsManager.");
            return;
        }

        this.logger.info("Plugin Initialized successfully!");
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        this.logger.info("Loading...");
        this.cmdManager = new CommandManager(game, this);
        this.logger.info("Loaded!");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        //this.logger.info("Game Server  Started...");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        final Player player = Tools.getPlayer(event.getCause()).get();
        Tools.MakeNewBP(player);
    }

    @Listener
    public void onGameReload(@Nullable final GameReloadEvent event) {
        this.logger.info("Reloading...");

        Tools.backpackUnlockAll();
        this.logger.info("Reloaded!");
    }

}
