package gurobiModel;


import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martina Cernekova
 */
public class Priklad {

    public void optimalizuj() {
        try {
            GRBEnv env = new GRBEnv("priklad.log");
            GRBModel model = new GRBModel(env);
            
            GRBVar x = model.addVar(0, 1, 0, GRB.BINARY, "x");
            GRBVar y = model.addVar(0, 1, 0, GRB.BINARY, "y");
            GRBVar z = model.addVar(0, 1, 0, GRB.BINARY, "z");
            
            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerm(1, x);
            expr.addTerm(1, y);
            expr.addTerm(2, z);
            model.setObjective(expr, GRB.MAXIMIZE);
            
            GRBLinExpr con1 = new GRBLinExpr();
            con1.addTerm(1, x);
            con1.addTerm(2, y);
            con1.addTerm(3, z);
            model.addConstr(con1, GRB.LESS_EQUAL, 4, "c0");
            
            GRBLinExpr con2 = new GRBLinExpr();
            con2.addTerm(1, x);
            con2.addTerm(1, y);
            model.addConstr(con2, GRB.GREATER_EQUAL, 1, "c1");
            
            model.optimize();
            System.out.println(x.get(GRB.StringAttr.VarName) + " " + x.get(GRB.DoubleAttr.X));
            System.out.println(y.get(GRB.StringAttr.VarName) + " " + y.get(GRB.DoubleAttr.X));
            System.out.println(z.get(GRB.StringAttr.VarName) + " " + z.get(GRB.DoubleAttr.X));
            
            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
            
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(Priklad.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
