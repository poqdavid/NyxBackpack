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

package io.github.poqdavid.nyx.nyxbackpack.Listeners;

import io.github.poqdavid.nyx.nyxbackpack.NyxBackpack;
import io.github.poqdavid.nyx.nyxbackpack.Utils.Backpack;
import io.github.poqdavid.nyx.nyxcore.Utils.CoreTools;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;
import java.util.UUID;

public class NyxBackpackListener {

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Optional<Player> optionalPlayer = CoreTools.getPlayer(event.getCause());
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            CoreTools.MakeNewBP(player);

            if (!NyxBackpack.Backpacks.containsKey(player.getUniqueId())) {
                Backpack backpack = new Backpack(player, CoreTools.getBackpackSize(player));
                NyxBackpack.Backpacks.put(player.getUniqueId(), backpack);
            }
        }
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
        Optional<Player> optionalPlayer = CoreTools.getPlayer(event.getCause());

        if (optionalPlayer.isPresent()) {
            final UUID uuid = optionalPlayer.get().getUniqueId();

            if (NyxBackpack.Backpacks.containsKey(uuid)) {
                if (NyxBackpack.Backpacks.get(uuid).readOnly.isEmpty()) {
                    NyxBackpack.Backpacks.remove(uuid);
                }
            }
        }
    }

}
