package me.evanf.simplecustommobs.mobs;

import me.evanf.simplecustommobs.SimpleCustomMobs;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSpider implements Listener {

    private static SimpleCustomMobs plugin;

    Map<String, Long> cooldown = new HashMap<>();

    public CustomSpider(SimpleCustomMobs main) {
        plugin = main;
    }

    public static void createCustomSpider(Location loc) {
        Spider spider = loc.getWorld().spawn(loc, Spider.class);

        String name = SimpleCustomMobs.config.getString("spider_name");
        spider.setCustomName(ChatColor.GRAY + name);
        spider.setCustomNameVisible(true);
        /*
        // Mojang's attributes are inconsistent and difficult to use.
        // It is best to avoid them if you can.
        // Especially when they are tied to a config.
        // In general, using potion effects are easier and flexible.
         */
        // Heath points added are empty when the entity spawns
        int health_val = SimpleCustomMobs.config.getInt("spider_stats.health");
        AttributeInstance hp_attribute = spider.getAttribute(
                Attribute.GENERIC_MAX_HEALTH);
        hp_attribute.setBaseValue(health_val);
        // Required to guarantee that the entity will have full health on spawn
        spider.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 99));

        if (SimpleCustomMobs.config.getInt("spider_stats.regen") > 0) {
            int regen_val = SimpleCustomMobs.config.getInt("spider_stats.regen");
            spider.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    1000000000, regen_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("spider_stats.resistance") > 0) {
            int resistance_val = SimpleCustomMobs.config.getInt("spider_stats.resistance");
            spider.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    1000000000, resistance_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("spider_stats.speed") > 0) {
            int speed_val = SimpleCustomMobs.config.getInt("spider_stats.speed");
            spider.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    1000000000, speed_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("spider_stats.strength") > 0) {
            int strength_val = SimpleCustomMobs.config.getInt("spider_stats.strength");
            spider.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    1000000000, strength_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("spider_stats.jump") > 0) {
            int jump_val = SimpleCustomMobs.config.getInt("spider_stats.jump");
            spider.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP,
                    1000000000, jump_val - 1));
        }

        spider.setMetadata("CustomSpider",
                new FixedMetadataValue(plugin, "customspider"));

        int max_targeting_distance = SimpleCustomMobs.config.getInt(
                "spider_stats.max_targeting_distance");
        new BukkitRunnable() {
            public void run() {
                if (!spider.isDead()) {
                    if (spider.getTarget() == null) {
                        for (Entity ent : spider.getNearbyEntities(
                                max_targeting_distance, // X
                                max_targeting_distance, // Y
                                max_targeting_distance)) // Z
                        {
                            if (ent instanceof Player) {
                                Player p = (Player)ent;
                                if (p.getGameMode() == GameMode.SURVIVAL) {
                                    spider.setTarget(p);
                                }
                            }
                        }
                    }
                    else {
                        LivingEntity target = spider.getTarget();

                        if (target.getLocation().distanceSquared(
                                spider.getLocation()) > max_targeting_distance + 5) {

                            spider.getWorld().playSound(
                                    spider.getLocation(),
                                    Sound.ENTITY_SPIDER_STEP,
                                    3, 3);
                            spider.setVelocity(
                                    target.getLocation().add(
                                            0,
                                            2,
                                            0).subtract(
                                            spider.getLocation()
                                    ).toVector().multiply(0.20));
                        }
                    }
                }
                else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Spider
                && e.getEntity() instanceof Player
                && e.getDamager().hasMetadata("CustomSpider")) {

            int vamp_chance = SimpleCustomMobs.config.getInt(
                    "spider_abilities.vampire.chance");
            int vamp_random = ThreadLocalRandom.current().nextInt(vamp_chance);
            if (vamp_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "spider_abilities.vampire.enable")) {

                    Spider spider = (Spider)e.getDamager();

                    int vamp_strength = SimpleCustomMobs.config.getInt(
                            "spider_abilities.vampire.strength");
                    int vamp_duration = SimpleCustomMobs.config.getInt(
                            "spider_abilities.vampire.duration");
                    spider.addPotionEffect(
                            new PotionEffect(PotionEffectType.HEAL,
                                    vamp_duration, vamp_strength));
                }
            }

            int venom_chance = SimpleCustomMobs.config.getInt(
                    "spider_abilities.venom.chance");
            int venom_random = ThreadLocalRandom.current().nextInt(venom_chance);
            if (venom_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "spider_abilities.venom.enable")) {

                    Player p = (Player)e.getEntity();

                    int venom_strength = SimpleCustomMobs.config.getInt(
                            "spider_abilities.venom.strength");
                    int venom_duration = SimpleCustomMobs.config.getInt(
                            "spider_abilities.venom.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.POISON,
                                    venom_duration, venom_strength));
                    p.sendMessage(ChatColor.RED + "You have been poisoned!");
                }
            }
        }
        if (e.getDamager() instanceof Player
                && e.getEntity() instanceof Spider
                && e.getEntity().hasMetadata("CustomSpider")) {

            int hard_shell_chance = SimpleCustomMobs.config.getInt(
                    "spider_abilities.hard_shell.chance");
            int hard_shell_random = ThreadLocalRandom.current().nextInt(hard_shell_chance);
            if (hard_shell_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "spider_abilities.hard_shell.enable")) {

                    Player p = (Player)e.getDamager();
                    p.sendMessage(ChatColor.RED +
                            "Your attack bounced off the spider's hard exoskeleton!");
                    p.playSound(p.getLocation(),
                            Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Spider
                && SimpleCustomMobs.config.getBoolean(
                        "natural_spider_spawning.enable_spawn")) {

            if (e.getLocation().getBlock().isLiquid()) {
                return;
            }

            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                if (SimpleCustomMobs.config.getBoolean(
                        "natural_spider_spawning.disable_spawner")) {
                    return;
                }
            }

            int chance = SimpleCustomMobs.config.getInt(
                    "natural_spider_spawning.spawn_number");
            int random = ThreadLocalRandom.current().nextInt(chance);
            if (random < 1) {
                e.setCancelled(true);
                Spider normal_spider = (Spider)e.getEntity();
                CustomSpider.createCustomSpider(normal_spider.getLocation());
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().hasMetadata("CustomSpider")
                && e.getEntity() instanceof Spider) {
            LivingEntity spider = e.getEntity();

            if (SimpleCustomMobs.config.getBoolean(
                    "spider_custom_drops.clear_vanilla")) {
                e.getDrops().clear();
            }

            if (SimpleCustomMobs.config.getBoolean(
                    "spider_custom_drops.enable_drop")) {

                int chance = SimpleCustomMobs.config.getInt(
                        "spider_custom_drops.staff_of_webs.drop_chance");
                int random = ThreadLocalRandom.current().nextInt(chance);
                if (random < 1) {
                    ItemStack item = new ItemStack(Material.STICK);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.WHITE + "Wand of Webs");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "R-click to spew out cobwebs!");
                    meta.setLore(lore);
                    meta.setUnbreakable(true);
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                    spider.getLocation().getWorld().dropItem(spider.getLocation(),
                            new ItemStack(item));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (!e.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
            return;
        }
        if (e.getItem().getType() != Material.STICK) {
            return;
        }

        String item_name = e.getItem().getItemMeta().getDisplayName();

        if (!item_name.equals(ChatColor.WHITE + "Wand of Webs")) {
            return;
        }

        Player p = e.getPlayer();
        if (cooldown.containsKey(p.getName())) {
            if (cooldown.get(p.getName()) > System.currentTimeMillis()) {

                long time_left = (cooldown.get(p.getName())
                        - System.currentTimeMillis()) / 1000;
                p.sendMessage(ChatColor.WHITE + "Wand of Webs "
                        + ChatColor.GRAY + "is recharging for "
                        + time_left + " second(s) " + "more");
                return;
            }
        }
        int cool_down = SimpleCustomMobs.config.getInt(
                "spider_custom_drops.staff_of_webs.cool_down");
        cooldown.put(p.getName(),
                System.currentTimeMillis() + (cool_down * 1000L));

        Location loc = p.getLocation();
        loc.setY(loc.getY() + 1.5f);
        float var = 0.75F;
        int vel = SimpleCustomMobs.config.getInt(
                "spider_custom_drops.staff_of_webs.velocity");

        int amount = SimpleCustomMobs.config.getInt(
                "spider_custom_drops.staff_of_webs.amount");
        for(int i = 0; i < amount; i++){
            // depreciated, but no other method is better for this application
            @SuppressWarnings("deprecation")
            Entity cob_web = loc.getWorld().spawnFallingBlock(loc,
                    Material.COBWEB, (byte) 0);
            Vector v = p.getLocation().getDirection().multiply(vel);
            // Gives a 'spread' effect
            v.add(new Vector(
                    Math.random() * var - var / 2,
                    Math.random() * var - var / 2,
                    Math.random() * var - var / 2));
            cob_web.setVelocity(v);
        }
    }
}