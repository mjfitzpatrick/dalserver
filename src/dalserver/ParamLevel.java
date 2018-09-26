/*
 * ParamLevel.java
 * $ID*
 */

package dalserver;

/**
 * The level to which a parameter belongs, e.g., the scope or type of
 * standard.
 */
public enum ParamLevel {
    /**
     * A parameter defined by the core standard or protocol.
     */
    CORE,

    /**
     * A parameter defined by some extension to the standard, which is
     * known to the service.  If a service defines new query parameters
     * which can be used by a client they should be of this type.
     */
    EXTENSION,

    /**
     * A parameter not known to the service, which was passed in
     * by the client application.
     */
    CLIENT,

    /**
     * An internal parameter used for communication within the service
     * implementation itself.
     */
    SERVICE
}
