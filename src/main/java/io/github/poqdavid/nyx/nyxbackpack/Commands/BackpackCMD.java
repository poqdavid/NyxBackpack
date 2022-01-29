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
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BackpackCMD implements CommandExecutor {

    private final NyxBackpack nb;

    public BackpackCMD(NyxBackpack nb) {
        this.nb = nb;
    }

    public static Text getDescription() {
        return Text.of("/backpack, /bp");
    }

    public static String[] getAlias() {
        return new String[]{"backpack", "bp"};
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            final Player player_cmd_src = CoreTools.getPlayer(src);

            if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_MAIN)) {

                this.backpackCheckLock(player_cmd_src, player_cmd_src);

                final Backpack backpack = new Backpack(player_cmd_src, player_cmd_src, this.getBackpackSize(player_cmd_src), true, nb);
                player_cmd_src.openInventory(backpack.getBackpack());

            } else {
                throw new CommandPermissionException(Text.of("You don't have permission to use this command."));
            }
        } else {
            throw new CommandException(Text.of("You can't use this command if you are not a player!"));
        }
        return CommandResult.success();
    }

    public int getBackpackSize(Player player) {
        if (player.hasPermission(BackpackPermission.COMMAND_BACKPACK_SIZE_SIX))
            return 6;
        if (player.hasPermission(BackpackPermission.COMMAND_BACKPACK_SIZE_FIVE))
            return 5;
        if (player.hasPermission(BackpackPermission.COMMAND_BACKPACK_SIZE_FOUR))
            return 4;
        if (player.hasPermission(BackpackPermission.COMMAND_BACKPACK_SIZE_THREE))
            return 3;
        if (player.hasPermission(BackpackPermission.COMMAND_BACKPACK_SIZE_TWO))
            return 2;
        if (player.hasPermission(BackpackPermission.COMMAND_BACKPACK_SIZE_ONE))
            return 1;
        return 1;
    }


    private void backpackCheckLock(Player player, Player playerSrc) throws CommandException {

        Path file = Paths.get(this.nb.getConfigPath() + File.separator + "backpacks" + File.separator + player.getUniqueId() + ".lock");

        if (Files.exists(file)) {
            throw new CommandPermissionException(Text.of("Sorry currently your backpack is locked."));
        } else {

            if (isBackpackOpen(player)) {
                throw new CommandPermissionException(Text.of("Sorry currently your backpack is locked!!"));
            }
        }
    }

    private Boolean isBackpackOpen(Player player) {
        String tl = player.getName() + "'s " + "Backpack";
        if (player.isOnline()) {
            if (player.isViewingInventory()) {
                Inventory inv = player.getInventory();

                InventoryTitle title = inv.getInventoryProperty(InventoryTitle.class).orElse(InventoryTitle.of(Text.of("NONE")));
                String titles = TextSerializers.FORMATTING_CODE.serialize(title.getValue());

                if (titles.equals("Backpack")) {
                    return true;
                } else {
                    return searchInvs(tl);
                }

            } else {

                return searchInvs(tl);
            }

        } else {
            return searchInvs(tl);
        }
    }

    private Boolean searchInvs(String title) {
        for (Player pl : this.nb.getGame().getServer().getOnlinePlayers()) {
            if (pl.isViewingInventory()) {
                Inventory inv2 = pl.getInventory();

                InventoryTitle title2 = inv2.getInventoryProperty(InventoryTitle.class).orElse(InventoryTitle.of(Text.of("NONE")));
                String titles2 = TextSerializers.FORMATTING_CODE.serialize(title2.getValue());

                if (titles2.equals(title)) {
                    return true;
                }
            }
        }
        return false;
    }


}
