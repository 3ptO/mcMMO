package com.gmail.nossr50.skills.repair;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.AdvancedConfig;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.McMMOPlayer;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.utilities.SkillTools;
import com.gmail.nossr50.skills.utilities.SkillType;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.Users;

public class Repair {
    private static final AdvancedConfig advancedConfig = AdvancedConfig.getInstance();

    public static final double REPAIR_MASTERY_CHANCE_MAX = advancedConfig.getRepairMasteryMaxBonus();
    public static final int REPAIR_MASTERY_MAX_BONUS_LEVEL = advancedConfig.getRepairMasteryMaxLevel();
    public static final double SUPER_REPAIR_CHANCE_MAX = advancedConfig.getSuperRepairChanceMax();
    public static final int SUPER_REPAIR_MAX_BONUS_LEVEL = advancedConfig.getSuperRepairMaxLevel();

    public static boolean arcaneForgingDowngrades = advancedConfig.getArcaneForgingDowngradeEnabled();
    public static boolean arcaneForgingEnchantLoss = advancedConfig.getArcaneForgingEnchantLossEnabled();

    public static boolean anvilMessagesEnabled = Config.getInstance().getRepairAnvilMessagesEnabled();
    public static int anvilID = Config.getInstance().getRepairAnvilId();

    /**
     * Handle the Xp gain for repair events.
     *
     * @param mcMMOPlayer Player repairing the item
     * @param durabilityBefore Durability of the item before repair
     * @param modify Amount to modify the durability by
     */
    protected static void xpHandler(McMMOPlayer mcMMOPlayer, short durabilityBefore, short durabilityAfter, double modify) {
        short dif = (short) ((durabilityBefore - durabilityAfter) * modify);
        Player player = mcMMOPlayer.getPlayer();

        player.playSound(player.getLocation(), Sound.ANVIL_USE, Misc.ANVIL_USE_VOLUME, Misc.ANVIL_USE_PITCH);
        mcMMOPlayer.beginXpGain(SkillType.REPAIR, dif * 10);
    }

    /**
     * Get current Arcane Forging rank.
     *
     * @param profile The PlayerProfile of the player to get the rank for
     * @return The player's current Arcane Forging rank
     */
    public static int getArcaneForgingRank(PlayerProfile profile) {
        int skillLevel = profile.getSkillLevel(SkillType.REPAIR);

        if (skillLevel >= advancedConfig.getArcaneForgingRankLevels4()) {
            return 4;
        }
        else if (skillLevel >= advancedConfig.getArcaneForgingRankLevels3()) {
            return 3;
        }
        else if (skillLevel >= advancedConfig.getArcaneForgingRankLevels2()) {
            return 2;
        }
        else if (skillLevel >= advancedConfig.getArcaneForgingRankLevels1()) {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * Handles removing & downgrading enchants.
     *
     * @param player Player repairing the item
     * @param is Item being repaired
     */
    protected static void addEnchants(Player player, ItemStack is) {
        if (Permissions.arcaneBypass(player)) {
            player.sendMessage(LocaleLoader.getString("Repair.Arcane.Perfect"));
            return;
        }
        Map<Enchantment, Integer> enchants = is.getEnchantments();

        if (enchants.size() == 0) {
            return;
        }

        int rank = getArcaneForgingRank(Users.getPlayer(player).getProfile());

        if (rank == 0 || !Permissions.arcaneForging(player)) {
            for (Enchantment x : enchants.keySet()) {
                is.removeEnchantment(x);
            }
            player.sendMessage(LocaleLoader.getString("Repair.Arcane.Lost"));
            return;
        }

        boolean downgraded = false;

        for (Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
            Enchantment enchantment = enchant.getKey();

            int activationChance = SkillTools.calculateActivationChance(Permissions.luckyRepair(player));

            if (Misc.getRandom().nextInt(activationChance) <= getEnchantChance(rank)) {
                int enchantLevel = enchant.getValue();

                if (advancedConfig.getArcaneForgingDowngradeEnabled() && enchantLevel > 1) {
                    if (Misc.getRandom().nextInt(activationChance) < getDowngradeChance(rank)) {
                        is.addEnchantment(enchantment, --enchantLevel);
                        downgraded = true;
                    }
                }
            }
            else {
                is.removeEnchantment(enchantment);
            }
        }

        Map<Enchantment, Integer> newEnchants = is.getEnchantments();

        if (newEnchants.isEmpty()) {
            player.sendMessage(LocaleLoader.getString("Repair.Arcane.Fail"));
        }
        else if (downgraded || newEnchants.size() < enchants.size()) {
            player.sendMessage(LocaleLoader.getString("Repair.Arcane.Downgrade"));
        }
        else {
            player.sendMessage(LocaleLoader.getString("Repair.Arcane.Perfect"));
        }
    }

    /**
     * Gets chance of keeping enchantment during repair.
     *
     * @param rank Arcane Forging rank
     * @return The chance of keeping the enchantment
     */
    public static int getEnchantChance(int rank) {
        switch (rank) {
        case 4:
            return advancedConfig.getArcaneForgingKeepEnchantsChanceRank4();

        case 3:
            return advancedConfig.getArcaneForgingKeepEnchantsChanceRank3();

        case 2:
            return advancedConfig.getArcaneForgingKeepEnchantsChanceRank2();

        case 1:
            return advancedConfig.getArcaneForgingKeepEnchantsChanceRank1();

        default:
            return 0;
        }
    }

    /**
     * Gets chance of enchantment being downgraded during repair.
     *
     * @param rank Arcane Forging rank
     * @return The chance of the enchantment being downgraded
     */
    public static int getDowngradeChance(int rank) {
        switch (rank) {
        case 4:
            return advancedConfig.getArcaneForgingDowngradeChanceRank4();

        case 3:
            return advancedConfig.getArcaneForgingDowngradeChanceRank3();

        case 2:
            return advancedConfig.getArcaneForgingDowngradeChanceRank2();

        case 1:
            return advancedConfig.getArcaneForgingDowngradeChanceRank1();

        default:
            return 100;
        }
    }

    /**
     * Computes repair bonuses.
     *
     * @param player The player repairing an item
     * @param skillLevel the skillLevel of the player in Repair
     * @param durability The durability of the item being repaired
     * @param repairAmount The base amount of durability repaired to the item
     * @return The final amount of durability repaired to the item
     */
    protected static short repairCalculate(Player player, int skillLevel, short durability, int repairAmount) {
        float  bonus;
        if (skillLevel >= REPAIR_MASTERY_MAX_BONUS_LEVEL) bonus = (float) (REPAIR_MASTERY_CHANCE_MAX / 100F);
        else bonus = (((float) skillLevel) / ((float) REPAIR_MASTERY_MAX_BONUS_LEVEL)) * (float) ((REPAIR_MASTERY_CHANCE_MAX) / 100F);

        if (Permissions.repairMastery(player)) {
            bonus = repairAmount * bonus;
            repairAmount += (int) bonus;
        }

        if (checkPlayerProcRepair(player)) {
            repairAmount = (int) (repairAmount * 2D);
        }

        if (repairAmount <= 0 || repairAmount > 32767)
            repairAmount = 32767;

        durability -= repairAmount;

        if (durability < 0) {
            durability = 0;
        }

        return durability;
    }

    /**
     * Checks for Super Repair bonus.
     *
     * @param player The player repairing an item
     * @return true if bonus granted, false otherwise
     */
    public static boolean checkPlayerProcRepair(Player player) {
        int skillLevel = Users.getPlayer(player).getProfile().getSkillLevel(SkillType.REPAIR);

        int chance = (int) ((SUPER_REPAIR_CHANCE_MAX / SUPER_REPAIR_MAX_BONUS_LEVEL) * skillLevel);
        if (skillLevel >= SUPER_REPAIR_MAX_BONUS_LEVEL) chance = (int) SUPER_REPAIR_CHANCE_MAX;

        int activationChance = SkillTools.calculateActivationChance(Permissions.luckyRepair(player));

        if (chance > Misc.getRandom().nextInt(activationChance) && Permissions.repairBonus(player)) {
            player.sendMessage(LocaleLoader.getString("Repair.Skills.FeltEasy"));
            return true;
        }
        return false;
    }

    /**
     * Handles notifications for placing an anvil.
     *
     * @param player The player placing the anvil
     * @param anvilID The item ID of the anvil block
     */
    public static void placedAnvilCheck(Player player, int anvilID) {
        PlayerProfile profile = Users.getPlayer(player).getProfile();

        if (!profile.getPlacedAnvil()) {
            if (mcMMO.spoutEnabled) {
                SpoutPlayer spoutPlayer = SpoutManager.getPlayer(player);

                if (spoutPlayer.isSpoutCraftEnabled()) {
                    spoutPlayer.sendNotification(LocaleLoader.getString("Repair.AnvilPlaced.Spout1"), LocaleLoader.getString("Repair.AnvilPlaced.Spout2"), Material.getMaterial(anvilID));
                }
            }
            else {
                player.sendMessage(LocaleLoader.getString("Repair.Listener.Anvil"));
            }

            player.playSound(player.getLocation(), Sound.ANVIL_LAND, Misc.ANVIL_USE_VOLUME, Misc.ANVIL_USE_PITCH);
            profile.togglePlacedAnvil();
        }
    }
}
