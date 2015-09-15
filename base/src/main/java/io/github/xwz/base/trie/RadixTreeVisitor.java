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

/**
 * An interface for implementing visitors that can traverse {@link RadixTree}.
 * A visitor defines how to treat a key/value pair in the radix tree, and can
 * also return a result from the traversal.
 *
 * @param <V> the type stored in the radix tree we will visit
 * @param <R> the type used for results
 */
interface RadixTreeVisitor<V, R> {
    /**
     * Visits a node in a radix tree.
     *
     * @param key   the key of the node being visited
     * @param value the value of the node being visited
     */
    void visit(String key, V value);

    /**
     * An overall result from the traversal of the radix tree.
     *
     * @return the result
     */
    R getResult();
}