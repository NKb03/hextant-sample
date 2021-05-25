/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import bundles.PublicProperty
import bundles.property
import hextant.sample.Identifier
import reaktive.Observer
import reaktive.map.bindings.get
import reaktive.map.reactiveMap
import reaktive.value.ReactiveValue
import reaktive.value.binding.flatMap
import reaktive.value.now
import reaktive.value.reactiveValue
import kotlin.collections.set

class GlobalScope {
    private val defs = reactiveMap<Identifier, FunctionDefinitionEditor>()

    fun addDefinition(editor: FunctionDefinitionEditor): Observer {
        editor.name.result.now?.let { n -> defs.now[n] = editor }
        return editor.name.result.observe { _, old, new ->
            old?.let { n -> defs.now.remove(n) }
            new?.let { n -> defs.now[n] = editor }
        }
    }

    fun removeDefinition(editor: FunctionDefinitionEditor) {
        editor.name.result.now?.let { n -> defs.now.remove(n) }
    }

    fun getDefinition(name: ReactiveValue<Identifier?>): ReactiveValue<FunctionDefinitionEditor?> =
        name.flatMap { n -> n?.let { defs[it] } ?: reactiveValue(null) }

    val definitions: Collection<GlobalFunction>
        get() = defs.now.values.map { e ->
            GlobalFunction(
                e.returnType.result.now,
                e.name.result.now!!,
                e.parameters.results.now
            )
        }

    companion object : PublicProperty<GlobalScope> by property("global scope")
}