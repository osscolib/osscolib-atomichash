/*
 * =============================================================================
 *
 *   Copyright (c) 2019, The OSSCOLIB team (http://www.osscolib.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.osscolib.atomichash;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

public final class AtomicHashMap<K,V> implements AtomicHash<K,V>, Serializable {

    private static final long serialVersionUID = 2626373528770987645L;

    private final AtomicReference<AtomicHashStore<K,V>> innerMap;



    AtomicHashMap(final int maskSize, final Node<K,V> root) {
        super();
        this.innerMap = new AtomicReference<>();
        this.innerMap.set(new AtomicHashStore<>(maskSize, root));
    }



    @Override
    public int size() {
        return this.innerMap.get().size();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.innerMap.get().containsKey(key);
    }

    @Override
    public V get(final Object key) {
        return this.innerMap.get().get(key);
    }


}