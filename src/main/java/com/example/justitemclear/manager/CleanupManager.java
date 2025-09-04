package com.example.justitemclear.manager;

import com.example.justitemclear.justitemclear;
import com.example.justitemclear.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class CleanupManager {

    private static CleanupManager instance;
    private final justitemclear plugin;
    private final ConfigManager configManager;
    
    private BukkitTask mainCleanupTask;
    private List<BukkitTask> activeTasks = new ArrayList<>();
    private AtomicBoolean isCountdownActive = new AtomicBoolean(false);
    private int itemTimer = 0;
    
    private CleanupManager(justitemclear plugin) {
        this.plugin = plugin;
        this.configManager = ConfigManager.getInstance();
    }
    
    public static void initialize(justitemclear plugin) {
        if (instance == null) {
            instance = new CleanupManager(plugin);
            instance.startMainCleanupTask();
        }
    }
    
    public static CleanupManager getInstance() {
        return instance;
    }
    
    private void startMainCleanupTask() {
        cancelMainTask();
        
        mainCleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!configManager.isCleanupItemsEnabled()) return;
                
                itemTimer++;
                int triggerTime = Math.max(0, configManager.getItemCleanupInterval() - configManager.getCountdownDuration());
                
                if (itemTimer >= triggerTime && isCountdownActive.compareAndSet(false, true)) {
                    isCountdownActive.set(true);
                startCountdown(configManager.getCountdownDuration());
                itemTimer = 0;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void startCountdown(int duration) {
        cancelAllActiveTasks();
        isCountdownActive.set(true);
        
        final AtomicInteger remainingSeconds = new AtomicInteger(duration);
        
        BukkitTask countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                int currentSeconds = remainingSeconds.getAndDecrement();
                
                if (currentSeconds <= 0) {
                    performCleanup();
                    resetState();
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
        
        activeTasks.add(countdownTask);

        scheduleReminders(duration);
    }

    private void scheduleReminders(int totalDuration) {
        int minutes = totalDuration / 60;
        String itemType = configManager.getString("messages.item_type", "掉落物");

        if (minutes > 0) {
            scheduleReminder(totalDuration - minutes * 60, "messages.countdown.custom_minutes", minutes, itemType);
        }

        if (minutes == 0 && totalDuration >= 60) {
            scheduleReminder(totalDuration - 60, "messages.countdown.one_minute", itemType);
        }

        if (totalDuration >= 30) {
            scheduleReminder(totalDuration - 30, "messages.countdown.thirty_seconds", itemType);
        }

        if (totalDuration >= 15) {
            scheduleReminder(totalDuration - 15, "messages.countdown.fifteen_seconds", itemType);
        }
        for (int i = 6; i >= 1; i--) {
            int delay = totalDuration - 6 + (6 - i);
            scheduleReminder(delay, "messages.countdown.seconds", i, itemType);
        }
    }
    
    private void scheduleReminder(int delay, String messageKey, Object... args) {
        if (delay < 0) return;
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                MessageUtils.broadcastMessage(messageKey, args);
            }
        }.runTaskLater(plugin, delay * 20L);
        
        activeTasks.add(task);
    }

    private int performCleanup() {
        String itemType = configManager.getString("messages.item_type", "掉落物");
        int totalItemsCleared = clearItems(itemType);
        MessageUtils.broadcastMessageWithActionBar(
                "messages.countdown.completed", 
                true, 
                totalItemsCleared, 
                itemType
        );
        
        MessageUtils.logToConsole("messages.log.total_cleanup", totalItemsCleared, itemType);
        
        return totalItemsCleared;
    }

    private int clearItems(String itemType) {
        int totalCount = 0;
        List<String> worldWhitelist = configManager.getWorldWhitelist();
        
        for (World world : Bukkit.getWorlds()) {

            if (!worldWhitelist.isEmpty() && worldWhitelist.contains(world.getName())) {
                continue;
            }
            
            int worldCount = 0;
            Iterator<Item> itemIterator = world.getEntitiesByClass(Item.class).iterator();
            
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                worldCount += item.getItemStack().getAmount();
                totalCount += item.getItemStack().getAmount();

                item.remove();
                itemIterator.remove();
            }

            if (worldCount > 0) {
                MessageUtils.logToConsole("messages.log.world_cleanup", world.getName(), worldCount, itemType);
            }
        }

        String baseMessage = configManager.getString("messages.player.cleanup_completed", "");
        if (!baseMessage.isEmpty()) {
            MessageUtils.broadcastMessage("messages.player.cleanup_completed");
        }

        return totalCount;
    }

    public void startQuickCleanup() {
        final int quickCountdown = 6;
        String itemType = configManager.getString("messages.item_type", "掉落物");

        cancelAllActiveTasks();
        isCountdownActive.set(true);

        for (int i = quickCountdown; i >= 1; i--) {
            int delay = quickCountdown - i;
            scheduleReminder(delay, "messages.countdown.seconds", i, itemType);
        }

        BukkitTask cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                performCleanup();
                resetState();
            }
        }.runTaskLater(plugin, quickCountdown * 20L);
        
        activeTasks.add(cleanupTask);
    }

    private void resetState() {
        isCountdownActive.set(false);
        itemTimer = 0;
        cancelAllActiveTasks();
        activeTasks.clear();
    }

    private void cancelMainTask() {
        if (mainCleanupTask != null && !mainCleanupTask.isCancelled()) {
            mainCleanupTask.cancel();
        }
        mainCleanupTask = null;
    }
    
    private void cancelAllActiveTasks() {
        for (BukkitTask task : activeTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();
    }

    public void onDisable() {
        cancelMainTask();
        cancelAllActiveTasks();
    }

    public void onConfigReload() {
        cancelMainTask();
        startMainCleanupTask();
    }
}