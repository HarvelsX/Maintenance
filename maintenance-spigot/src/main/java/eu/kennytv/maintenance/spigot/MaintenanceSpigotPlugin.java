/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.spigot.MaintenanceSpigotAPI;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.spigot.command.MaintenanceSpigotCommand;
import eu.kennytv.maintenance.spigot.listener.PlayerLoginListener;
import eu.kennytv.maintenance.spigot.metrics.MetricsLite;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author KennyTV
 * @since 2.0
 */
public final class MaintenanceSpigotPlugin extends MaintenanceModePlugin {
    private final MaintenanceSpigotBase plugin;
    private final SettingsSpigot settings;

    MaintenanceSpigotPlugin(final MaintenanceSpigotBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.SPIGOT);
        this.plugin = plugin;
        plugin.getLogger().info("Plugin by KennyTV");
        plugin.getLogger().info(getUpdateMessage());

        settings = new SettingsSpigot(this, plugin);

        plugin.getCommand("maintenancespigot").setExecutor(new MaintenanceSpigotCommand(this, settings));

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerLoginListener(this, settings), plugin);

        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (pm.isPluginEnabled(serverListPlus)) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceSpigotAPI.getAPI();
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        settings.setMaintenance(maintenance);
        settings.getConfig().set("enable-maintenance-mode", maintenance);
        settings.saveConfig();

        if (isTaskRunning())
            cancelTask();

        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);

        if (maintenance) {
            getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                    .forEach(p -> p.kickPlayer(settings.getKickMessage().replace("%NEWLINE%", "\n")));
            broadcast(settings.getMessage("maintenanceActivated"));
        } else
            broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    public int startMaintenanceRunnable(final Runnable runnable) {
        return getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 20);
    }

    @Override
    public void async(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void cancelTask() {
        getServer().getScheduler().cancelTask(taskId);
        runnable = null;
        taskId = 0;
    }

    @Override
    public void broadcast(final String message) {
        getServer().broadcastMessage(message);
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        sender.sendMessage(getPrefix() + "§cThere is a newer version available: §aVersion " + getNewestVersion() + "§c, you're on §a" + getVersion());
        final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(getPrefix()));
        final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/"));
        final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenancemode.40699/"));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDownload the latest version").create()));
        tc1.addExtra(tc2);
        tc1.addExtra(click);
        ((BukkitSenderInfo) sender).sendMessage(tc1, getPrefix() + "§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/");
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public ISettings getSettings() {
        return settings;
    }

    @Override
    public File getPluginFile() {
        return plugin.getPluginFile();
    }

    @Override
    public InputStream getResource(final String name) {
        return plugin.getResource(name);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public Server getServer() {
        return plugin.getServer();
    }
}
