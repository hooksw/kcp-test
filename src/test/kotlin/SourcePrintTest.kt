
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import kotlin.test.Test
import kotlin.test.assertEquals

class SourcePrintTest {

    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `func expression`() {
        val ktSource = SourceFile.kotlin(
            "file.kt", """


    """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            sources = listOf(ktSource)
            // pass your own instance of a compiler plugin
            compilerPluginRegistrars = listOf(FunExpReplacePluginRegistrar())

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        assertEquals(result.exitCode , KotlinCompilation.ExitCode.OK)
    }
}

@OptIn(ExperimentalCompilerApi::class)
class FunExpReplacePluginRegistrar() : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(
            object : IrGenerationExtension {
                override fun generate(
                    moduleFragment: IrModuleFragment,
                    pluginContext: IrPluginContext
                ) {
                    println(moduleFragment.dump())
//                    moduleFragment.transformChildrenVoid(object :IrElementTransformerVoid(){
//                        override fun visitExpression(expression: IrExpression): IrExpression {
//                            println(expression.dump())
//                            return super.visitExpression(expression)
//                        }
//                    })
//                    moduleFragment.acceptVoid(object :IrElementVisitorVoid{
//                        override fun visitExpression(expression: IrExpression) {
//                            println(expression.dump())
//                            return super.visitExpression(expression)
//                        }
//                    })
                }

            }
        )
    }

}