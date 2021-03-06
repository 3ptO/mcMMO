package com.gmail.nossr50.commands.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.datatypes.McMMOPlayer;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Users;

public class McgodCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PlayerProfile profile;

        switch (args.length) {
        case 0:
            if (!sender.hasPermission("mcmmo.commands.mcgod")) {
                sender.sendMessage(command.getPermissionMessage());
                return true;
            }

            if (!(sender instanceof Player)) {
                return false;
            }

            profile = Users.getPlayer((Player) sender).getProfile();

            if (profile == null) {
                sender.sendMessage(LocaleLoader.getString("Commands.DoesNotExist"));
                return true;
            }

            if (profile.getGodMode()) {
                sender.sendMessage(LocaleLoader.getString("Commands.GodMode.Disabled"));
            }
            else {
                sender.sendMessage(LocaleLoader.getString("Commands.GodMode.Enabled"));
            }

            profile.toggleGodMode();
            return true;

        case 1:
            if (!sender.hasPermission("mcmmo.commands.mcgod.others")) {
                sender.sendMessage(command.getPermissionMessage());
                return true;
            }

            McMMOPlayer mcMMOPlayer = Users.getPlayer(args[0]);

            if (mcMMOPlayer == null) {
                profile = new PlayerProfile(args[0], false);

                if (!profile.isLoaded()) {
                    sender.sendMessage(LocaleLoader.getString("Commands.DoesNotExist"));
                    return true;
                }

                sender.sendMessage(LocaleLoader.getString("Commands.Offline"));
                return true;
            }

            profile = mcMMOPlayer.getProfile();
            Player player = mcMMOPlayer.getPlayer();

            if (!player.isOnline()) {
                sender.sendMessage(LocaleLoader.getString("Commands.Offline"));
                return true;
            }

            if (profile.getGodMode()) {
                player.sendMessage(LocaleLoader.getString("Commands.GodMode.Disabled"));
            }
            else {
                player.sendMessage(LocaleLoader.getString("Commands.GodMode.Enabled"));
            }

            profile.toggleGodMode();
            return true;

        default:
            return false;
        }
    }
}
