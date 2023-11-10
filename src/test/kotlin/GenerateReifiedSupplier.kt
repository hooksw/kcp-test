import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.isInlineOrValueClass
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Reified

private val Name_ReifiedSupplier = Name.identifier("ReifiedSupplier")

private val Name_FunInvoke = Name.identifier("invoke")

class GenerateReifiedSupplier(session: FirSession) : FirDeclarationGenerationExtension(session) {

    private val predicate = DeclarationPredicate.create {
        annotated(FqName("Reified"))
    }
    private val map = hashMapOf<FirClassSymbol<*>, FirClassLikeSymbol<*>>()

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        val result = mutableSetOf<Name>()
        if (classSymbol is FirRegularClassSymbol
            && classSymbol.isInlineOrValueClass()
            && session.predicateBasedProvider.matches(predicate, classSymbol)
        ) {
            result += Name_ReifiedSupplier
        }

        return result
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        println("generateNestedClassLikeDeclaration:owner:$owner")
        if (owner !is FirRegularClassSymbol) return null
        if (!session.predicateBasedProvider.matches(predicate, owner)) return null
        return createNestedClass(owner, Name_ReifiedSupplier, KonifyPluginKey, ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
            status {
                isFun = true
            }
        }.symbol.apply {
            map[this] = owner
        }
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val owner=context.owner
        println("generateConstructors:owner:$owner")
        val result = mutableListOf<FirConstructorSymbol>()
        if(owner.name==Name_ReifiedSupplier){
            result+=createConstructor(owner,KonifyPluginKey){

            }.symbol
        }
        return result
    }
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        println("getCallableNamesForClass:classSymbol:$classSymbol")
        if (classSymbol.name == Name_ReifiedSupplier) {
            return setOf(Name_FunInvoke)
        }
        return super.getCallableNamesForClass(classSymbol, context)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        println("generateFunctions:callableId:$callableId")
        val owner = context?.owner ?: return emptyList()
        if (callableId.callableName == Name_FunInvoke) {
            val fn = createMemberFunction(
                owner, KonifyPluginKey, Name_FunInvoke,
                session.builtinTypes.unitType.coneType
            ) {
                modality = Modality.ABSTRACT
            }
            return listOf(fn.symbol)
        }
        return super.generateFunctions(callableId, context)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }
}

object KonifyPluginKey : GeneratedDeclarationKey() {
    override fun toString(): String {
        return "KonifyPlugin"
    }
}