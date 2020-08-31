package at.rechnerherz.aoprofiling

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "at.rechnerherz.aoprofiling")
class ProfilingProperties {

    enum class Mode {
        PLAIN, TREE, VERBOSE
    }

    /** Whether to enable aspect oriented profiling. */
    var enabled: Boolean = false

    /** Display mode: TREE adds line drawings to the call tree, VERBOSE prints to multiple lines, PLAIN does neither. */
    var mode: Mode = Mode.TREE

    /** Strings will be truncated to at most this number of characters, unless mode is VERBOSE. */
    var truncate: Int = 100

    /** Comma-separated list of target.method names to ignore. */
    var ignore: String = ""

    /**
     * Order of the ProfilingAspect. Should be lower than the transaction advisor order.
     */
    var profilingAspectOrder = -1

    /**
     * Order of the ProfilingSummaryAspect. Should be lower than the profilingAspectOrder.
     */
    var profilingSummaryAspectOrder = -2
}
