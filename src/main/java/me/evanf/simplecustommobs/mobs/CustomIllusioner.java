package me.evanf.simplecustommobs.mobs;

import me.evanf.simplecustommobs.SimpleCustomMobs;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
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
import org.bukkit.inventory.meta.PotionMeta;
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

public class CustomIllusioner implements Listener {

    private static SimpleCustomMobs plugin;

    Map<String, Long> cooldown = new HashMap<>();

    public CustomIllusioner(SimpleCustomMobs main) {
        plugin = main;
    }

    public static void createCustomIllusioner(Location loc) {
        Illusioner illusioner = loc.getWorld().spawn(loc, Illusioner.class);

        String name = SimpleCustomMobs.config.getString("illusioner_name");
        illusioner.setCustomName(ChatColor.BLUE + name);
        illusioner.setCustomNameVisible(true);
        /*
        // Mojang's attributes are inconsistent and difficult to use.
        // It is best to avoid them if you can.
        // Especially when they are tied to a config.
        // In general, using potion effects are easier and flexible.
         */
        // Heath points added are empty when the entity spawns
        int health_val = SimpleCustomMobs.config.getInt("illusioner_stats.health");
        AttributeInstance hp_attribute = illusioner.getAttribute(
                Attribute.GENERIC_MAX_HEALTH);
        hp_attribute.setBaseValue(health_val);
        // Required to guarantee that the entity will have full health on spawn
        illusioner.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 99));

        if (SimpleCustomMobs.config.getInt("illusioner_stats.regen") > 0) {
            int regen_val = SimpleCustomMobs.config.getInt(
                    "illusioner_stats.regen");
            illusioner.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    1000000000, regen_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("illusioner_stats.resistance") > 0) {
            int resistance_val = SimpleCustomMobs.config.getInt(
                    "illusioner_stats.resistance");
            illusioner.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    1000000000, resistance_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("illusioner_stats.speed") > 0) {
            int speed_val = SimpleCustomMobs.config.getInt(
                    "illusioner_stats.speed");
            illusioner.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    1000000000, speed_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("illusioner_stats.strength") > 0) {
            int strength_val = SimpleCustomMobs.config.getInt(
                    "illusioner_stats.strength");
            illusioner.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    1000000000, strength_val - 1));
        }

        if (SimpleCustomMobs.config.getInt("illusioner_stats.jump") > 0) {
            int jump_val = SimpleCustomMobs.config.getInt(
                    "illusioner_stats.jump");
            illusioner.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP,
                    1000000000, jump_val - 1));
        }

        illusioner.setMetadata("CustomIllusioner",
                new FixedMetadataValue(plugin, "customillusioner"));

        int max_targeting_distance = SimpleCustomMobs.config.getInt(
                "illusioner_stats.max_targeting_distance");
        new BukkitRunnable() {
            public void run() {
                if (!illusioner.isDead()) {
                    if (illusioner.getTarget() == null) {
                        for (Entity ent : illusioner.getNearbyEntities(
                                max_targeting_distance, // X
                                max_targeting_distance, // Y
                                max_targeting_distance)) // Z
                        {
                            if (ent instanceof Player) {
                                Player p = (Player)ent;
                                if (p.getGameMode() == GameMode.SURVIVAL) {
                                    illusioner.setTarget(p);
                                }
                            }
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
        if (e.getDamager() instanceof Player
                && e.getEntity() instanceof Illusioner
                && e.getEntity().hasMetadata("CustomIllusioner")) {

            Illusioner illusioner = (Illusioner)e.getEntity();
            Player p = (Player)e.getDamager();

            int confusion_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.confusion.chance");
            int confusion_random = ThreadLocalRandom.current().nextInt(confusion_chance);
            if (confusion_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.confusion.enable")) {

                    int confusion_strength = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.confusion.strength");
                    int confusion_duration = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.confusion.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.CONFUSION,
                                    confusion_duration, confusion_strength));
                    p.sendMessage(ChatColor.RED + "You have been confused!");
                }
            }

            int poison_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.poison.chance");
            int poison_random = ThreadLocalRandom.current().nextInt(poison_chance);
            if (poison_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.poison.enable")) {

                    int poison_strength = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.poison.strength");
                    int poison_duration = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.poison.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.POISON,
                                    poison_duration, poison_strength));
                    p.sendMessage(ChatColor.RED + "You have been poisoned!");
                }
            }

            int slow_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.slowness.chance");
            int slow_random = ThreadLocalRandom.current().nextInt(slow_chance);
            if (slow_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.slowness.enable")) {

                    int slow_strength = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.slowness.strength");
                    int slow_duration = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.slowness.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.SLOW,
                                    slow_duration, slow_strength));
                    p.sendMessage(ChatColor.RED + "You have been slowed!");
                }
            }

            int levy_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.levitation.chance");
            int levy_random = ThreadLocalRandom.current().nextInt(levy_chance);
            if (levy_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.levitation.enable")) {

                    int levitation_strength = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.levitation.strength");
                    int levitation_duration = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.levitation.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.LEVITATION,
                                    levitation_duration, levitation_strength));
                    p.sendMessage(ChatColor.RED + "You are now floating up!");
                }
            }

            int hunger_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.hunger.chance");
            int hunger_random = ThreadLocalRandom.current().nextInt(hunger_chance);
            if (hunger_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.hunger.enable")) {

                    int hunger_strength = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.hunger.strength");
                    int hunger_duration = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.hunger.duration");
                    p.addPotionEffect(
                            new PotionEffect(PotionEffectType.HUNGER,
                                    hunger_duration, hunger_strength));
                    p.sendMessage(ChatColor.RED + "You now have hunger!");
                }
            }

            int absorb_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.absorption.chance");
            int absorb_random = ThreadLocalRandom.current().nextInt(absorb_chance);
            if (absorb_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.absorption.enable")) {

                    int absorption_strength = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.absorption.strength");
                    int absorption_duration = SimpleCustomMobs.config.getInt(
                            "illusioner_abilities.absorption.duration");
                    illusioner.addPotionEffect(
                            new PotionEffect(PotionEffectType.HEAL,
                                    absorption_duration, absorption_strength));
                    p.sendMessage(ChatColor.RED +
                            "Your attack was absorbed, some damage was negated!");
                }
            }

            int deflect_chance = SimpleCustomMobs.config.getInt(
                    "illusioner_abilities.deflect.chance");
            int deflect_random = ThreadLocalRandom.current().nextInt(deflect_chance);
            if (deflect_random < 1) {

                if (SimpleCustomMobs.config.getBoolean(
                        "illusioner_abilities.deflect.enable")) {

                    p.sendMessage(ChatColor.RED +
                            "Your attack was deflected!");
                    p.playSound(p.getLocation(),
                            Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Pillager
                || e.getEntity() instanceof Vindicator
                || e.getEntity() instanceof Evoker) {
            if (SimpleCustomMobs.config.getBoolean(
                    "natural_illusioner_spawning.enable_spawn")) {

                if (e.getLocation().getBlock().isLiquid()) {
                    return;
                }

                if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    if (SimpleCustomMobs.config.getBoolean(
                            "natural_illusioner_spawning.disable_spawner")) {
                        return;
                    }
                }

                int chance = SimpleCustomMobs.config.getInt(
                        "natural_illusioner_spawning.spawn_number");
                int random = ThreadLocalRandom.current().nextInt(chance);
                if (random < 1) {
                    e.setCancelled(true);
                    CustomIllusioner.createCustomIllusioner(
                            e.getEntity().getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().hasMetadata("CustomIllusioner")
                && e.getEntity() instanceof Illusioner) {
            LivingEntity illusioner = e.getEntity();

            if (SimpleCustomMobs.config.getBoolean(
                    "illusioner_custom_drops.clear_vanilla")) {
                e.getDrops().clear();
            }

            if (SimpleCustomMobs.config.getBoolean(
                    "illusioner_custom_drops.enable_drop")) {

                int chance = SimpleCustomMobs.config.getInt(
                        "illusioner_custom_drops.staff_of_pots.drop_chance");
                int random = ThreadLocalRandom.current().nextInt(chance);
                if (random < 1) {
                    ItemStack item = new ItemStack(Material.BLAZE_ROD);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Staff of Pots");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "R-click to shoot out random harmful potions!");
                    meta.setLore(lore);
                    meta.setUnbreakable(true);
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                    illusioner.getLocation().getWorld().dropItem(illusioner.getLocation(),
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
        if (e.getItem().getType() != Material.BLAZE_ROD) {
            return;
        }

        String item_name = e.getItem().getItemMeta().getDisplayName();

        if (!item_name.equals(ChatColor.GOLD + "Staff of Pots")) {
            return;
        }

        Player p = e.getPlayer();
        if (cooldown.containsKey(p.getName())) {
            if (cooldown.get(p.getName()) > System.currentTimeMillis()) {

                long time_left = (cooldown.get(p.getName())
                        - System.currentTimeMillis()) / 1000;
                p.sendMessage(ChatColor.WHITE + "Staff of Pots "
                        + ChatColor.GRAY + "is recharging for "
                        + time_left + " second(s) " + "more");
                return;
            }
        }
        int cool_down = SimpleCustomMobs.config.getInt(
                "illusioner_custom_drops.staff_of_pots.cool_down");
        cooldown.put(p.getName(),
                System.currentTimeMillis() + (cool_down * 1000L));

        Location loc = p.getLocation();
        loc.setY(loc.getY() + 1.5f);
        float var = 0.50F;
        int vel = SimpleCustomMobs.config.getInt(
                "illusioner_custom_drops.staff_of_pots.velocity");

        int amount = SimpleCustomMobs.config.getInt(
                "illusioner_custom_drops.staff_of_pots.amount");
        for(int i = 0; i < amount; i++){
            ItemStack item = new ItemStack(Material.SPLASH_POTION);
            PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
            int random = ThreadLocalRandom.current().nextInt(9);
            switch (random) {
                case 0:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.WITHER, 300, 0), true);
                    break;
                case 1:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.POISON, 300, 0), true);
                    break;
                case 2:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.SLOW, 300, 0), true);
                    break;
                case 3:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.HUNGER, 300, 0), true);
                    break;
                case 4:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.BLINDNESS, 300, 0), true);
                    break;
                case 5:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.CONFUSION, 300, 0), true);
                    break;
                case 6:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.SLOW_DIGGING, 300, 0), true);
                    break;
                case 7:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.UNLUCK, 300, 0), true);
                    break;
                case 8:
                    potionMeta.addCustomEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS, 300, 0), true);
                    break;
            }
            item.setItemMeta(potionMeta);
            ThrownPotion thrownPotion = p.launchProjectile(ThrownPotion.class);
            thrownPotion.setItem(item);
            Vector v = p.getLocation().getDirection().multiply(vel);
            v.add(new Vector(
                    Math.random() * var - var / 2,
                    Math.random() * var - var / 2,
                    Math.random() * var - var / 2));
            thrownPotion.setVelocity(v);
        }
    }
}
