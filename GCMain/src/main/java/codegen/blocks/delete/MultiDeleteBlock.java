package codegen.blocks.delete;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.modify.ModifyProxyBlock;
import soot.Body;
import soot.SootMethod;
import soot.util.Numberable;

import java.util.List;

public class MultiDeleteBlock extends BasicBlock {
    private List<Numberable> numberables;

    public MultiDeleteBlock(List<Numberable> numberables) {
        this.numberables = numberables;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        boolean success = false;

        for (Numberable numberable : numberables) {
            ModifyProxyBlock block = new ModifyProxyBlock(new DeleteBlock(), numberable);
            block.setInserationTarget(getInserationTarget());
            success = success || block.insertBlock(clazz, sootMethod);
        }
        return success;
    }
}
