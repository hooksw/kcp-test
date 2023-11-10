import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals


@OptIn(ExperimentalCompilerApi::class)
class ReifiedSupplierTest {
    @Test
    fun test() {
        val ktSource = SourceFile.kotlin(
            "file.kt", """
    @JvmInline
    @Reified
    value class IntWrap(val int: Int){
    fun interface M{
        fun invoke():IntWrap
    }
    }
    fun test(param:IntWrap.ReifiedSupplier=IntWrap.ReifiedSupplier{IntWrap(1)}){

    }
    """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            sources = listOf(ktSource)
            // pass your own instance of a compiler plugin
            compilerPluginRegistrars = listOf(ReifiedPlugin)

            languageVersion="2.0"
            inheritClassPath = true
            supportsK2=true
            multiplatform=true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    object ReifiedPlugin : CompilerPluginRegistrar() {
        override val supportsK2: Boolean=true

        override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
            IrGenerationExtension.registerExtension(
                object : IrGenerationExtension {
                    override fun generate(
                        moduleFragment: IrModuleFragment,
                        pluginContext: IrPluginContext
                    ) {
                        println(moduleFragment.dump())
//                        moduleFragment.dump().also {
//                            FileOutputStream("D:\\Desktop\\a.txt").apply {
//                                write(it.toByteArray())
//                                flush()
//                            }
//                        }
                    }
                }
            )
            FirExtensionRegistrarAdapter.registerExtension(
                object : FirExtensionRegistrar() {
                    override fun ExtensionRegistrarContext.configurePlugin() {
                        println("FirExtensionRegistrarAdapter")
                        +::GenerateReifiedSupplier
                    }
                }
            )
        }

    }
}