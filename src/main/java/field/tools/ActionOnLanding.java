package field.tools;

/**
 * Wird von allen Feldern implementiert, welche eine Aktion haben, welche ausgeführt wird, wenn man auf diesem Feld landet
 * @author David Kulbe
 */
public interface ActionOnLanding
{
    /**
     * Was mach das Feld, wenn die Bewegung darauf endet
     * @author David Kulbe
     */
    void actionOnLanding();
}
