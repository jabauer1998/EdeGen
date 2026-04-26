package ede.gen.compiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ede.stl.common.Utils;
import ede.stl.ast.Reg;
import ede.stl.ast.Element;
import ede.stl.ast.Slice;
import ede.stl.ast.Identifier;
import ede.stl.ast.Expression;
import ede.stl.ast.BlockingAssignment;
import ede.stl.ast.SystemTaskStatement;
import ede.stl.ast.TaskStatement;
import ede.stl.compiler.VerilogToJavaGen;
import ede.gen.gui.GuiGenPanel;

public class VerilogToEdeGen extends VerilogToJavaGen{
    private GuiGenPanel gen;
    private String standardOutputPane;
    private String standardInputPane;
    
    public VerilogToEdeGen(int javaVersion, GuiGenPanel gen,  String standardOutputPane, String standardInputPane){
        super(javaVersion);
        this.gen = gen;
        this.standardOutputPane = standardOutputPane;
        this.standardInputPane = standardInputPane;
    }

    protected void printStringNow(String str){
        gen.addLog(str);
    }

    protected void codeGenFieldRegScalarIdent(Reg.Scalar.Ident ident, MethodVisitor constructor, String modName, ClassVisitor moduleWriter){
        String annotationLexeme = ident.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@status")){
                FieldVisitor fv = moduleWriter.visitField(Opcodes.ACC_PRIVATE, ident.declarationIdentifier, "Lede/stl/values/EdeStatVal;", null, null);
                if (fv != null) {
                    addField(ident.declarationIdentifier, "Lede/stl/values/EdeStatVal;");
                    constructor.visitVarInsn(Opcodes.ALOAD, 0);
                    constructor.visitTypeInsn(Opcodes.NEW, "ede/stl/values/EdeStatVal");
                    constructor.visitInsn(Opcodes.DUP);
                    constructor.visitLdcInsn(ident.declarationIdentifier);
                    constructor.visitVarInsn(Opcodes.ALOAD, 1);
                    constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "ede/stl/values/EdeStatVal", "<init>", "(Ljava/lang/String;Lede/stl/gui/GuiEde;)V", false);
                    constructor.visitFieldInsn(Opcodes.PUTFIELD, modName, ident.declarationIdentifier, "Lede/stl/values/EdeStatVal;");
                    fv.visitEnd();
                }
                return;
            }
        }
        super.codeGenFieldRegScalarIdent(ident, constructor, modName, moduleWriter);
    }

    protected void codeGenFieldRegVectorIdent(Reg.Vector.Ident ident, MethodVisitor constructor, String modName, ClassVisitor moduleWriter) throws Exception {
        String annotationLexeme = ident.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@register")){
                FieldVisitor fv = moduleWriter.visitField(Opcodes.ACC_PRIVATE, ident.declarationIdentifier, "Lede/stl/values/EdeRegVal;", null, null);
                if (fv != null) {
                    addField(ident.declarationIdentifier, "Lede/stl/values/EdeRegVal;");
                    constructor.visitVarInsn(Opcodes.ALOAD, 0);
                    constructor.visitTypeInsn(Opcodes.NEW, "ede/stl/values/EdeRegVal");
                    constructor.visitInsn(Opcodes.DUP);
                    constructor.visitLdcInsn(ident.declarationIdentifier);
                    constructor.visitVarInsn(Opcodes.ALOAD, 1);
                    constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "ede/stl/values/EdeRegVal", "<init>", "(Ljava/lang/String;Lede/stl/gui/GuiEde;)V", false);
                    constructor.visitFieldInsn(Opcodes.PUTFIELD, modName, ident.declarationIdentifier, "Lede/stl/values/EdeRegVal;");
                    fv.visitEnd();
                }
                return;
            }
        }
        super.codeGenFieldRegVectorIdent(ident, constructor, modName, moduleWriter);
    }

    protected void codeGenFieldRegVectorArray(Reg.Vector.Array array, MethodVisitor constructor, String modName, ClassVisitor moduleWriter) throws Exception {
        String annotationLexeme = array.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@memory")){
                FieldVisitor fv = moduleWriter.visitField(Opcodes.ACC_PRIVATE, array.declarationIdentifier, "Lede/stl/values/EdeMemVal;", null, null);
                if (fv != null) {
                    addField(array.declarationIdentifier, "Lede/stl/values/EdeMemVal;");
                    constructor.visitVarInsn(Opcodes.ALOAD, 0);
                    constructor.visitTypeInsn(Opcodes.NEW, "ede/stl/values/EdeMemVal");
                    constructor.visitInsn(Opcodes.DUP);
                    constructor.visitVarInsn(Opcodes.ALOAD, 1);
                    constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "ede/stl/values/EdeMemVal", "<init>", "(Lede/stl/gui/GuiEde;)V", false);
                    constructor.visitFieldInsn(Opcodes.PUTFIELD, modName, array.declarationIdentifier, "Lede/stl/values/EdeMemVal;");
                    fv.visitEnd();
                }
                return;
            }
        }
        super.codeGenFieldRegVectorArray(array, constructor, modName, moduleWriter);
    }

    protected void codeGenShallowTaskCall(TaskStatement taskCall, MethodVisitor mv, String modName, ClassVisitor moduleWriter) throws Exception{
        String annotationLexeme = taskCall.annotationLexeme;
        if(annotationLexeme != null && annotationLexeme.toLowerCase().equals("@breakpoint")){
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "isDebuggerEnabled", "()Z", false);
            Label l0 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "waitForStep", "()V", false);
            mv.visitLabel(l0);
        }
        super.codeGenShallowTaskCall(taskCall, mv, modName, moduleWriter);
    }

    protected void codeGenShallowSystemTaskCall(SystemTaskStatement taskCall, MethodVisitor mv, String modName, ClassVisitor moduleWriter) throws Exception {
        if(taskCall.taskName.equals("display")){
            if(taskCall.argumentList.size() >= 2){
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitLdcInsn("StandardOutput");
                codeGenShallowExpression(taskCall.argumentList.get(0), mv, modName, moduleWriter);
                mv.visitIntInsn(Opcodes.BIPUSH, taskCall.argumentList.size() - 1);
                mv.visitTypeInsn(Opcodes.ANEWARRAY, "ede/stl/values/Value");
                for(int i = 1; i < taskCall.argumentList.size(); i++){
                        mv.visitInsn(Opcodes.DUP);
                        mv.visitIntInsn(Opcodes.BIPUSH, i - 1);
                        codeGenShallowExpression(taskCall.argumentList.get(i), mv, modName, moduleWriter);
                        mv.visitInsn(Opcodes.AASTORE); 
                }
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ede/stl/common/Utils", "formatString", "(Lede/stl/values/Value;[Lede/stl/values/Value;)Ljava/lang/String;", false);
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ede/stl/common/Utils", "display", "(Ljava/lang/String;)V", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "appendIoText", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                return;
            } else if(taskCall.argumentList.size() == 1){
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitLdcInsn("StandardOutput");
                codeGenShallowExpression(taskCall.argumentList.get(0), mv, modName, moduleWriter);
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ede/stl/values/Value", "toString", "()Ljava/lang/String;", true);
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ede/stl/common/Utils", "display", "(Ljava/lang/String;)V", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "appendIoText", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                return;
            }
        }
        super.codeGenShallowSystemTaskCall(taskCall, mv, modName, moduleWriter);
    }

    private static boolean isEdeType(String type){
        return (type.equals("Lede/stl/values/EdeStatVal;") || type.equals("Lede/stl/values/EdeRegVal;") || type.equals("Lede/stl/values/EdeMemVal;"));
    }

    protected void codeGenShallowExpression(Expression exp, MethodVisitor method, String moduleName, ClassVisitor module) throws Exception {
        if(exp instanceof Identifier){
            Identifier ident = (Identifier)exp;
            String type = getTypeFromFieldScope(ident.labelIdentifier);
            if(type != null && isEdeType(type)){
                if(fieldInScope(ident.labelIdentifier)){
                    method.visitVarInsn(Opcodes.ALOAD, 0);
                    method.visitFieldInsn(Opcodes.GETFIELD, moduleName, ident.labelIdentifier, type);
                    method.visitMethodInsn(Opcodes.INVOKESTATIC, "ede/stl/common/Utils", "toLongValue", "(Lede/stl/values/Value;)Lede/stl/values/Value;", false);
                    return;
                } else {
                    Utils.errorAndExit("Error identifier " + ident.labelIdentifier + " not found in scope!!!");
                }
            }
        } else if(exp instanceof Slice){
            Slice slice = (Slice)exp;
            String type = getTypeFromFieldScope(slice.labelIdentifier);
            if(type != null && isEdeType(type)){
                if(fieldInScope(slice.labelIdentifier)){
                    if(type.equals("Lede/stl/values/EdeRegVal;")){
                        method.visitTypeInsn(Opcodes.NEW, "ede/stl/values/LongVal");
                        method.visitInsn(Opcodes.DUP);
                        method.visitVarInsn(Opcodes.ALOAD, 0);
                        method.visitFieldInsn(Opcodes.GETFIELD, moduleName, slice.labelIdentifier, type);
                        codeGenShallowExpression(slice.index1, method, moduleName, module);
                        method.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ede/stl/values/Value", "intValue", "()I", true);
                        codeGenShallowExpression(slice.index2, method, moduleName, module);
                        method.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ede/stl/values/Value", "intValue", "()I", true);
                        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/values/EdeRegVal", "getBitsInRange", "(II)J", false);
                        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "ede/stl/values/LongVal", "<init>", "(J)V", false);
                        return;
                    }
                } else {
                    Utils.errorAndExit("Error variable " + slice.labelIdentifier + " is not found!!!\nin module " + moduleName + "\nat position " + slice.position.toString());
                }
            }
        } else if(exp instanceof Element){
            Element elem = (Element)exp;
            String type = getTypeFromFieldScope(elem.labelIdentifier);
            if(type != null && isEdeType(type)){
                if(fieldInScope(elem.labelIdentifier)){
                    if(type.equals("Lede/stl/values/EdeRegVal;")){
                        method.visitTypeInsn(Opcodes.NEW, "ede/stl/values/LongVal");
                        method.visitInsn(Opcodes.DUP);
                        method.visitVarInsn(Opcodes.ALOAD, 0);
                        method.visitFieldInsn(Opcodes.GETFIELD, moduleName, elem.labelIdentifier, type);
                        codeGenShallowExpression(elem.index1, method, moduleName, module);
                        method.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ede/stl/values/Value", "intValue", "()I", true);
                        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/values/EdeRegVal", "getBitAtIndex", "(I)J", false);
                        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "ede/stl/values/LongVal", "<init>", "(J)V", false);
                        return;
                    } else if(type.equals("Lede/stl/values/EdeMemVal;")){
                        method.visitTypeInsn(Opcodes.NEW, "ede/stl/values/LongVal");
                        method.visitInsn(Opcodes.DUP);
                        method.visitVarInsn(Opcodes.ALOAD, 0);
                        method.visitFieldInsn(Opcodes.GETFIELD, moduleName, elem.labelIdentifier, type);
                        codeGenShallowExpression(elem.index1, method, moduleName, module);
                        method.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ede/stl/values/Value", "intValue", "()I", true);
                        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/values/EdeMemVal", "elemAtIndex", "(I)J", false);
                        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "ede/stl/values/LongVal", "<init>", "(J)V", false);
                        return;
                    }
                } else {
                    Utils.errorAndExit("Error cant find elem from identifier " + elem.labelIdentifier);
                }
            }
        }
        super.codeGenShallowExpression(exp, method, moduleName, module);
    }

    protected void codeGenShallowBlockingAssign(BlockingAssignment assign, String funcName, MethodVisitor mv, String modName, ClassVisitor moduleWriter) throws Exception {
        if(assign.leftHandSide instanceof Element){
            Element leftHandSide = (Element)assign.leftHandSide;
            String type = getTypeFromFieldScope(leftHandSide.labelIdentifier);
            if(type != null && isEdeType(type)){
                 if(this.fieldInScope(leftHandSide.labelIdentifier)){
                     mv.visitVarInsn(Opcodes.ALOAD, 0);
                     mv.visitFieldInsn(Opcodes.GETFIELD, modName, leftHandSide.labelIdentifier, type);
                     codeGenShallowExpression(leftHandSide.index1, mv, modName, moduleWriter);
                     codeGenShallowExpression(assign.rightHandSide, mv, modName, moduleWriter);
                     mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ede/stl/common/Utils", "shallowAssignElemEde", "(Lede/stl/values/Value;Lede/stl/values/Value;Lede/stl/values/Value;)V", false);
                     return;
                 } else {
                     Utils.errorAndExit("Variable " + leftHandSide.labelIdentifier + " does not exist in the current scope", leftHandSide.position);
                 }
            }
        } else if(assign.leftHandSide instanceof Slice){
            Slice leftHandSide = (Slice)assign.leftHandSide;
            String type = getTypeFromFieldScope(leftHandSide.labelIdentifier);
            if(type != null && isEdeType(type)){
                if(this.fieldInScope(leftHandSide.labelIdentifier)){
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, modName, leftHandSide.labelIdentifier, type);
                    codeGenShallowExpression(leftHandSide.index1, mv, modName, moduleWriter);
                    codeGenShallowExpression(leftHandSide.index2, mv, modName, moduleWriter);
                    codeGenShallowExpression(assign.rightHandSide, mv, modName, moduleWriter);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                       "ede/stl/common/Utils",
                                       "shallowAssignSliceEde",
                                       "(Lede/stl/values/Value;Lede/stl/values/Value;Lede/stl/values/Value;Lede/stl/values/Value;)V",
                                       false);
                    return;
                } else {
                    Utils.errorAndExit("Variable " + leftHandSide.labelIdentifier + " does not exist in the current scope", leftHandSide.position);
                }
            }
        } else if(assign.leftHandSide instanceof Identifier){
            Identifier leftHandSide = (Identifier)assign.leftHandSide;
            String type = getTypeFromFieldScope(leftHandSide.labelIdentifier);
            if(type != null && isEdeType(type)){
                if(fieldInScope(leftHandSide.labelIdentifier)){
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, modName, leftHandSide.labelIdentifier, type);
                    codeGenShallowExpression(assign.rightHandSide, mv, modName, moduleWriter);
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "ede/stl/values/Value", "setValue", "(Lede/stl/values/Value;)V", true);
                    return;
                } else {
                    Utils.errorAndExit("Variable " + leftHandSide.labelIdentifier + " does not exist in the current scope", leftHandSide.position);
                }
            }
        }
        super.codeGenShallowBlockingAssign(assign, funcName, mv, modName, moduleWriter);
    }
}
