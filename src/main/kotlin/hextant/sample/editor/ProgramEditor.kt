package hextant.sample.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.sample.Program
import reaktive.Observer
import reaktive.list.observeEach
import reaktive.value.ReactiveValue

class ProgramEditor(context: Context) : CompoundEditor<Program?>(context) {
    val functions by child(FunctionDefinitionListEditor(context))
    val main by child(BlockEditor(context))

    private val observer: Observer

    init {
        observer = functions.editors.observeEach { _, editor ->
            context[GlobalScope].addDefinition(editor)
        } and functions.editors.observeList { ch ->
            if (ch.wasRemoved) context[GlobalScope].removeDefinition(ch.removed)
        }
    }

    override val result: ReactiveValue<Program?> = composeResult {
        Program(functions.get(), main.get())
    }
}
