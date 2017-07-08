package cn.patterncat.metrics.utils;

/**
 * Utility class for OS-specific behaviour
 */
public class OSHelper {

    /**
     * The current OS is Linux, BSD, Darwin, AIX, Solaris (etc.)
     */
    public static final boolean IS_UNIX;

    /**
     * The current OS is Windows of any flavour (implies IS_UNIX is false)
     */

    public static final boolean IS_WINDOWS;

    /**
     * The current OS is Linux (implies IS_UNIX is also true)
     */
    public static final boolean IS_LINUX;

    /**
     * The current OS is MacOS (implies IS_UNIX is also true)
     */
    public static final boolean IS_MACOS;

    static {
        final String osname = System.getProperty("os.name").toLowerCase();

        IS_UNIX = !osname.contains("win");
        IS_WINDOWS = !IS_UNIX;

        IS_MACOS = osname.contains("os x") || osname.contains("mac") || osname.contains("darwin");
        IS_LINUX = osname.contains("linux");
    }
}
