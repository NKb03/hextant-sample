package hextant.sample

import hextant.plugins.PluginInitializer
import hextant.plugins.registerCommand
import hextant.plugins.registerInspection
import hextant.plugins.stylesheet
import hextant.sample.editor.*
import hextant.sample.rt.Interpreter
import hextant.sample.rt.RuntimeContext
import reaktive.value.binding.impl.notNull
import reaktive.value.binding.map
import reaktive.value.binding.or
import reaktive.value.now

object SamplePlugin : PluginInitializer({
    stylesheet("sample.css")
    registerCommand<ProgramEditor, Unit> {
        name = "Execute Program"
        shortName = "execute"
        description = "Executes the program"
        defaultShortcut("Ctrl?+X")
        applicableIf { e -> e.result.now != null }
        executing { program, _ ->
            val result = program.result.now!!
            val interpreter = Interpreter(result.functions)
            val ctx = RuntimeContext.root()
            interpreter.execute(result.main, ctx)
        }
    }
    registerInspection<ReferenceEditor> {
        id = "unresolved.variable"
        description = "Detects unresolved variable references"
        isSevere(true)
        message { "Variable ${inspected.result.now} cannot be resolved" }
        checkingThat {
            val name = inspected.result.map { it?.name }
            val type = inspected.context[Scope].resolve(name, inspected.line)
            type.notNull()
        }
    }
    registerInspection<FunctionCallEditor> {
        id = "unresolved.function"
        description = "Detects unresolved function calls"
        isSevere(true)
        message { "Function ${inspected.name.result.now} cannot be resolved" }
        checkingThat {
            val name = inspected.name.result
            val def = inspected.context[GlobalScope].getDefinition(name)
            name.map { it == null } or def.notNull()
        }
    }
})