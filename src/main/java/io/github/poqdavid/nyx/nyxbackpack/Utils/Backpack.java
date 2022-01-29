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

package io.github.poqdavid.nyx.nyxbackpack.Utils;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.poqdavid.nyx.nyxbackpack.NyxBackpack;
import io.github.poqdavid.nyx.nyxcore.Utils.CoreTools;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Backpack {

    private final Path backpackFilePath;
    private final User user_args;
    private final Player playerCmdSrc;
    private final Inventory inventory;
    private final NyxBackpack nb;
    private final Text backpackTitleText;
    private final String backpackTitleStr;
    private final int size;
    private final Boolean saveIt;

    public Backpack(User user_args, Player player_cmd_src, int size, Boolean saveit, NyxBackpack nb) {
        this.backpackFilePath = Paths.get(nb.getBackpackPath() + File.separator + user_args.getUniqueId() + ".backpack");

        this.nb = nb;
        this.user_args = user_args;
        this.playerCmdSrc = player_cmd_src;
        this.size = size;
        this.saveIt = saveit;

        if (!player_cmd_src.getUniqueId().equals(this.user_args.getUniqueId())) {
            this.backpackTitleText = Text.of(this.user_args.getName() + "'s " + "Backpack");
            this.backpackTitleStr = this.user_args.getName() + "'s " + "Backpack";
        } else {
            this.backpackTitleText = Text.of("Backpack");
            this.backpackTitleStr = "Backpack";
        }

        this.inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST).withCarrier(this.playerCmdSrc)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(this.backpackTitleText)))
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, this.size))
                .listener(ClickInventoryEvent.class, this::triggerClickEvent)
                .build(NyxBackpack.getInstance());
        this.loadBackpack(this.user_args, this.nb);
    }

    private void saveBackpack(User user, Map<String, String> items, NyxBackpack nb) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        if (items == null || items.isEmpty()) {
            CoreTools.WriteFile(this.backpackFilePath.toFile(), "{}");
        } else {
            CoreTools.WriteFile(this.backpackFilePath.toFile(), gson.toJson(items));
        }
    }

    private Map<String, String> loadStacks(User user) throws Exception {
        Map<String, String> items = loadSlots();

        for (Inventory slot : this.inventory.slots()) {

            if (slot.getProperty(SlotIndex.class, "slotindex").isPresent()) {

                Integer index = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
                SlotPos slotP = CoreTools.indexToSP(index);
                items.put(slotP.getX() + "," + slotP.getY(), "EMPTY");

                if (slot.size() > 0) {

                    if (slot.peek().isPresent()) {

                        if (slot.getProperty(SlotIndex.class, "slotindex").isPresent()) {

                            if (!slot.peek().get().getType().equals(ItemTypes.NONE)) {
                                try {

                                    items.put(slotP.getX() + "," + slotP.getY(), CoreTools.ItemStackToBase64(slot.peek().get()));

                                } catch (Exception e) {
                                    NyxBackpack.getInstance().getLogger().error("Failed to load a stack data from inventory for this user: " + user.getName() + " SlotPos: " + slotP.getX() + "X," + slotP.getY() + "Y");
                                    e.printStackTrace();
                                    throw new Exception("Failed to load a stack data from inventory for this user: " + user.getName() + " SlotPos: " + slotP.getX() + "X," + slotP.getY() + "Y");
                                }
                            }

                        }

                    }
                }

            }

        }

        return items;

    }

    private Map<String, String> loadSlots() throws Exception {
        final File file = this.backpackFilePath.toFile();

        if (!file.exists()) {
            CoreTools.WriteFile(file, "{}");
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Map<String, String> models = null;
        try {

            models = gson.fromJson(FileUtils.readFileToString(file, Charsets.UTF_8), type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (models != null) {
            return models;
        } else {
            throw new Exception("There was a error loading backpack file. (" + file.getPath() + ")");
        }
    }

    private void loadBackpack(User user, NyxBackpack nb) {
        Map<String, String> items = new HashMap<String, String>();
        try {
            items = loadSlots();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (items != null) {
            for (Map.Entry<String, String> entry : items.entrySet()) {
                if (entry != null) {
                    if (entry.getValue() != null) {
                        final SlotPos sp = SlotPos.of(Integer.parseInt(entry.getKey().split(",")[0]), Integer.parseInt(entry.getKey().split(",")[1]));
                        try {
                            if (!entry.getValue().equals("EMPTY")) {
                                final ItemStack itemST = CoreTools.Base64ToItemStack(entry.getValue());
                                this.inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(sp)).set(itemST);
                            } else {
                                this.inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(sp)).set(ItemStack.empty());
                            }


                        } catch (Exception ex) {
                            NyxBackpack.getInstance().getLogger().error("Failed to load a stack data from file for this user: " + user.getName() + " SlotPos: " + sp.getX() + "X," + sp.getY() + "Y");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void triggerClickEvent(ClickInventoryEvent event) {
        if (this.saveIt) {
            try {
                Map<String, String> items = this.loadStacks(this.user_args);
                this.saveBackpack(this.user_args, items, this.nb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            event.setCancelled(true);
        }
    }

    public Inventory getBackpack() {
        return this.inventory;
    }

}
