package com.gmail.nossr50.commands.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.datatypes.McMMOPlayer;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.utilities.SkillTools;
import com.gmail.nossr50.skills.utilities.SkillType;
import com.gmail.nossr50.util.Users;

public class SkillresetCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PlayerProfile profile;
        boolean allSkills = false;
        SkillType skill = null;
        String skillName = "";

        switch (args.length) {
        case 1:
            if (!sender.hasPermission("mcmmo.commands.skillreset")) {
                sender.sendMessage(command.getPermissionMessage());
                return true;
            }

            if (!(sender instanceof Player)) {
                return false;
            }

            if (args[0].equalsIgnoreCase("all")) {
                allSkills = true;
            }
            else if (!SkillTools.isSkill(args[0])) {
                sender.sendMessage(LocaleLoader.getString("Commands.Skill.Invalid"));
                return true;
            }

            profile = Users.getPlayer((Player) sender).getProfile();

            if (allSkills) {
                for (SkillType skillType : SkillType.values()) {
                    if (skillType.isChildSkill()) {
                        continue;
                    }

                    if (!sender.hasPermission("mcmmo.commands.skillreset." + skillType.toString().toLowerCase())) {
                        sender.sendMessage(command.getPermissionMessage());
                        continue;
                    }

                    profile.modifySkill(skillType, 0);
                }

                sender.sendMessage(LocaleLoader.getString("Commands.Reset.All"));
            }
            else {
                skill = SkillType.getSkill(args[0]);
                skillName = SkillTools.getSkillName(skill);

                if (!sender.hasPermission("mcmmo.commands.skillreset." + skill.toString().toLowerCase())) {
                    sender.sendMessage(command.getPermissionMessage());
                    return true;
                }

                profile.modifySkill(skill, 0);
                sender.sendMessage(LocaleLoader.getString("Commands.Reset.Single", skillName));
            }

            return true;

        case 2:
            if (!sender.hasPermission("mcmmo.commands.skillreset.others")) {
                sender.sendMessage(command.getPermissionMessage());
                return true;
            }

            if (args[1].equalsIgnoreCase("all")) {
                allSkills = true;
            }
            else if (!SkillTools.isSkill(args[1])) {
                sender.sendMessage(LocaleLoader.getString("Commands.Skill.Invalid"));
                return true;
            }

            if (!allSkills) {
                skill = SkillType.getSkill(args[1]);
                skillName = SkillTools.getSkillName(skill);

                if (!sender.hasPermission("mcmmo.commands.skillreset.others." + skill.toString().toLowerCase())) {
                    sender.sendMessage(command.getPermissionMessage());
                    return true;
                }
            }

            McMMOPlayer mcMMOPlayer = Users.getPlayer(args[0]);

            // If the mcMMOPlayer doesn't exist, create a temporary profile and check if it's present in the database. If it's not, abort the process.
            if (mcMMOPlayer == null) {
                profile = new PlayerProfile(args[0], false);

                if (!profile.isLoaded()) {
                    sender.sendMessage(LocaleLoader.getString("Commands.DoesNotExist"));
                    return true;
                }

                if (allSkills) {
                    for (SkillType skillType : SkillType.values()) {
                        if (skillType.isChildSkill()) {
                            continue;
                        }

                        if (!sender.hasPermission("mcmmo.commands.skillreset.others." + skillType.toString().toLowerCase())) {
                            sender.sendMessage(command.getPermissionMessage());
                            continue;
                        }

                        profile.modifySkill(skillType, 0);
                    }
                }
                else {
                    profile.modifySkill(skill, 0);
                }

                profile.save(); // Since this is a temporary profile, we save it here.
            }
            else {
                profile = mcMMOPlayer.getProfile();

                if (allSkills) {
                    for (SkillType skillType : SkillType.values()) {
                        if (skillType.isChildSkill()) {
                            continue;
                        }

                        if (!sender.hasPermission("mcmmo.commands.skillreset.others." + skillType.toString().toLowerCase())) {
                            sender.sendMessage(command.getPermissionMessage());
                            continue;
                        }

                        profile.modifySkill(skillType, 0);
                    }

                    mcMMOPlayer.getPlayer().sendMessage(LocaleLoader.getString("Commands.Reset.All"));
                }
                else {
                    profile.modifySkill(skill, 0);
                    mcMMOPlayer.getPlayer().sendMessage(LocaleLoader.getString("Commands.Reset.Single", skillName));
                }
            }

            if (allSkills) {
                sender.sendMessage(LocaleLoader.getString("Commands.addlevels.AwardAll.2", args[0]));
            }
            else {
                sender.sendMessage(LocaleLoader.getString("Commands.mmoedit.Modified.2", skillName, args[0]));
            }

            return true;

        default:
            return false;
        }
    }
}
