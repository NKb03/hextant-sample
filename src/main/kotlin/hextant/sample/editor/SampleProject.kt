package hextant.sample.editor

import bundles.set
import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.project.ProjectType


@ProvideProjectType("Sample Project")
object SampleProject : ProjectType {
    override fun initializeContext(context: Context) {
        context[Scope] = Scope.root()
        context[GlobalScope] = GlobalScope()
    }

    override fun createProject(context: Context): Editor<*> = ProgramEditor(context)
}