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
 * Radix tree utility functions.
 */
class RadixTreeUtil {
    /**
     * Finds the length of the largest prefix for two character sequences.
     *
     * @param a character sequence
     * @param b character sequence
     * @return the length of largest prefix of <code>a</code> and <code>b</code>
     * @throws IllegalArgumentException if either <code>a</code> or <code>b</code>
     *                                  is <code>null</code>
     */
    public static int largestPrefixLength(CharSequence a, CharSequence b) {
        int len = 0;
        for (int i = 0; i < Math.min(a.length(), b.length()); ++i) {
            if (a.charAt(i) != b.charAt(i))
                break;
            ++len;
        }
        return len;
    }


    /**
     * Prints a radix tree to <code>System.out</code>.
     *
     * @param tree the tree
     */
    public static <V> void dumpTree(RadixTree<V> tree) {
        dumpTree(tree.root, "");
    }

    /**
     * Prints a subtree to <code>System.out</code>.
     *
     * @param node         the subtree
     * @param outputPrefix prefix to be printed to output
     */
    private static <V> void dumpTree(RadixTreeNode<V> node, String outputPrefix) {
        if (node.hasValue())
            System.out.format("%s{%s : %s}%n", outputPrefix, node.getPrefix(), node.getValue());
        else
            System.out.format("%s{%s}%n", outputPrefix, node.getPrefix(), node.getValue());

        for (RadixTreeNode<V> child : node)
            dumpTree(child, outputPrefix + "\t");
    }
}