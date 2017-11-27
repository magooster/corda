package net.corda.node.services.statemachine

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.IllegalFlowLogicException
import net.corda.core.flows.SchedulableFlow
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class FlowLogicRefFactoryImplTest {

    data class ParamType1(val value: Int)
    data class ParamType2(val value: String)

    @Suppress("UNUSED_PARAMETER", "unused") // Things are used via reflection.
    class KotlinFlowLogic(A: ParamType1, b: ParamType2) : FlowLogic<Unit>() {
        constructor() : this(ParamType1(1), ParamType2("2"))

        constructor(C: ParamType2) : this(ParamType1(1), C)

        constructor(illegal: Duration) : this(ParamType1(1), ParamType2(illegal.toString()))

        constructor(primitive: String) : this(ParamType1(1), ParamType2(primitive))

        constructor(kotlinType: Int) : this(ParamType1(kotlinType), ParamType2("b"))

        override fun call() = Unit
    }

    @SchedulableFlow
    class SchedulableKotlinFlow(value: String): FlowLogic<Unit>() {
        override fun call() = Unit
    }

    class KotlinNoArgFlowLogic : FlowLogic<Unit>() {
        override fun call() = Unit
    }

    class NonSchedulableFlow : FlowLogic<Unit>() {
        override fun call() = Unit
    }

    @Test
    fun `create kotlin no arg`() {
        FlowLogicRefFactoryImpl.createForRPC(KotlinNoArgFlowLogic::class.java)
    }

    @Test
    fun `should create kotlin types`() {
        val args = mapOf(Pair("A", ParamType1(1)), Pair("b", ParamType2("Hello Jack")))
        FlowLogicRefFactoryImpl.createKotlin(KotlinFlowLogic::class.java, args)
    }

    @Test
    fun `create primary`() {
        FlowLogicRefFactoryImpl.createForRPC(KotlinFlowLogic::class.java, ParamType1(1), ParamType2("Hello Jack"))
    }

    @Test
    fun `create kotlin void`() {
        FlowLogicRefFactoryImpl.createKotlin(KotlinFlowLogic::class.java, emptyMap())
    }

    @Test
    fun `create kotlin non primary`() {
        val args = mapOf(Pair("C", ParamType2("Hello Jack")))
        FlowLogicRefFactoryImpl.createKotlin(KotlinFlowLogic::class.java, args)
    }

    @Test
    fun `create java primitive no registration required`() {
        val args = mapOf(Pair("primitive", "A string"))
        FlowLogicRefFactoryImpl.createKotlin(KotlinFlowLogic::class.java, args)
    }

    @Test
    fun `create kotlin primitive no registration required`() {
        val args = mapOf(Pair("kotlinType", 3))
        FlowLogicRefFactoryImpl.createKotlin(KotlinFlowLogic::class.java, args)
    }

    @Test(expected = IllegalFlowLogicException::class)
    fun `create for non-schedulable flow logic`() {
        FlowLogicRefFactoryImpl.create(NonSchedulableFlow::class.java)
    }

    @Test
    fun `should verify reference`() {
        val verified = FlowLogicRefFactoryImpl.verify(SchedulableKotlinFlow::class.java, "Hello Jack")
        assertEquals("net.corda.node.services.statemachine.FlowLogicRefFactoryImplTest\$SchedulableKotlinFlow", verified.flowClass)
        FlowLogicRefFactoryImpl.create(verified)
    }

    @Test(expected = IllegalFlowLogicException::class)
    fun `should reject with invalid parameters`() {
        FlowLogicRefFactoryImpl.verify(SchedulableKotlinFlow::class.java, "Hello Jack", 12345)
    }
}
