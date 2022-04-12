package me.snazzy.gungame;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class GunGame extends Thread implements Listener, CommandExecutor {

    static HashMap<String, Boolean> ReadyPlayers = new HashMap<>();
    static HashMap<String, Integer> Progression = new HashMap<>();
    static int NumReadyPlayers;
    static int AdventurePlayers = 0;
    static Location spawn = Bukkit.getServer().getWorld("world").getSpawnLocation();
    static Location startLoc = Bukkit.getServer().getWorld("world").getBlockAt(-58, 7, -50).getLocation();
    static boolean isGameStarted = false;
    static ItemStack[] weapons = {
            new ItemStack(Material.WOODEN_SWORD),
            new ItemStack(Material.STONE_SWORD),
            new ItemStack(Material.WOODEN_AXE),
            new ItemStack(Material.IRON_SWORD),
            new ItemStack(Material.STONE_AXE),
            new ItemStack(Material.DIAMOND_SWORD),
            new ItemStack(Material.DIAMOND_AXE),
            new ItemStack(Material.NETHERITE_AXE),
            new ItemStack(Material.NETHERITE_SWORD),
            new ItemStack(Material.AIR)
    };


    // Keep track of adventure players
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isGameStarted && event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);

        } else if (!isGameStarted && event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        }

        if (event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            AdventurePlayers ++;
            System.out.println("Adventure players: " + AdventurePlayers);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            AdventurePlayers --;
            System.out.println("Adventure players: " + AdventurePlayers);
        }
    }

    @EventHandler
    public void GamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode().equals(GameMode.ADVENTURE)) {
            AdventurePlayers ++;
        } else if (event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            AdventurePlayers --;
        }
        System.out.println("Adventure players: " + AdventurePlayers);
    }

    // Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("ready")) {

            if (isGameStarted) {
                sender.sendMessage(ChatColor.RED + "You cannot ready up while there is a round in progress.");

            } else if (ReadyPlayers.containsKey(sender.getName())) {
                sender.sendMessage(ChatColor.RED + "You are already ready.");

            } else {
                ReadyPlayers.put(sender.getName(), Boolean.TRUE);
                NumReadyPlayers = ReadyPlayers.size();
                System.out.println("Ready players: " + ReadyPlayers);
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GREEN
                            + sender.getName()
                            + " is now ready!\n"
                            + ChatColor.AQUA
                            + NumReadyPlayers
                            + ChatColor.GREEN
                            + " out of "
                            + ChatColor.AQUA
                            + AdventurePlayers
                            + ChatColor.GREEN
                            + " players are ready.");

                    if (AdventurePlayers <= 1) {
                        player.sendMessage(ChatColor.GREEN + "There is only 1 player online so the game isn't starting.");
                    }
                }

                if ((NumReadyPlayers >= AdventurePlayers) && (NumReadyPlayers > 1)) {
                    startGame();
                }
            }
        }

        if (label.equalsIgnoreCase("unready")) {
            if (!ReadyPlayers.containsKey(sender.getName())) {
                sender.sendMessage(ChatColor.RED + "You are already not ready.");
            } else {
                ReadyPlayers.remove(sender.getName());
                System.out.println("Ready players: " + ReadyPlayers);
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GREEN
                            + sender.getName()
                            + " is no longer ready.\n"
                            + ChatColor.AQUA
                            + (NumReadyPlayers - 1)
                            + ChatColor.GREEN
                            + " out of "
                            + ChatColor.AQUA
                            + AdventurePlayers
                            + ChatColor.GREEN
                            + " players are ready.");
                }
            }
        }

        if (label.equalsIgnoreCase("force")) {
            if (args[0].equalsIgnoreCase("start") && sender.hasPermission("force.use")) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GREEN + "The game has been forced to start");
                }
                startGame();
            } else if (args[0].equalsIgnoreCase("end") && sender.hasPermission("force.use")) {
                endGame(null);
            }
        }

        if (label.equalsIgnoreCase("fix") && sender.hasPermission("fix.use")) {
            AdventurePlayers = 0;
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.getGameMode().equals(GameMode.ADVENTURE)) {
                    AdventurePlayers++;
                }
            }
            sender.sendMessage("Adventure players recalculated. Currently: " + AdventurePlayers);
        }
        return true;
    }

    // Cancelers
    @EventHandler
    public void onInteractAtEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemFrameBreak(HangingBreakByEntityEvent event) {
        Player breaker = (Player) event.getRemover();
        if (breaker.getGameMode().equals(GameMode.ADVENTURE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttackEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {return;}
        Player attacker = (Player) event.getDamager();
        if (!(event.getEntity() instanceof ItemFrame)) {return;}
        if (attacker.getGameMode().equals(GameMode.ADVENTURE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
            event.setCancelled(true);
        }
    }

    // Other stuff
    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {return;}
        if (!(event.getDamager() instanceof Player)) {return;}
        if (!isGameStarted) {event.setCancelled(true);}

        Player killer = (Player) event.getDamager();
        Player killedPlayer = (Player) event.getEntity();

        if (((killedPlayer.getHealth() - event.getFinalDamage()) <= 0) && !killedPlayer.getGameMode().equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
            killedPlayer.getLocation().getWorld().playSound(killedPlayer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
            spawnPlayer(killedPlayer);

            int killerInt = Progression.get(killer.getName());
            killerInt++;
            Progression.replace(killer.getName(), killerInt);

            killer.getInventory().clear();
            killer.getInventory().addItem(weapons[killerInt]);

            if (killerInt == 9) {
                Player winner;
                winner = killer;
                endGame(winner);
            }
        }
    }

    public static void spawnPlayer(Player player) {
        int xOffset = ThreadLocalRandom.current().nextInt(103);
        int yOffset = ThreadLocalRandom.current().nextInt(118);
        player.teleport(startLoc.add(xOffset, 0, yOffset));
        startLoc.subtract(xOffset, 0, yOffset);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "effect give " + player.getName() + " instant_health 1 5");
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "effect give " + player.getName() + " saturation 1 20");
    }

    public static void startGame() {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "fix");
        int timeLeft = 5;
        while (timeLeft > 0) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.sendMessage("Starting in " + timeLeft + "...");
            }
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            timeLeft--;
        }
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getGameMode().equals(GameMode.ADVENTURE)) {
                player.getInventory().clear();
                player.setExp(0);
                player.setLevel(0);
                ItemStack startingWeapon = new ItemStack(Material.WOODEN_SWORD);
                spawnPlayer(player);
                player.getInventory().addItem(startingWeapon);
                player.sendMessage(ChatColor.RED + "The game has started, Good luck!");
                Progression.put(player.getName(), 0);
            }
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "effect give @a instant_health 1 5");
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "effect give @a saturation 1 20");
        ReadyPlayers.clear();
        NumReadyPlayers = 0;
        isGameStarted = true;

    }

    public static void endGame(Player winner) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (winner == null) {
                player.sendMessage(ChatColor.RED + "The game has been forced to end");
            } else {
                player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + winner.getName() + " has won the game!");
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 10, 1);
            }
        }

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        // create kills string
        String kills = "";
        for (Object name : Progression.keySet().toArray()) {
            kills = kills + "\n" + name + " - " + Progression.get(name);
        }

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Kills:");
            player.sendMessage(ChatColor.AQUA + kills);
        }

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.teleport(spawn);
            player.getInventory().clear();
            player.setExp(0);
            player.setLevel(0);
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "effect give @a instant_health 1 5");
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "effect give @a saturation 1 20");
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=item]");
        isGameStarted = false;
        Progression.clear();
    }
}

