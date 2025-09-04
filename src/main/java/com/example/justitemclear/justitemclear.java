package com.example.justitemclear;

import com.example.justitemclear.manager.CleanupManager;
import com.example.justitemclear.manager.ConfigManager;
import com.example.justitemclear.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import org.bukkit.ChatColor;

import org.bukkit.event.Listener;
import java.util.List;
import java.util.Arrays;

public class justitemclear extends JavaPlugin implements Listener {

    private Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();

        saveDefaultConfig();

        ConfigManager.initialize(this);
        MessageUtils.initialize(this);
        CleanupManager.initialize(this);
        
        getServer().getPluginManager().registerEvents(this, this);
        logger.info("JustItemClear 插件已启用！");
        logger.info("JustItemClear By ZEFDERERF QQ 2028356250");
    }

    @Override
    public void onDisable() {
        CleanupManager.getInstance().onDisable();
        logger.info("JustItemClear 插件已禁用！");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("justitemclear")) {
            if (!sender.hasPermission("justitemclear.admin")) {
                MessageUtils.sendMessage(sender, "no-permission");
                return true;
            }

            if (args.length == 0) {
                showHelp(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                handleReloadCommand(sender);
            } else if (args[0].equalsIgnoreCase("clear")) {
                handleCleanCommand(sender);
            } else {
                showHelp(sender);
            }
            return true;
        }
        return false;
    }

    private void handleReloadCommand(CommandSender sender) {
        if (sender.hasPermission("justitemclear.reload")) {
            ConfigManager.getInstance().reloadConfig();
            CleanupManager.getInstance().onConfigReload();
            MessageUtils.sendMessage(sender, "messages.player.command_reloaded");
        } else {
            MessageUtils.sendMessage(sender, "messages.player.command_no_permission");
        }
    }
    
    
    private void handleCleanCommand(CommandSender sender) {
        if (sender.hasPermission("justitemclear.clear")) {
            CleanupManager.getInstance().startQuickCleanup();
            MessageUtils.sendMessage(sender, "messages.player.quick_clean_start");
        } else {
            MessageUtils.sendMessage(sender, "messages.player.command_no_permission");
        }
    }

    private void showHelp(CommandSender sender) {
        String title = getConfig().getString("messages.help.title", "&6=== JustItemClear 帮助 ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', title));
        List<String> commands = getConfig().getStringList("messages.help.commands");

        if (commands == null || commands.isEmpty()) {

            commands = Arrays.asList(
                "&a/jic help - 显示帮助信息",
                "&a/jic reload - 重载配置文件",
                "&a/jic clear - 立即清理物品"

            );
        }

        for (String cmd : commands) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                String formattedCmd = cmd.contains("/") ? cmd : "&a" + cmd;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedCmd));
            }
        }
    }



}
