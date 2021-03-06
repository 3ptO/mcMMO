package com.gmail.nossr50.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.database.Leaderboard;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.runnables.McTopAsync;
import com.gmail.nossr50.skills.utilities.SkillTools;
import com.gmail.nossr50.skills.utilities.SkillType;
import com.gmail.nossr50.util.StringUtils;

public class MctopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean useMySQL = Config.getInstance().getUseMySQL();

        switch (args.length) {
        case 0:
            display(1, "ALL", sender, useMySQL, command);
            return true;

        case 1:
            if (StringUtils.isInt(args[0])) {
                display(Integer.parseInt(args[0]), "ALL", sender, useMySQL, command);
            }
            else if (SkillTools.isSkill(args[0])) {
                display(1, SkillType.getSkill(args[0]).toString(), sender, useMySQL, command);
            }
            else {
                sender.sendMessage(LocaleLoader.getString("Commands.Skill.Invalid"));
            }

            return true;

        case 2:
            if (!StringUtils.isInt(args[1])) {
                return false;
            }

            if (SkillTools.isSkill(args[0])) {
                display(Integer.parseInt(args[1]), SkillType.getSkill(args[0]).toString(), sender, useMySQL, command);
            }
            else {
                sender.sendMessage(LocaleLoader.getString("Commands.Skill.Invalid"));
            }

            return true;

        default:
            return false;
        }
    }

    private void display(int page, String skill, CommandSender sender, boolean sql, Command command) {
        if (sql) {
            if (skill.equalsIgnoreCase("all")) {
                sqlDisplay(page, "taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing", sender, command);
            }
            else {
                sqlDisplay(page, skill, sender, command);
            }
        }
        else {
            flatfileDisplay(page, skill, sender, command);
        }
    }

    private void flatfileDisplay(int page, String skill, CommandSender sender, Command command) {
        if (!skill.equalsIgnoreCase("all") && !sender.hasPermission("mcmmo.commands.mctop." + skill.toLowerCase())) {
            sender.sendMessage(command.getPermissionMessage());
            return;
        }

        Leaderboard.updateLeaderboards(); //Make sure we have the latest information

        String[] info = Leaderboard.retrieveInfo(skill, page);

        if (skill.equalsIgnoreCase("all")) {
            sender.sendMessage(LocaleLoader.getString("Commands.PowerLevel.Leaderboard"));
        }
        else {
            sender.sendMessage(LocaleLoader.getString("Commands.Skill.Leaderboard", StringUtils.getCapitalized(skill)));
        }

        int n = (page * 10) - 9; // Position
        for (String x : info) {
            if (x != null) {
                String digit = String.valueOf(n);

                if (n < 10) {
                    digit = "0" + digit;
                }

                String[] splitx = x.split(":");

                // Format: 1. Playername - skill value
                sender.sendMessage(digit + ". " + ChatColor.GREEN + splitx[1] + " - " + ChatColor.WHITE + splitx[0]);
                n++;
            }
        }

        sender.sendMessage(LocaleLoader.getString("Commands.mctop.Tip"));
    }

    private void sqlDisplay(int page, String query, CommandSender sender, Command command) {
        Bukkit.getScheduler().runTaskAsynchronously(mcMMO.p, new McTopAsync(page, query, sender, command));
    }
}
