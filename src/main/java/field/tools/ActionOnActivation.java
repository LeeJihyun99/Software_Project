package field.tools;

/**
 * Wird von allen Feldern implementiert, welche eine Aktion haben, welche ausgeführt wird, wenn man dieses aktiviert wird
 * @author David Kulbe
 */
public interface ActionOnActivation
{
    /**
     * Was mach das Feld, wenn es aktiviert wird
     * @author David Kulbe
     */
    void actionOnActivation();
}
