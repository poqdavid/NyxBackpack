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

import io.github.poqdavid.nyx.nyxcore.Permissions.BackpackPermission;
import io.github.poqdavid.nyx.nyxcore.Utils.CoreTools;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class BackpackLockCMD implements CommandExecutor {

    public BackpackLockCMD() {
    }

    public static Text getDescription() {
        return Text.of("/backpacklock, /bplock");
    }

    public static String[] getAlias() {
        return new String[]{"backpacklock", "bplock"};
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<User> user = args.getOne("user");
        User user_args = null;
        if (user.isPresent()) {
            user_args = user.get();
        }

        if (src.hasPermission(BackpackPermission.COMMAND_BACKPACKLOCK)) {
            if (user_args != null) {
                if (args.hasAny("l") || args.hasAny("u")) {
                    if (args.hasAny("l")) {
                        if (!CoreTools.backpackCheckLock(user_args)) {
                            this.bpLock(user_args, src);
                        }
                    }
                    if (args.hasAny("u")) {
                        if (CoreTools.backpackCheckLock(user_args)) {
                            this.bpunlock(user_args, src);
                        }
                    }
                } else {
                    if (CoreTools.backpackCheckLock(user_args)) {
                        this.bpunlock(user_args, src);
                    } else {
                        this.bpLock(user_args, src);
                    }
                }
            }
        } else {
            throw new CommandPermissionException(Text.of("You don't have permission to use this command."));
        }
        return CommandResult.success();
    }

    private void bpLock(User user_args, CommandSource src) {
        if (CoreTools.lockBackpack(user_args, true)) {
            src.sendMessage(Text.of("Backpack for " + user_args.getName() + " is now locked"));
        } else {
            src.sendMessage(Text.of("Backpack lock for " + user_args.getName() + " didn't work"));
        }
    }

    private void bpunlock(User user_args, CommandSource src) {
        if (CoreTools.unlockBackpack(user_args, true)) {
            src.sendMessage(Text.of("Backpack for " + user_args.getName() + " is now unlocked"));
        } else {
            src.sendMessage(Text.of("Backpack unlock for " + user_args.getName() + " didn't work"));
            src.sendMessage(Text.of("This can be a error or just there is no lock"));
        }
    }
}