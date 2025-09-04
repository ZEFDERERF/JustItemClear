package com.example.justitemclear.utils;

import com.example.justitemclear.justitemclear;
import com.example.justitemclear.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private static justitemclear plugin;
    private static BossBar bossBar;
    private static final Map<String, String> DEFAULT_MESSAGES = new HashMap<>();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final ConsoleCommandSender CONSOLE = Bukkit.getConsoleSender();
    private static Method sendActionBarMethod = null;

    static {
        // 初始化默认消息
        // 倒计时消息默认值
        DEFAULT_MESSAGES.put("messages.countdown.custom_minutes", "&e还有 &r%d分钟 &e清理%s");
        DEFAULT_MESSAGES.put("messages.countdown.five_minutes", "&e还有 &r5分钟 &e清理%s");
        DEFAULT_MESSAGES.put("messages.countdown.one_minute", "&e还有 &r1分钟 &e清理%s");
        DEFAULT_MESSAGES.put("messages.countdown.thirty_seconds", "&e还有 &r30秒 &e清理%s");
        DEFAULT_MESSAGES.put("messages.countdown.fifteen_seconds", "&e还有 &r15秒 &e清理%s");
        DEFAULT_MESSAGES.put("messages.countdown.seconds", "&c还有 &r%d秒 &c清理%s");
        DEFAULT_MESSAGES.put("messages.countdown.second_warning", "秒后清理所有%s");
        // 玩家消息默认值
        DEFAULT_MESSAGES.put("messages.player.cleanup_completed", "&a清理完成! 距离下次清理还剩%d分钟");
        DEFAULT_MESSAGES.put("messages.player.command_reloaded", "justitemclear 配置已重载！");
        DEFAULT_MESSAGES.put("messages.player.quick_clean_start", "立即清理已启动！");
        DEFAULT_MESSAGES.put("messages.player.command_no_permission", "你没有权限执行此命令！");
        // 日志消息默认值
        DEFAULT_MESSAGES.put("messages.log.total_cleanup", "总共清理了 %d 个%s");
        DEFAULT_MESSAGES.put("messages.log.world_cleanup", "世界 '%s' 清理了 %d 个%s");

        try {
            sendActionBarMethod = Player.class.getMethod("sendActionBar", String.class);
        } catch (NoSuchMethodException e) {

        }
    }

    public static void initialize(justitemclear pluginInstance) {
        plugin = pluginInstance;
    }

    public static void sendMessage(CommandSender sender, String messageKey, Object... args) {
        String message = formatMessage(messageKey, args);
        sender.sendMessage(message);
    }
    
    public static void broadcastMessage(String messageKey, Object... args) {
        broadcastMessageWithActionBar(messageKey, false, args);
    }
    
    public static void broadcastMessageWithActionBar(String messageKey, boolean showActionBar, Object... args) {
        String message = formatMessage(messageKey, args);
        String soundKey = getSoundKey(messageKey);
        
        Sound sound = getSound(soundKey);
        boolean showChat = ConfigManager.getInstance().getBoolean("messages.display.chat", true);
        boolean showActionBarMsg = ConfigManager.getInstance().getBoolean("messages.display.actionbar", true);
        boolean showBossBar = ConfigManager.getInstance().getBoolean("messages.display.bossbar", true);

        handleBossBar(messageKey, message, showBossBar, args);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (showChat) {
                player.sendMessage(message);
            }
            if (showActionBar && showActionBarMsg) {
                sendActionBarMessage(player, message);
            }
            
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }

        CONSOLE.sendMessage(message);
    }
    
    private static String getSoundKey(String messageKey) {
        if (messageKey.equals("messages.countdown.completed")) {
            return "completion";
        } else if (messageKey.startsWith("messages.countdown")) {
            return "countdown";
        } else {
            return "completion";
        }
    }
    
    private static void handleBossBar(String messageKey, String message, boolean showBossBar, Object... args) {
        if (!showBossBar || !messageKey.startsWith("messages.countdown.seconds") || args.length == 0) {
            if (bossBar != null) {
                bossBar.setVisible(false);
            }
            return;
        }
        
        try {
            int seconds = (int) args[0];
            if (seconds <= 6) {
                if (bossBar == null) {
                    bossBar = Bukkit.createBossBar(
                        translateColorCodes(message), 
                        BarColor.RED, 
                        BarStyle.SOLID
                    );
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        bossBar.addPlayer(player);
                    }
                }
                bossBar.setTitle(translateColorCodes(message));
                bossBar.setProgress(seconds / 6.0);
                bossBar.setVisible(true);
            } else if (bossBar != null) {
                bossBar.setVisible(false);
            }
        } catch (ClassCastException e) {
            plugin.getLogger().warning("BossBar处理错误: 参数类型不匹配");
        }
    }

    public static void logToConsole(String messageKey, Object... args) {
        String message = formatMessage(messageKey, args);

        CONSOLE.sendMessage(message);
    }

    public static void sendActionBarMessage(Player player, String message) {
        if (!player.isOnline()) {
            return;
        }

        if (sendActionBarMethod != null) {
            try {
                sendActionBarMethod.invoke(player, translateColorCodes(message));
                return;
            } catch (Exception e) {
                plugin.getLogger().warning("发送ActionBar消息时出错: " + e.getMessage());

            }
        }

        try {
            Object craftPlayer = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer")
                    .cast(player);
            Object packetPlayOutChat = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutChat")
                    .getConstructor(
                            Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent"),
                            byte.class
                    ).newInstance(
                            Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent$ChatSerializer")
                                    .getMethod("a", String.class)
                                    .invoke(null, "{\"text\":\"" + translateColorCodes(message) + "\"}"),
                            (byte) 2
                    );
            craftPlayer.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"))
                    .invoke(craftPlayer, packetPlayOutChat);
        } catch (Exception ex) {
            plugin.getLogger().warning("无法发送ActionBar消息: " + ex.getMessage());
        }
    }
    
    private static String formatMessage(String messageKey, Object... args) {
        String message = ConfigManager.getInstance().getString(messageKey, DEFAULT_MESSAGES.getOrDefault(messageKey, "[消息缺失] " + messageKey));
        
        try {
            if (args.length > 0) {
                message = String.format(message, args);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("消息格式化错误 (" + messageKey + "): " + e.getMessage());
        }

        if (ConfigManager.getInstance().getBoolean("messages.show_prefix", true)) {
            String prefix = ConfigManager.getInstance().getString("messages.prefix", "[JIC] ");
            message = prefix + message;
        }
        
        return translateColorCodes(message);
    }

    public static String translateColorCodes(String message) {
        if (message == null) {
            return "";
        }

        String translated = ChatColor.translateAlternateColorCodes('&', message);

        Matcher matcher = HEX_PATTERN.matcher(translated);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x" +
                    ChatColor.COLOR_CHAR + color.charAt(0) + ChatColor.COLOR_CHAR + color.charAt(1) +
                    ChatColor.COLOR_CHAR + color.charAt(2) + ChatColor.COLOR_CHAR + color.charAt(3) +
                    ChatColor.COLOR_CHAR + color.charAt(4) + ChatColor.COLOR_CHAR + color.charAt(5));
        }
        translated = matcher.appendTail(buffer).toString();
        
        return translated;
    }
    
    private static Sound getSound(String soundKey) {
        String soundName = plugin.getConfig().getString("sounds." + soundKey);
        if (soundName == null || soundName.isEmpty()) {
            return null;
        }
        
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("无效的声音配置: " + soundName);
            return null;
        }
    }

    private static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}