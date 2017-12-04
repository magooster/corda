package net.corda.vega.plugin.customserializers

import com.opengamma.strata.market.param.CurrencyParameterSensitivities
import com.opengamma.strata.market.param.CurrencyParameterSensitivity
import net.corda.core.serialization.CordaCustomSerializer
import net.corda.core.serialization.CordaCustomSerializerProxy
import net.corda.core.serialization.SerializationCustomSerializer
import java.lang.reflect.Type

@CordaCustomSerializer
@Suppress("UNUSED")
class CurrencyParameterSensitivitiesSerializer :
        SerializationCustomSerializer<CurrencyParameterSensitivities, CurrencyParameterSensitivitiesSerializer.Proxy> {
    @CordaCustomSerializerProxy
    data class Proxy(val sensitivities: List<CurrencyParameterSensitivity>)

    override fun fromProxy(proxy: Proxy) = CurrencyParameterSensitivities.of(proxy.sensitivities)
    override fun toProxy(obj: CurrencyParameterSensitivities) = Proxy(obj.sensitivities)
}