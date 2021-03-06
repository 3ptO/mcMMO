package com.gmail.nossr50.skills.excavation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.config.TreasuresConfig;
import com.gmail.nossr50.datatypes.McMMOPlayer;
import com.gmail.nossr50.datatypes.treasure.ExcavationTreasure;
import com.gmail.nossr50.mods.ModChecks;
import com.gmail.nossr50.skills.utilities.SkillTools;
import com.gmail.nossr50.skills.utilities.SkillType;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;

public class Excavation {
    public static boolean requiresTool = Config.getInstance().getExcavationRequiresTool();

    /**
     * Check to see if treasures were found.
     *
     * @param block The block to check
     * @param mcMMOPlayer The player who broke the block
     */
    public static void excavationProcCheck(Block block, McMMOPlayer mcMMOPlayer) {
        Material material = block.getType();
        int xp;

        switch (material) {
        case CLAY:
            xp = Config.getInstance().getExcavationClayXP();
            break;

        case DIRT:
            xp = Config.getInstance().getExcavationDirtXP();
            break;

        case GRASS:
            xp = Config.getInstance().getExcavationGrassXP();
            break;

        case GRAVEL:
            xp = Config.getInstance().getExcavationGravelXP();
            break;

        case MYCEL:
            xp = Config.getInstance().getExcavationMycelXP();
            break;

        case SAND:
            xp = Config.getInstance().getExcavationSandXP();
            break;

        case SOUL_SAND:
            xp = Config.getInstance().getExcavationSoulSandXP();
            break;

        default:
            xp = ModChecks.getCustomBlock(block).getXpGain();;
            break;
        }

        Player player = mcMMOPlayer.getPlayer();
        List<ExcavationTreasure> treasures = new ArrayList<ExcavationTreasure>();

        if (Permissions.excavationTreasures(player)) {
            switch (material) {
            case DIRT:
                treasures = TreasuresConfig.getInstance().excavationFromDirt;
                break;

            case GRASS:
                treasures = TreasuresConfig.getInstance().excavationFromGrass;
                break;

            case SAND:
                treasures = TreasuresConfig.getInstance().excavationFromSand;
                break;

            case GRAVEL:
                treasures = TreasuresConfig.getInstance().excavationFromGravel;
                break;

            case CLAY:
                treasures = TreasuresConfig.getInstance().excavationFromClay;
                break;

            case MYCEL:
                treasures = TreasuresConfig.getInstance().excavationFromMycel;
                break;

            case SOUL_SAND:
                treasures = TreasuresConfig.getInstance().excavationFromSoulSand;
                break;

            default:
                break;
            }

            Location location = block.getLocation();

            for (ExcavationTreasure treasure : treasures) {
                if (mcMMOPlayer.getProfile().getSkillLevel(SkillType.EXCAVATION) >= treasure.getDropLevel()) {
                    int activationChance = SkillTools.calculateActivationChance(Permissions.luckyExcavation(player));

                    if (Misc.getRandom().nextDouble() * activationChance <= treasure.getDropChance()) {
                        xp += treasure.getXp();
                        Misc.dropItem(location, treasure.getDrop());
                    }
                }
            }
        }

        mcMMOPlayer.beginXpGain(SkillType.EXCAVATION, xp);
    }

    /**
     * Handle triple drops from Giga Drill Breaker.
     *
     * @param mcMMOPlayer The player using the ability
     * @param block The block to check
     */
    public static void gigaDrillBreaker(McMMOPlayer mcMMOPlayer, Block block) {
        Excavation.excavationProcCheck(block, mcMMOPlayer);
        Excavation.excavationProcCheck(block, mcMMOPlayer);
   }
}