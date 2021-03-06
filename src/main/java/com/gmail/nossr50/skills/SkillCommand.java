package com.gmail.nossr50.skills;

import java.text.DecimalFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.commands.CommandHelper;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.utilities.SkillTools;
import com.gmail.nossr50.skills.utilities.SkillType;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.StringUtils;
import com.gmail.nossr50.util.Users;

public abstract class SkillCommand implements CommandExecutor {
    private SkillType skill;
    private String skillString;

    protected Player player;
    protected PlayerProfile profile;
    protected float skillValue;
    protected boolean isLucky;
    protected boolean hasEndurance;

    protected DecimalFormat percent = new DecimalFormat("##0.00%");
    protected DecimalFormat decimal = new DecimalFormat("##0.00");

    public SkillCommand(SkillType skill) {
        this.skill = skill;
        this.skillString = StringUtils.getCapitalized(skill.toString());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (CommandHelper.noConsoleUsage(sender)) {
            return true;
        }

        player = (Player) sender;
        profile = Users.getPlayer(player).getProfile();

        if (profile == null) {
            sender.sendMessage(LocaleLoader.getString("Commands.DoesNotExist"));
            return true;
        }

        skillValue = profile.getSkillLevel(skill);
        isLucky = player.hasPermission("mcmmo.perks.lucky." + skill.toString().toLowerCase());
        hasEndurance = (player.hasPermission("mcmmo.perks.activationtime.twelveseconds") || player.hasPermission("mcmmo.perks.activationtime.eightseconds") || player.hasPermission("mcmmo.perks.activationtime.fourseconds"));

        dataCalculations();
        permissionsCheck();

        player.sendMessage(LocaleLoader.getString("Skills.Header", LocaleLoader.getString(skillString + ".SkillName")));

        if (!skill.isChildSkill()) {
            player.sendMessage(LocaleLoader.getString("Commands.XPGain", LocaleLoader.getString("Commands.XPGain." + skillString)));
            player.sendMessage(LocaleLoader.getString("Effects.Level", profile.getSkillLevel(skill), profile.getSkillXpLevel(skill), profile.getXpToLevel(skill)));
        }

        if (effectsHeaderPermissions()) {
            player.sendMessage(LocaleLoader.getString("Skills.Header", LocaleLoader.getString("Effects.Effects")));
        }

        effectsDisplay();

        if (statsHeaderPermissions()) {
            player.sendMessage(LocaleLoader.getString("Skills.Header", LocaleLoader.getString("Commands.Stats.Self")));
        }

        statsDisplay();

        return SkillGuide.grabGuidePageForSkill(skill, player, args);
    }

    protected String calculateRank(int maxLevel, int rankChangeLevel) {
        if (skillValue >= maxLevel) {
            return String.valueOf(maxLevel / rankChangeLevel);
        }

        return String.valueOf((int) (skillValue / rankChangeLevel));
    }

    protected String[] calculateAbilityDisplayValues(double chance) {
        if (isLucky) {
            double luckyChance = chance * 1.3333D;

            if (luckyChance >= 100D) {
                return new String[] { percent.format(chance / 100.0D), percent.format(1.0D) };
            }

            return new String[] { percent.format(chance / 100.0D), percent.format(luckyChance / 100.0D) };
        }

        return new String[] { percent.format(chance / 100.0D), null };
    }

    protected String[] calculateAbilityDisplayValues(int maxBonusLevel, double maxChance) {
        double abilityChance;

        if (skillValue >= maxBonusLevel) {
            abilityChance = maxChance;
        }
        else {
            abilityChance = (maxChance / maxBonusLevel) * skillValue;
        }

        if (isLucky) {
            double luckyChance = abilityChance * 1.3333D;

            if (luckyChance >= 100D) {
                return new String[] { percent.format(abilityChance / 100.0D), percent.format(1.0D) };
            }

            return new String[] { percent.format(abilityChance / 100.0D), percent.format(luckyChance / 100.0D) };
        }

        return new String[] { percent.format(abilityChance / 100.0D), null };
    }

    protected String[] calculateLengthDisplayValues() {
        int maxLength = skill.getAbility().getMaxTicks();
        int length = 2 + (int) (skillValue / Misc.abilityLengthIncreaseLevel);
        int enduranceLength = 0;

        if (player.hasPermission("mcmmo.perks.activationtime.twelveseconds")) {
            enduranceLength = length + 12;
        }
        else if (player.hasPermission("mcmmo.perks.activationtime.eightseconds")) {
            enduranceLength = length + 8;
        }
        else if (player.hasPermission("mcmmo.perks.activationtime.fourseconds")) {
            enduranceLength = length + 4;
        }

        if (maxLength != 0) {
            if (length > maxLength) {
                length = maxLength;
            }

            if (enduranceLength > maxLength) {
                enduranceLength = maxLength;
            }
        }

        return new String[] { String.valueOf(length), String.valueOf(enduranceLength) };
    }

    protected void luckyEffectsDisplay() {
        if (isLucky) {
            String perkPrefix = LocaleLoader.getString("MOTD.PerksPrefix");
            player.sendMessage(perkPrefix + LocaleLoader.getString("Effects.Template", LocaleLoader.getString("Perks.lucky.name"), LocaleLoader.getString("Perks.lucky.desc", SkillTools.getSkillName(skill))));
        }
    }

    protected abstract void dataCalculations();

    protected abstract void permissionsCheck();

    protected abstract boolean effectsHeaderPermissions();

    protected abstract void effectsDisplay();

    protected abstract boolean statsHeaderPermissions();

    protected abstract void statsDisplay();
}
