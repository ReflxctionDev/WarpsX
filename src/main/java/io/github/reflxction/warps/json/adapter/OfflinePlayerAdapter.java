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
package io.github.reflxction.warps.json.adapter;

import com.google.gson.*;
import io.github.reflxction.warps.config.PlayerStoringStrategy;
import io.github.reflxction.warps.config.PluginSettings;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Type;

/**
 * A custom serialization/deserialization strategy for {@link OfflinePlayer}
 */
public class OfflinePlayerAdapter implements JsonSerializer<OfflinePlayer>, JsonDeserializer<OfflinePlayer> {

    public static final OfflinePlayerAdapter INSTANCE = new OfflinePlayerAdapter();

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context Context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public OfflinePlayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).from(json.getAsString());
    }

    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
     * non-trivial field of the {@code src} object. However, you should never invoke it on the
     * {@code src} object itself since that will cause an infinite loop (Gson will call your
     * call-back method again).</p>
     *
     * @param src       the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @param context   Context
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(OfflinePlayer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).to(src));
    }
}