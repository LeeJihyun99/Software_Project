package field.tools;

import field.reducedFields.ReducedField;

/**
 * Interface, welches von Feldern verlangt die reduzierte Version zurückzugeben
 */
public interface ReduceField
{
    ReducedField reduce();
}
