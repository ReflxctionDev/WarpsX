/*
 * * Copyright 2019 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.reflxction.warps.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Settings controlled by commands
 */
public class PluginData {

    @Expose
    private boolean adminOnly = false;

    @Expose
    private int warpsLimit = 15;

    @Expose
    @SerializedName("bannedPlayers")
    private Set<UUID> bannedUsers = new HashSet<>();

    @Expose
    private int warpCreationCost = 0;

    @Expose
    private Set<UUID> bannedWarpOwners = new HashSet<>();

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public int getWarpsLimit() {
        return warpsLimit;
    }

    public Set<UUID> getBannedUsers() {
        return bannedUsers;
    }

    public Set<UUID> getBannedWarpOwners() {
        return bannedWarpOwners;
    }

    public int getWarpCreationCost() {
        return warpCreationCost;
    }

    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public void setWarpsLimit(int warpsLimit) {
        this.warpsLimit = warpsLimit;
    }

    public void setWarpCreationCost(int warpCreationCost) {
        this.warpCreationCost = warpCreationCost;
    }

}