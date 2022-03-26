/*
 *     This file is part of NyxBackpack.
 *
 *     NyxBackpack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     NyxBackpack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with NyxBackpack.  If not, see <https://www.gnu.org/licenses/>.
 *
 *     Copyright (c) POQDavid <https://github.com/poqdavid/NyxBackpack>
 *     Copyright (c) contributors
 */

package io.github.poqdavid.nyx.nyxbackpack.Commands;

import io.github.poqdavid.nyx.nyxbackpack.NyxBackpack;
import io.github.poqdavid.nyx.nyxbackpack.Utils.Backpack;
import io.github.poqdavid.nyx.nyxcore.Permissions.BackpackPermission;
import io.github.poqdavid.nyx.nyxcore.Utils.CoreTools;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class OpenCMD implements CommandExecutor {

    private final NyxBackpack nb;

    public OpenCMD(NyxBackpack nb) {
        this.nb = nb;
    }

    public static Text getDescription() {
        return Text.of("/backpack open, /bp open");
    }

    public static String[] getAlias() {
        return new String[]{"open", "op"};
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            final Player player_cmd_src = CoreTools.getPlayer(src);
            final Optional<User> user = args.getOne("user");
            User user_args = null;
            if (user.isPresent()) {
                user_args = user.get();
            }

            src.sendMessage(Text.of("§6Opening Backpack for:"));

            src.sendMessage(Text.of("§6Name: §7" + user_args.getName()));
            src.sendMessage(Text.of("§6UUID: §7" + user_args.getUniqueId()));

            if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_MAIN)) {
                if (user_args != null) {
                    if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_ADMIN_READ)) {
                        if (user_args.hasPermission(BackpackPermission.COMMAND_BACKPACK_MAIN)) {
                            if (args.hasAny("m")) {
                                if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_ADMIN_MODIFY)) {

                                    this.backpackCheck(user_args);
                                    this.backpackCheckLock(user_args, player_cmd_src);

                                    if (!NyxBackpack.Backpacks.containsKey(user_args.getUniqueId())) {
                                        Backpack backpack = new Backpack(user_args, CoreTools.getBackpackSize(user_args));
                                        NyxBackpack.Backpacks.put(user_args.getUniqueId(), backpack);
                                    }

                                    NyxBackpack.Backpacks.get(user_args.getUniqueId()).openBackpack(player_cmd_src, false, false);

                                } else {
                                    throw new CommandPermissionException(Text.of("You don't have permission to modify other backpacks."));
                                }
                            } else {

                                this.backpackCheck(user_args);

                                if (!NyxBackpack.Backpacks.containsKey(user_args.getUniqueId())) {
                                    Backpack backpack = new Backpack(user_args, CoreTools.getBackpackSize(user_args));
                                    NyxBackpack.Backpacks.put(user_args.getUniqueId(), backpack);
                                }

                                NyxBackpack.Backpacks.get(user_args.getUniqueId()).openBackpack(player_cmd_src, true);

                            }
                        } else {
                            throw new CommandPermissionException(Text.of("This user doesn't have permission to use backpack."));
                        }
                    } else {
                        throw new CommandPermissionException(Text.of("You don't have permission to view other backpacks."));
                    }

                } else {
                    throw new CommandPermissionException(Text.of("Player doesn't exist!"));
                }
            } else {
                throw new CommandPermissionException(Text.of("You don't have permission to use this command."));
            }
        } else {
            throw new CommandException(Text.of("You can't use this command if you are not a player!"));
        }
        return CommandResult.success();
    }

    private void backpackCheckLock(User user, Player playersrc) throws CommandException {

        Path file = Paths.get(this.nb.getConfigPath() + File.separator + "backpacks" + File.separator + user.getUniqueId() + ".lock");

        if (Files.exists(file)) {
            throw new CommandPermissionException(Text.of("Sorry currently your backpack is locked."));
        }
    }

    private void backpackCheck(User user) throws CommandException {
        Path file = Paths.get(this.nb.getConfigPath() + File.separator + "backpacks" + File.separator + user.getUniqueId() + ".backpack");
        if (!Files.exists(file)) {
            throw new CommandPermissionException(Text.of("Sorry there is no backpack data for " + user.getName()));
        } else {
            String content = null;
            try {
                content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (content == "{}") {
                throw new CommandPermissionException(Text.of("Sorry there is no backpack data for " + user.getName()));
            }
        }
    }
}
