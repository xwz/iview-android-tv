/*
 * Copyright (C) 2012 Jason Gedge <www.gedge.ca>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 *     The above copyright notice and this permission notice shall be included
 *     in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.github.xwz.base.trie;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A node in a radix tree.
 *
 * @param <V>
 */
class RadixTreeNode<V> implements Iterable<RadixTreeNode<V>>, Comparable<RadixTreeNode<V>>, Serializable {
    /**
     * The prefix at this node
     */
    private String prefix;

    /**
     * The value stored at this node
     */
    private V value;

    /**
     * Whether or not this node stores a value. This value is mainly used by
     * {@link RadixTreeVisitor} to figure out whether or not this node should
     * be visited.
     */
    private boolean hasValue;

    /**
     * The children for this node. Note, because we use {@link TreeSet} here,
     * traversal of {@link RadixTree} will be in lexicographical order.
     */
    private Collection<RadixTreeNode<V>> children;

    /**
     * Constructs a node from the given prefix.
     *
     * @param prefix the prefix
     */
    RadixTreeNode(String prefix) {
        this(prefix, null);
        this.hasValue = false;
    }

    /**
     * Constructs a node from the given prefix and value.
     *
     * @param prefix the prefix
     * @param value  the value
     */
    RadixTreeNode(String prefix, V value) {
        this.prefix = prefix;
        this.value = value;
        this.hasValue = true;
    }


    /**
     * Gets the value attached to this node.
     *
     * @return the value, or <code>null</code> if an internal node
     */
    V getValue() {
        return value;
    }

    /**
     * Sets the value attached to this node.
     *
     * @param value the value, or <code>null</code> if an internal node
     */
    void setValue(V value) {
        this.value = value;
    }

    /**
     * Gets the prefix associated with this node.
     *
     * @return the prefix
     */
    String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix associated with this node.
     *
     * @param prefix the prefix
     */
    void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the children of this node.
     *
     * @return the list of children
     */
    Collection<RadixTreeNode<V>> getChildren() {
        // Delayed creation of children to reduce memory cost
        if (children == null)
            children = new TreeSet<>();
        return children;
    }

    /**
     * Whether or not this node has a value attached to it.
     *
     * @return whether or not this node has a value
     */
    boolean hasValue() {
        return hasValue;
    }

    /**
     * Sets whether or not this node has a value attached to it.
     *
     * @param hasValue <code>true</code> if this node will have a value,
     *                 <code>false</code> otherwise. If <code>false</code>,
     *                 {@link #getValue()} will return <code>null</code>
     *                 after this call.
     */
    void setHasValue(boolean hasValue) {
        this.hasValue = hasValue;
        if (!hasValue)
            this.value = null;
    }

    @Override
    public Iterator<RadixTreeNode<V>> iterator() {
        if (children == null) {
            return new Iterator<RadixTreeNode<V>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public RadixTreeNode<V> next() {
                    return null;
                }

                @Override
                public void remove() {
                }
            };
        }

        return children.iterator();
    }

    @Override
    public int compareTo(RadixTreeNode<V> node) {
        return prefix.compareTo(node.getPrefix());
    }
}