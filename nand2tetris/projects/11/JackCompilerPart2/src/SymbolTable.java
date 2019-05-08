import java.util.Hashtable;

enum Kind
{
    STATIC,
    FIELD,
    ARG,
    VAR,
    NONE
}

class SymbolVariable
{
    Kind kind;
    String type;
    int index;

    SymbolVariable(Kind kind, String type, int index)
    {
        this.kind = kind;
        this.type = type;
        this.index = index;
    }
}

public class SymbolTable
{
    Hashtable<String, SymbolVariable> classScope;
    Hashtable<String, SymbolVariable> subroutineScope;

    SymbolTable()
    {
        classScope = new Hashtable<>();
        subroutineScope = new Hashtable<>();
    }

    void startSubroutine()
    {
        subroutineScope.clear();
    }

    void Define(String name, String type, Kind kind)
    {
        SymbolVariable variable = new SymbolVariable(kind,type,-1);
        variable.index = VarCount(kind);

        switch (kind)
        {
            case STATIC:
            case FIELD: classScope.put(name,variable);
                break;
            case ARG:
            case VAR: subroutineScope.put(name,variable);
                break;
            case NONE:
                break;
        }

        System.out.println("Adding a new variable to the scope");
        System.out.format("Name = %s, Type = %s, Kind = %s, Index = %d\n",name,type,kind,variable.index);
    }

    int VarCount(Kind kind)
    {
        int count = 0;

        for (SymbolVariable variable: classScope.values())
        {
            if(variable.kind == kind)
            {
                count++;
            }
        }

        for (SymbolVariable variable: subroutineScope.values())
        {
            if(variable.kind == kind)
            {
                count++;
            }
        }

        return count;
    }

    Kind KindOf(String name)
    {
        if(classScope.containsKey(name))
        {
            return classScope.get(name).kind;
        }

        if(subroutineScope.containsKey(name))
        {
            return subroutineScope.get(name).kind;
        }

        return Kind.NONE;
    }

    String TypeOf(String name)
    {
        if(classScope.containsKey(name))
        {
            return classScope.get(name).type;
        }

        if(subroutineScope.containsKey(name))
        {
            return subroutineScope.get(name).type;
        }

        return null;
    }

    int IndexOf(String name)
    {
        if(classScope.containsKey(name))
        {
            return classScope.get(name).index;
        }

        if(subroutineScope.containsKey(name))
        {
            return subroutineScope.get(name).index;
        }

        return -1;
    }
}
