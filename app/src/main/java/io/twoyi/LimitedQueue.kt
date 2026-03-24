package io.twoyi

import java.util.LinkedList

class LimitedQueue<E>(private val limit: Int) : LinkedList<E>() {
    override fun add(element: E): Boolean {
        val added = super.add(element)
        while (size > limit) {
            super.remove()
        }
        return added
    }
}