package fr.insee.arc.utils.sqlengine.model;

public interface IType
{

    IToken name();

    int hashCode();

    boolean equals(Object obj);

    String toString();
}
