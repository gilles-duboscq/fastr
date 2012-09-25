package r.builtins;

import java.util.*;

import r.*;
import r.data.*;
import r.errors.*;
import r.nodes.*;
import r.nodes.tools.*;
import r.nodes.truffle.*;

public class Primitives {

    private static Map<RSymbol, PrimitiveEntry> map;
    static {
        map = new HashMap<>();
        add(":", 2, 2, Colon.FACTORY);
        add("c", 0, -1, Combine.FACTORY);
        add("double", 0, 1, ArrayConstructor.DOUBLE_FACTORY);
        add("integer", 0, 1, ArrayConstructor.INT_FACTORY);
        add("length", 1, 1, Length.FACTORY);
        add("logical", 0, 1, ArrayConstructor.LOGICAL_FACTORY);
        add("max", 0, -1, Max.FACTORY);
        add("return", 0, -1, Return.FACTORY);
    }

    public static CallFactory getCallFactory(final RSymbol name, final RFunction enclosing) {
        final PrimitiveEntry pe = Primitives.get(name, enclosing);
        if (pe == null) {
            return null;
        }
        return new CallFactory() {

            @Override
            public RNode create(ASTNode call, RSymbol[] names, RNode[] exprs) {
                int minArgs = pe.getMinArgs();
                int maxArgs = pe.getMaxArgs();

                if (minArgs != -1 && exprs.length < minArgs || maxArgs != -1 && exprs.length > maxArgs) {
                    throw RError.getGenericError(call, "Wrong number of arguments for call to BuiltIn (" + PrettyPrinter.prettyPrint(call) + ")");
                }

                return pe.factory.create(call, names, exprs);
            }
        };
    }

    public static PrimitiveEntry get(RSymbol name, RFunction fun) {
        PrimitiveEntry pe = get(name);
        if (pe != null && fun != null && fun.isInWriteSet(name)) {
            Utils.nyi(); // TODO case were a primitive is shadowed by a local symbol
        }
        return pe;
    }

    public static PrimitiveEntry get(RSymbol name) {
        return map.get(name);
    }

    private static void add(String name, int minArgs, int maxArgs, CallFactory body) {
        add(name, minArgs, maxArgs, body, PrimitiveEntry.PREFIX);
    }

    private static void add(String name, int minArgs, int maxArgs, CallFactory body, int pp) {
        RSymbol sym = RSymbol.getSymbol(name);
        assert Utils.check(!map.containsKey(sym));
        map.put(sym, new PrimitiveEntry(sym, minArgs, maxArgs, body, pp));
    }
}