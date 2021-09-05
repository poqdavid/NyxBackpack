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
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;


public class CommandManager {

    public static CommandSpec helpCmd;
    public static CommandSpec openCmd;
    public static CommandSpec backpackCmd;
    public static CommandSpec backpacklockCmd;
    private final Game game;
    private final NyxBackpack nb;


    public CommandManager(Game game, NyxBackpack nb) {
        this.game = game;
        this.nb = nb;
        registerCommands();
    }

    public void registerCommands() {
        helpCmd = CommandSpec.builder()
                .description(Text.of("/bp help"))
                .executor(new HelpCMD())
                .build();

        openCmd = CommandSpec.builder()
                .description(OpenCMD.getDescription())
                .executor(new OpenCMD(this.nb))
                .arguments(GenericArguments.user(Text.of("user")), GenericArguments.flags().flag("m").buildWith(GenericArguments.none()), GenericArguments.optional(GenericArguments.integer(Text.of("size"))))
                .build();

        backpacklockCmd = CommandSpec.builder()
                .description(BackpackLockCMD.getDescription())
                .executor(new BackpackLockCMD())
                .arguments(GenericArguments.user(Text.of("user")), GenericArguments.flags().flag("l").flag("u").buildWith(GenericArguments.none()))
                .build();

        backpackCmd = CommandSpec.builder()
                .description(BackpackCMD.getDescription())
                .executor(new BackpackCMD(this.nb))
                .child(openCmd, OpenCMD.getAlias())
                .child(helpCmd, HelpCMD.getAlias())
                .build();

        game.getCommandManager().register(nb, backpacklockCmd, BackpackLockCMD.getAlias());
        game.getCommandManager().register(nb, backpackCmd, BackpackCMD.getAlias());
        game.getCommandManager().register(nb, openCmd, "backpackopen", "bpop");

    }

}
