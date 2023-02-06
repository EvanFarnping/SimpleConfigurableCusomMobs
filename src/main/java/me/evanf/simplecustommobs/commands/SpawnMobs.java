package me.evanf.simplecustommobs.commands;

import me.evanf.simplecustommobs.SimpleCustomMobs;
import me.evanf.simplecustommobs.mobs.CustomIllusioner;
import me.evanf.simplecustommobs.mobs.CustomSpider;
import me.evanf.simplecustommobs.mobs.CustomWitherSkeleton;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnMobs implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        } else {
            Player player = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("spawncustomspider")) {
                if (player.isOp() || hasPerm(player)) {
                    CustomSpider.createCustomSpider(player.getLocation());
                }
                else {
                    player.sendMessage(ChatColor.RED
                            + "You do not have access to this command.");
                }
                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("spawncustomwitherskeleton")) {
                if (player.isOp() || hasPerm(player)) {
                    CustomWitherSkeleton.createCustomWitherSkeleton(player.getLocation());
                }
                else {
                    player.sendMessage(ChatColor.RED
                            + "You do not have access to this command.");
                }
                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("spawncustomillusioner")) {
                if (player.isOp() || hasPerm(player)) {
                    CustomIllusioner.createCustomIllusioner(player.getLocation());
                }
                else {
                    player.sendMessage(ChatColor.RED
                            + "You do not have access to this command.");
                }
                return true;
            }
            else {
                return false;
            }
        }
    }
    private boolean hasPerm(Player p) {
        if(SimpleCustomMobs.config.getBoolean("custom_spawn_permissions")) {
            return p.hasPermission("custom_spawn_permissions.power");
        }
        else {
            return true;
        }
    }
}