package me.evanf.simplecustommobs.mobs;

import me.evanf.simplecustommobs.SimpleCustomMobs;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomWitherSkeleton implements Listener {

    private static SimpleCustomMobs plugin;

    public CustomWitherSkeleton(SimpleCustomMobs main) {
        plugin = main;
    }

    public static void createCustomWitherSkeleton(Location loc) {
        WitherSkeleton witherskelly = loc.getWorld().spawn(loc, WitherSkeleton.class);

        String name = SimpleCustomMobs.config.getString("witherskeleton_name");
        witherskelly.setCustomName(ChatColor.RED + name);
        witherskelly.setCustomNameVisible(true);

        Material material;
        if (SimpleCustomMobs.config.getString(
                "witherskeleton_equipment.main_hand") != null) {

            material = Material.valueOf(SimpleCustomMobs.config.getString(
                    "witherskeleton_equipment.main_hand"));

            ItemStack main_item = new ItemStack(material);
            witherskelly.getEquipment().setItemInMainHand(main_item);
        }

        if (SimpleCustomMobs.config.getString(
                "witherskeleton_equipment.off_hand") != null) {

            material = Material.valueOf(SimpleCustomMobs.config.getString(
                    "witherskeleton_equipment.off_hand"));

            ItemStack off_item = new ItemStack(material);
            witherskelly.getEquipment().setItemInOffHand(off_item);
        }

        if (SimpleCustomMobs.config.getString(
                "witherskeleton_equipment.helmet") != null) {

            material = Material.valueOf(SimpleCustomMobs.config.getString(
                    "witherskeleton_equipment.helmet"));

            ItemStack helmet = new ItemStack(material);
            witherskelly.getEquipment().setHelmet(helmet);
        }

        if (SimpleCustomMobs.config.getString(
                "witherskeleton_equipment.chestplate") != null) {

            material = Material.valueOf(SimpleCustomMobs.config.getString(
                    "witherskeleton_equipment.chestplate"));

            ItemStack chestplate = new ItemStack(material);
            witherskelly.getEquipment().setChestplate(chestplate);
        }

        if (SimpleCustomMobs.config.getString(
                "witherskeleton_equipment.leggings") != null) {

            material = Material.valueOf(SimpleCustomMobs.config.getString(
                    "witherskeleton_equipment.leggings"));

            ItemStack leggings = new ItemStack(material);
            witherskelly.getEquipment().setLeggings(leggings);
        }

        if (SimpleCustomMobs.config.getString(
                "witherskeleton_equipment.boots") != null) {

            material = Material.valueOf(SimpleCustomMobs.config.getString(
                    "witherskeleton_equipment.boots"));

            ItemStack boots = new ItemStack(material);
            witherskelly.getEquipment().setBoots(boots);
        }
        /*
        // Mojang's attributes are inconsistent and difficult to use.
        // It is best to avoid them if you can.
        // Especially when they are tied to a config.
        // In general, using potion effects are easier and flexible.
         */
        // Heath points added are empty when the entity spawns
        int health_val = SimpleCustomMobs.config.getInt("witherskeleton_stats.health");
        AttributeInstance hp_attribute = witherskelly.getAttribute(
                Attribute.GENERIC_MAX_HEALTH);
        hp_attribute.setBaseValue(health_val);
        // Required to guarantee that the entity will have full health on spawn
        witherskelly.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 1, 99));

        if (SimpleCustomMobs.config.getInt("witherskeleton_stats.resistance") > 0) {
            int resistance_val = SimpleCustomMobs.config.getInt(
                    "witherskeleton_stats.resistance");
            witherskelly.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    1000000000, resistance_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("witherskeleton_stats.speed") > 0) {
            int speed_val = SimpleCustomMobs.config.getInt(
                    "witherskeleton_stats.speed");
            witherskelly.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    1000000000, speed_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("witherskeleton_stats.strength") > 0) {
            int strength_val = SimpleCustomMobs.config.getInt(
                    "witherskeleton_stats.strength");
            witherskelly.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    1000000000, strength_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("witherskeleton_stats.jump") > 0) {
            int jump_val = SimpleCustomMobs.config.getInt(
                    "witherskeleton_stats.jump");
            witherskelly.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP,
                    1000000000, jump_val - 1));
        }

        witherskelly.setMetadata("CustomWitherSkeleton",
                new FixedMetadataValue(plugin,
                        "customwitherskeleton"));

        int max_targeting_distance = SimpleCustomMobs.config.getInt(
                "witherskeleton_stats.max_targeting_distance");
        new BukkitRunnable() {
            public void run() {
                if (!witherskelly.isDead()) {
                    if (witherskelly.getTarget() == null) {
                        for (Entity ent : witherskelly.getNearbyEntities(
                                max_targeting_distance, // X
                                max_targeting_distance, // Y
                                max_targeting_distance)) // Z
                        {
                            if (ent instanceof Player) {
                                Player p = (Player) ent;
                                if (p.getGameMode() == GameMode.SURVIVAL) {
                                    witherskelly.setTarget(p);
                                }
                            }
                        }
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof WitherSkeleton
                && e.getEntity() instanceof Player
                && e.getDamager().hasMetadata("CustomWitherSkeleton")) {

            Player p = (Player) e.getEntity();
            WitherSkeleton witherskelly = (WitherSkeleton) e.getDamager();

            int leech_life_chance = SimpleCustomMobs.config.getInt(
                    "witherskeleton_abilities.leech_life.chance");
            int leech_life_random = ThreadLocalRandom.current().nextInt(leech_life_chance);
            if (leech_life_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "witherskeleton_abilities.leech_life.enable")) {

                    int leech_life_strength = SimpleCustomMobs.config.getInt(
                            "witherskeleton_abilities.leech_life.strength");
                    int leech_life_duration = SimpleCustomMobs.config.getInt(
                            "witherskeleton_abilities.leech_life.duration");
                    witherskelly.addPotionEffect(
                            new PotionEffect(PotionEffectType.HARM,
                                    leech_life_duration, leech_life_strength));
                }
            }
            int withering_chance = SimpleCustomMobs.config.getInt(
                    "witherskeleton_abilities.deep_withering.chance");
            int withering_random = ThreadLocalRandom.current().nextInt(withering_chance);
            if (withering_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "witherskeleton_abilities.deep_withering.enable")) {

                    int withering_strength = SimpleCustomMobs.config.getInt(
                            "witherskeleton_abilities.deep_withering.strength");
                    int withering_duration = SimpleCustomMobs.config.getInt(
                            "witherskeleton_abilities.deep_withering.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.WITHER,
                                    withering_duration, withering_strength));
                    p.sendMessage(ChatColor.RED + "You have been strongly withered!");
                }
            }
            int blindness_chance = SimpleCustomMobs.config.getInt(
                    "witherskeleton_abilities.blindness.chance");
            int blindness_random = ThreadLocalRandom.current().nextInt(blindness_chance);
            if (blindness_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "witherskeleton_abilities.blindness.enable")) {

                    int blindness_strength = SimpleCustomMobs.config.getInt(
                            "witherskeleton_abilities.blindness.strength");
                    int blindness_duration = SimpleCustomMobs.config.getInt(
                            "witherskeleton_abilities.blindness.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.BLINDNESS,
                                    blindness_duration, blindness_strength));
                    p.sendMessage(ChatColor.RED + "You have been blinded!");
                }
            }
        }
        if (e.getDamager() instanceof Player
                && e.getEntity() instanceof WitherSkeleton
                && e.getEntity().hasMetadata("CustomWitherSkeleton")) {

            int hard_bones_chance = SimpleCustomMobs.config.getInt(
                    "witherskeleton_abilities.hard_bones.chance");
            int hard_bones_random = ThreadLocalRandom.current().nextInt(hard_bones_chance);
            if (hard_bones_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "witherskeleton_abilities.hard_bones.enable")) {

                    Player p = (Player) e.getDamager();
                    p.sendMessage(ChatColor.RED +
                            "Your attack bounced off the wither skeleton's hard bones!");
                    p.playSound(p.getLocation(),
                            Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Skeleton
                && SimpleCustomMobs.config.getBoolean(
                "natural_witherskeleton_spawning.enable_spawn")) {

            if (e.getLocation().getBlock().isLiquid()) {
                return;
            }

            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                if (SimpleCustomMobs.config.getBoolean(
                        "natural_witherskeleton_spawning.disable_spawner")) {
                    return;
                }
            }

            int chance = SimpleCustomMobs.config.getInt(
                    "natural_witherskeleton_spawning.spawn_number");
            int random = ThreadLocalRandom.current().nextInt(chance);
            if (random < 1) {
                e.setCancelled(true);
                Skeleton normal_witherskelly = (Skeleton)e.getEntity();
                CustomWitherSkeleton.createCustomWitherSkeleton(
                        normal_witherskelly.getLocation());
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().hasMetadata("CustomWitherSkeleton")
                && e.getEntity() instanceof WitherSkeleton) {
            LivingEntity witherskelly = e.getEntity();

            if (SimpleCustomMobs.config.getBoolean(
                    "witherskeleton_custom_drops.clear_vanilla")) {
                e.getDrops().clear();
            }

            if (SimpleCustomMobs.config.getBoolean(
                    "witherskeleton_custom_drops.enable_drop")) {

                int chance = SimpleCustomMobs.config.getInt(
                        "witherskeleton_custom_drops.axe_of_withering.drop_chance");
                int random = ThreadLocalRandom.current().nextInt(chance);
                if (random < 1) {
                    ItemStack item = new ItemStack(Material.NETHERITE_AXE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.LIGHT_PURPLE
                            + "Axe of Withering");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Deal bonus withering damage!");
                    meta.setLore(lore);
                    meta.setUnbreakable(true);
                    item.setItemMeta(meta);
                    witherskelly.getLocation().getWorld().dropItem(
                            witherskelly.getLocation(), new ItemStack(item));
                }
            }
        }
    }

    @EventHandler
    public void CustomSwordDamageEvent1(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player
                && e.getEntity() instanceof Player) {
            Player damager = (Player)e.getDamager();
            Player damaged = (Player)e.getEntity();
            if (damager.getInventory().getItemInMainHand().
                    getType().equals(Material.NETHERITE_AXE)
                    && damager.getInventory().getItemInMainHand()
                    .getItemMeta().isUnbreakable()) {

                String item_name = damager.getInventory().getItemInMainHand()
                        .getItemMeta().getDisplayName();

                if (!item_name.equals(ChatColor.LIGHT_PURPLE + "Axe of Withering")) {
                    return;
                }

                int chance = SimpleCustomMobs.config.getInt(
                        "witherskeleton_custom_drops.axe_of_withering.effect_chance");
                int random = ThreadLocalRandom.current().nextInt(chance);
                if (random < 1) {

                    int duration = SimpleCustomMobs.config.getInt(
                            "witherskeleton_custom_drops.axe_of_withering.duration");
                    int strength = SimpleCustomMobs.config.getInt(
                            "witherskeleton_custom_drops.axe_of_withering.strength");
                    damaged.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                            duration, strength));
                    damaged.sendMessage(ChatColor.GREEN + ""
                            + ChatColor.ITALIC + "You have been withered!");
                }
            }
        }
        else if (e.getDamager() instanceof Player
                && e.getEntity() instanceof Mob) {
            Player damager = (Player) e.getDamager();
            Mob damaged = (Mob) e.getEntity();
            if (damager.getInventory().getItemInMainHand()
                    .getType().equals(Material.NETHERITE_AXE)
                    && damager.getInventory().getItemInMainHand()
                    .getItemMeta().isUnbreakable()) {

                String item_name = damager.getInventory().getItemInMainHand().
                        getItemMeta().getDisplayName();

                if (!item_name.equals(ChatColor.LIGHT_PURPLE + "Axe of Withering")) {
                    return;
                }

                int chance = SimpleCustomMobs.config.getInt(
                        "witherskeleton_custom_drops.axe_of_withering.effect_chance");
                int random = ThreadLocalRandom.current().nextInt(chance);
                if (random < 1) {

                    int duration = SimpleCustomMobs.config.getInt(
                            "witherskeleton_custom_drops.axe_of_withering.duration");
                    int strength = SimpleCustomMobs.config.getInt(
                            "witherskeleton_custom_drops.axe_of_withering.strength");
                    damaged.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,
                            duration, strength));
                }
            }
        }
    }
}
