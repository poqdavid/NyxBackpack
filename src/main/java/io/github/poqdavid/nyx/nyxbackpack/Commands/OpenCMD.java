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
import io.github.poqdavid.nyx.nyxcore.Utils.Invs;
import io.github.poqdavid.nyx.nyxcore.Utils.Tools;
import org.spongepowered.api.Game;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenCMD implements CommandExecutor {

    private final Game game;
    private final NyxBackpack nb;
    private final Invs inv;

    public OpenCMD(Game game, Invs inv, NyxBackpack nb) {
        this.game = game;
        this.nb = nb;
        this.inv = inv;

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
            final Player player_cmd_src = Tools.getPlayer(src);
            final Player player_args = args.<Player>getOne("player").orElse(null);
            final Integer bpszie = args.<Integer>getOne("size").orElse(0);

            if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_MAIN)) {
                if (player_args != null) {
                    if (!player_cmd_src.getUniqueId().equals(player_args.getUniqueId())) {
                        if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_ADMIN_READ)) {
                            if (player_args.hasPermission(BackpackPermission.COMMAND_BACKPACK_MAIN)) {
                                if (args.hasAny("m")) {
                                    if (player_cmd_src.hasPermission(BackpackPermission.COMMAND_BACKPACK_ADMIN_MODIFY)) {
                                        this.backpackcheck(player_args);
                                        this.backpackchecklock(player_args, player_cmd_src);

                                        final Backpack backpack = new Backpack(player_args, player_cmd_src, this.getBackpackSize(player_args, bpszie), true, this.nb);
                                        player_cmd_src.openInventory(backpack.getbackpack());
                                    } else {
                                        throw new CommandPermissionException(Text.of("You don't have permission to modify other backpacks."));
                                    }
                                } else {
                                    this.backpackcheck(player_args);
                                    final Backpack backpack = new Backpack(player_args, player_cmd_src, this.getBackpackSize(player_args, bpszie), false, nb);
                                    player_cmd_src.openInventory(backpack.getbackpack());
                                }
                            } else {
                                throw new CommandPermissionException(Text.of("This user doesn't have permission to use backpack."));
                            }
                        } else {
                            throw new CommandPermissionException(Text.of("You don't have permission to view other backpacks."));
                        }
                    } else {
                        this.backpackchecklock(player_cmd_src, player_cmd_src);

                        final Backpack backpack = new Backpack(player_cmd_src, player_cmd_src, this.getBackpackSize(player_cmd_src), true, nb);
                        player_cmd_src.openInventory(backpack.getbackpack());
                    }
                } else {
                    this.backpackchecklock(player_cmd_src, player_cmd_src);

                    final Backpack backpack = new Backpack(player_cmd_src, player_cmd_src, this.getBackpackSize(player_cmd_src), true, nb);
                    player_cmd_src.openInventory(backpack.getbackpack());
                }
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

    public int getBackpackSize(Player player, Integer size) {
        if (size == 0) {
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
        } else {
            if (size < 6) {
                return 6;
            } else {
                return size;
            }
        }

    }

    private void backpackchecklock(Player player, Player playersrc) throws CommandException {

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

                if (titles == "Backpack") {
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

                if (titles2 == title) {
                    return true;
                }
            }
        }
        return false;
    }

    private void backpackcheck(Player player) throws CommandException {
        Path file = Paths.get(this.nb.getConfigPath() + File.separator + "backpacks" + File.separator + player.getUniqueId() + ".backpack");
        if (!Files.exists(file)) {
            throw new CommandPermissionException(Text.of("Sorry there is no backpack data for " + player.getName()));
        } else {
            String content = null;
            try {
                content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (content == "{}") {
                throw new CommandPermissionException(Text.of("Sorry there is no backpack data for " + player.getName()));
            }
        }
    }
}
