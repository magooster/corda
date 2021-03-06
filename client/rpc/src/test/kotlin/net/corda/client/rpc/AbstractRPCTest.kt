package net.corda.client.rpc

import net.corda.client.rpc.internal.RPCClientConfiguration
import net.corda.core.internal.concurrent.flatMap
import net.corda.core.internal.concurrent.map
import net.corda.core.messaging.RPCOps
import net.corda.node.services.messaging.RPCServerConfiguration
import net.corda.nodeapi.internal.config.User
import net.corda.testing.SerializationEnvironmentRule
import net.corda.testing.internal.RPCDriverExposedDSLInterface
import net.corda.testing.internal.rpcTestUser
import net.corda.testing.internal.startInVmRpcClient
import net.corda.testing.internal.startRpcClient
import org.apache.activemq.artemis.api.core.client.ClientSession
import org.junit.Rule
import org.junit.runners.Parameterized

open class AbstractRPCTest {
    @Rule
    @JvmField
    val testSerialization = SerializationEnvironmentRule(true)

    enum class RPCTestMode {
        InVm,
        Netty
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Mode = {0}")
        fun defaultModes() = modes(RPCTestMode.InVm, RPCTestMode.Netty)

        fun modes(vararg modes: RPCTestMode) = listOf(*modes).map { arrayOf(it) }
    }

    @Parameterized.Parameter
    lateinit var mode: RPCTestMode

    data class TestProxy<out I : RPCOps>(
            val ops: I,
            val createSession: () -> ClientSession
    )

    inline fun <reified I : RPCOps> RPCDriverExposedDSLInterface.testProxy(
            ops: I,
            rpcUser: User = rpcTestUser,
            clientConfiguration: RPCClientConfiguration = RPCClientConfiguration.default,
            serverConfiguration: RPCServerConfiguration = RPCServerConfiguration.default
    ): TestProxy<I> {
        return when (mode) {
            RPCTestMode.InVm ->
                startInVmRpcServer(ops = ops, rpcUser = rpcUser, configuration = serverConfiguration).flatMap {
                    startInVmRpcClient<I>(rpcUser.username, rpcUser.password, clientConfiguration).map {
                        TestProxy(it, { startInVmArtemisSession(rpcUser.username, rpcUser.password) })
                    }
                }
            RPCTestMode.Netty ->
                startRpcServer(ops = ops, rpcUser = rpcUser, configuration = serverConfiguration).flatMap { server ->
                    startRpcClient<I>(server.broker.hostAndPort!!, rpcUser.username, rpcUser.password, clientConfiguration).map {
                        TestProxy(it, { startArtemisSession(server.broker.hostAndPort!!, rpcUser.username, rpcUser.password) })
                    }
                }
        }.get()
    }
}
