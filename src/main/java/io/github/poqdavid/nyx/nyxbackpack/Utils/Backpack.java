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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Backpack {

    private final Path backpackFilePath;
    private final Text backpackTitleText;
    private final User owner;
    private final Inventory inventory;
    public Map<UUID, Boolean> readOnly = new HashMap<>();
    public int size;
    public Boolean ownerAllowed = true;

    public Backpack(User owner, int size) {
        this.backpackFilePath = Paths.get(NyxBackpack.getInstance().getBackpackPath() + File.separator + owner.getUniqueId() + ".backpack");

        this.owner = owner;
        this.size = size;

        this.backpackTitleText = Text.of("Backpack");

        this.inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST).withCarrier(owner)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(this.backpackTitleText)))
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, this.size))
                .listener(ClickInventoryEvent.class, this::triggerClickEvent)
                .listener(InteractInventoryEvent.Close.class, this::triggerInventoryCloseEvent)
                .listener(InteractInventoryEvent.Open.class, this::triggerInventoryOpenEvent)
                .build(NyxBackpack.getInstance());

        this.loadBackpack(this.owner);
    }

    public void openBackpack() {
        openBackpack(Sponge.getServer().getPlayer(owner.getUniqueId()).get(), false);
    }

    public void openBackpack(Player player, Boolean readonly, Boolean ownerallowed) {
        this.ownerAllowed = ownerallowed;
        openBackpack(player, readonly);
    }

    public void openBackpack(Player player, Boolean readonly) {
        String inventoryname = "";
        if (!player.getUniqueId().equals(this.owner.getUniqueId())) {
            inventoryname = owner.getName() + "'s " + "Backpack";
            this.readOnly.put(player.getUniqueId(), readonly);
        } else {
            inventoryname = "Backpack";
        }

        player.openInventory(this.inventory, Text.of(inventoryname));
    }

    private void saveBackpack(User owner) {
        try {
            Map<String, String> items = this.loadStacks(owner);
            this.saveBackpack(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBackpack(Map<String, String> items) {
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

    private void loadBackpack(User user) {
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
        final Optional<Player> optionalPlayer = CoreTools.getPlayer(event.getCause());

        if (optionalPlayer.isPresent()) {
            final Player player = optionalPlayer.get();

            if (player.getUniqueId().equals(this.owner.getUniqueId())) {
                if (this.ownerAllowed && !this.backpackCheckLock(owner)) {
                    this.saveBackpack(this.owner);
                } else {
                    event.setCancelled(true);
                }
            } else {
                if (readOnly.containsKey(player.getUniqueId())) {

                    if (readOnly.get(player.getUniqueId())) {
                        event.setCancelled(true);
                    } else {
                        this.saveBackpack(this.owner);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    private void triggerInventoryOpenEvent(InteractInventoryEvent.Open event) {
        final Optional<Player> optionalPlayer = CoreTools.getPlayer(event.getCause());

        if (optionalPlayer.isPresent()) {
            final Player player = optionalPlayer.get();
            if (!player.getUniqueId().equals(this.owner.getUniqueId())) {
                player.sendMessage(Text.of("§6Backpack is Open!!"));
            }
        }
    }

    private void triggerInventoryCloseEvent(InteractInventoryEvent.Close event) {
        final Optional<Player> optionalPlayer = CoreTools.getPlayer(event.getCause());

        if (optionalPlayer.isPresent()) {
            final Player player = optionalPlayer.get();
            if (readOnly.containsKey(player.getUniqueId())) {

                if (!readOnly.get(player.getUniqueId())) {
                    this.ownerAllowed = true;
                }
                readOnly.remove(player.getUniqueId());
                player.sendMessage(Text.of("§6Closed Backpack for:"));
                player.sendMessage(Text.of("§6Name: §7" + owner.getName()));
                player.sendMessage(Text.of("§6UUID: §7" + owner.getUniqueId()));
            }
        }
    }

    private Boolean backpackCheckLock(User user) {
        Path file = Paths.get(NyxBackpack.getInstance().getConfigPath() + File.separator + "backpacks" + File.separator + user.getUniqueId() + ".lock");
        return Files.exists(file);
    }

    public Inventory getBackpack() {
        return this.inventory;
    }

}
