package hextant.sample.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.editor.CompoundEditor
import hextant.sample.ForLoop
import reaktive.value.ReactiveValue

@ProvideFeature
class ForLoopEditor @ProvideImplementation(EditorFactory::class) constructor(context: Context) :
    CompoundEditor<ForLoop?>(context), StatementEditor<ForLoop> {
    private val initializerContext = context.child()

    val initializer by child(StatementExpander(initializerContext))
    val condition by child(ExprExpander(initializerContext))
    val after by child(StatementExpander(initializerContext))
    val body by child(BlockEditor(context))
    override val result: ReactiveValue<ForLoop?> =
        composeResult { ForLoop(initializer.get(), condition.get(), after.get(), body.get()) }
}
