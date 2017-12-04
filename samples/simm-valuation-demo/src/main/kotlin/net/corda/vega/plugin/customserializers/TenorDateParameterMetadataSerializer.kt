package net.corda.vega.plugin.customserializers

import com.opengamma.strata.basics.date.Tenor
import com.opengamma.strata.market.param.TenorDateParameterMetadata
import net.corda.core.serialization.*
import java.lang.reflect.Type
import java.time.LocalDate

@CordaCustomSerializer
@Suppress("UNUSED")
class TenorDateParameterMetadataSerializer :
        SerializationCustomSerializer<TenorDateParameterMetadata, TenorDateParameterMetadataSerializer.Proxy> {
    @CordaCustomSerializerProxy
    data class Proxy(val tenor: Tenor, val date: LocalDate, val identifier: Tenor, val label: String)

    override fun toProxy(obj: TenorDateParameterMetadata) = Proxy(obj.tenor, obj.date, obj.identifier, obj.label)
    override fun fromProxy(proxy: Proxy) = TenorDateParameterMetadata.of(proxy.date, proxy.tenor, proxy.label)
}
