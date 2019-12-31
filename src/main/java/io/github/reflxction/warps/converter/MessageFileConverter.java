/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
package io.github.reflxction.warps.converter;

import net.moltenjson.configuration.direct.DirectConfiguration;
import net.moltenjson.json.JsonFile;

import java.io.File;

public class MessageFileConverter implements Runnable {

    private File file;

    public MessageFileConverter(File messagesFile) {
        file = messagesFile;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (!file.exists()) return;
        DirectConfiguration messagesFile = DirectConfiguration.of(JsonFile.of(file));
        boolean changed = false;
        if (!messagesFile.contains("warmingUp")) {
            messagesFile.set("warmingUp", "&eWarping in &d{warm_up} &esecond(s). Move to cancel.");
            changed = true;
        }
        if (!messagesFile.contains("warmUpCancelled")) {
            messagesFile.set("warmUpCancelled", "&cYou moved. Warping cancelled.");
            changed = true;
        }
        if (changed)
            messagesFile.save(Throwable::printStackTrace);
    }
}
