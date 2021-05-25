/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import bundles.PublicProperty
import bundles.property
import hextant.sample.Identifier
import hextant.sample.SimpleType
import reaktive.Observer
import reaktive.collection.binding.find
import reaktive.dependencies
import reaktive.set.MutableReactiveSet
import reaktive.set.reactiveSet
import reaktive.set.withDependencies
import reaktive.value.ReactiveInt
import reaktive.value.ReactiveValue
import reaktive.value.binding.flatMap
import reaktive.value.binding.map
import reaktive.value.binding.orElse
import reaktive.value.now
import reaktive.value.reactiveValue

class Scope private constructor(private val parent: Scope?) {
    private val definitions = mutableMapOf<Identifier, MutableReactiveSet<Def>>()

    private fun definitions(name: Identifier) =
        definitions.getOrPut(name) { reactiveSet() }

    fun resolve(name: ReactiveValue<Identifier?>, line: ReactiveInt): ReactiveValue<SimpleType?> {
        val t = name.flatMap { r ->
            r?.let { n ->
                definitions(n)
                    .withDependencies { dependencies(it.line, line) }
                    .find { it.line.now < line.now }
                    .map { it?.type }
            } ?: reactiveValue(null)
        }
        return if (parent == null) t
        else t.orElse(parent.resolve(name, line))
    }

    fun addDefinition(
        name: ReactiveValue<Identifier?>,
        line: ReactiveInt,
        type: ReactiveValue<SimpleType?>
    ): Observer {
        addDefinition(name.now, type.now, line)
        return name.observe { _, old, new ->
            removeDefinition(old, type.now, line)
            addDefinition(new, type.now, line)
        } and type.observe { _, old, new ->
            removeDefinition(name.now, old, line)
            addDefinition(name.now, new, line)
        }
    }

    fun availableBindings(line: Int): List<Def> =
        definitions.values.flatMap { it.now }.filter { it.line.now < line } + parent?.availableBindings(line).orEmpty()

    fun removeDefinition(name: Identifier?, type: SimpleType?, line: ReactiveInt) {
        if (name != null && type != null) {
            val def = Def(name, type, line)
            val removed = definitions(name).now.remove(def)
            check(removed) { "Could not remove $def because only ${definitions(name).now} exist" }
        }
    }

    private fun addDefinition(name: Identifier?, type: SimpleType?, line: ReactiveInt) {
        if (name != null && type != null) {
            definitions(name).now.add(Def(name, type, line))
        }
    }

    fun child() = Scope(parent = this)

    data class Def(val name: Identifier, val type: SimpleType, val line: ReactiveInt) {
        override fun equals(other: Any?): Boolean = when {
            this === other                  -> true
            other !is Def                   -> false
            this.name != other.name         -> false
            this.type != other.type         -> false
            this.line.now != other.line.now -> false
            else                            -> true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }

        override fun toString(): String = "$type $name on line ${line.now}"
    }

    companion object : PublicProperty<Scope> by property("scope") {
        fun root() = Scope(parent = null)
    }
}