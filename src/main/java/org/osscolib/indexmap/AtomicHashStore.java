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
package org.osscolib.indexmap;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


// TODO Serializable (+ transient for computed things like sets), Clonable
public final class AtomicHashStore<K,V> implements IndexMap<K,V>, Iterable<Map.Entry<K,V>>, Serializable {

    private static final long serialVersionUID = 6362537038828380833L;


    private final int mask;
    private final int maskSize;
    private final Node<K,V> root;

    private transient StoreEntrySet<K,V> entrySet = null;



    AtomicHashStore(final int mask, final Node<K,V> root) {
        super();
        this.mask = mask;
        this.maskSize = (31 - Integer.numberOfLeadingZeros(mask + 1)); // maskSize = log2(mask + 1)
        this.root = root;
    }



    @Override
    public int size() {
        if (this.root == null) {
            return 0;
        }
        return this.root.size();
    }



    @Override
    public boolean containsKey(final Object key) {
        return getEntry(hash(key), key) != null;
    }


    @Override
    public V get(final Object key) {
        final Entry<K,V> entry = getEntry(hash(key), key);
        return entry != null ? entry.value : null;
    }



    Entry<K,V> getEntry(final int hash, final Object key) {
        final int m  = this.mask, msize = this.maskSize;
        Node<K,V> node = this.root;
        NodeData<K,V> data = null;
        for (int mh = hash; node != null && (data = node.data) == null; mh = mh >>> msize) {
            node = node.children[mh & m];
        }
        return (data != null && data.hash == hash) ? getEntry(key, data) : null;
    }


    Entry<K,V> getEntry(final Object key, final NodeData<K,V> data) {
        return (data.entry == null) ? getMultiEntry(key, data.entries) : getSingleEntry(key, data.entry);
    }


    Entry<K,V> getSingleEntry(final Object key, final Entry<K,V> entry) {
        return Objects.equals(entry.key, key) ? entry : null;
    }


    Entry<K,V> getMultiEntry(final Object key, final Entry<K,V>[] entries) {
        for (int i = 0; i < entries.length; i++) {
            // TODO Performance degradation with large number of collisions -> adopt some kind of tree?
            if (Objects.equals(entries[i].key, key)) {
                return entries[i];
            }
        }
        return null;
    }




    public AtomicHashStore<K,V> put(final K key, final V value) {

        final int hash = hash(key);
        final Entry<K,V> entry = Entry.build(key, value);

        final Node newRoot;
        if (this.root == null) {

            final NodeData<K,V> newData = new NodeData<>(hash, entry);
            newRoot = new Node<>(newData);

        } else {

            newRoot = this.root.put(hash, 0, this.mask, entry);
            if (this.root == newRoot) {
                return this;
            }

        }

        return new AtomicHashStore<K,V>(this.mask, newRoot);

    }


    public AtomicHashStore<K,V> remove(final Object key) {

        if (this.root == null) {
            return this;
        }

        final Node newRoot = this.root.remove(hash(key), 0, this.mask, key);
        if (this.root == newRoot) {
            return this;
        }

        return new AtomicHashStore<K,V>(this.mask, newRoot);

    }



    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new EntryIterator<>(this.root, this.maskSize);
    }



    public Set<Map.Entry<K,V>> entrySet() {
        StoreEntrySet<K,V> entrySet;
        if ((entrySet = this.entrySet) != null) {
            return entrySet;
        }
        this.entrySet = new StoreEntrySet<>(this);
        return this.entrySet;
    }



    static int hash(final Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }




    String prettyPrint() {
        final AtomicHashVisitor<K,V> visitor = new PrettyPrinter(this.maskSize);
        visitor.visitRoot(this.root);
        return visitor.toString();
    }


}
