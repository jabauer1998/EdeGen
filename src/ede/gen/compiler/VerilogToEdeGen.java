package ede.gen.compiler;

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
import ede.stl.ast.BlockingAssignment;
import ede.stl.ast.SystemTaskStatement;
import ede.stl.ast.TaskStatement;
import ede.stl.compiler.VerilogToJavaGen;

public class VerilogToEdeGen extends VerilogToJavaGen{
    public VerilogToEdeGen(int javaVersion, EdeGenerator gen,  String standardOutputPane, String standardInputPane){
        super(javaVersion);
    }

    protected void printString(String str){
        
    }

    protected void codeGenFieldRegScalarIdent(Reg.Scalar.Ident ident, MethodVisitor constructor, String modName, ClassWriter moduleWriter){
        String annotationLexeme = ident.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@status")){
                FieldVisitor fv = moduleWriter.visitField(Opcodes.ACC_PRIVATE, ident.declarationIdentifier, "Lede/stl/values/EdeStatVal;", null, null);
                if (fv != null) {
                    fv.visitEnd();
                }
                return;
            }
        }
        super.codeGenFieldRegScalarIdent(ident, constructor, modName, moduleWriter);
    }

    protected void codeGenFieldRegVectorIdent(Reg.Vector.Ident ident, MethodVisitor constructor, String modName, ClassWriter moduleWriter) throws Exception {
        String annotationLexeme = ident.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@register")){
                FieldVisitor fv = moduleWriter.visitField(Opcodes.ACC_PRIVATE, ident.declarationIdentifier, "Lede/stl/values/EdeRegVal;", null, null);
                if (fv != null) {
                    fv.visitEnd();
                }
                return;
            }
        }
        super.codeGenFieldRegVectorIdent(ident, constructor, modName, moduleWriter);
    }

    protected void codeGenFieldRegVectorArray(Reg.Vector.Array array, MethodVisitor constructor, String modName, ClassWriter moduleWriter) throws Exception {
        String annotationLexeme = array.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@memory")){
                FieldVisitor fv = moduleWriter.visitField(Opcodes.ACC_PRIVATE, array.declarationIdentifier, "Lede/stl/values/EdeMemVal;", null, null);
                if (fv != null) {
                    fv.visitEnd();
                }
                return;
            }
        }
        super.codeGenFieldRegVectorArray(array, constructor, modName, moduleWriter);
    }

    protected void codeGenShallowTaskCall(TaskStatement taskCall, MethodVisitor mv, String modName, ClassWriter moduleWriter) throws Exception{
        String annotationLexeme = taskCall.annotationLexeme;
        if(annotationLexeme != null){
            if(annotationLexeme.toLowerCase().equals("@breakpoint")){
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, modName, "guiInstance", "Lede/stl/gui/GuiEde;");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "isDebuggerEnabled", "()Z", false);
                Label l0 = new Label();
                mv.visitJumpInsn(Opcodes.IFEQ, l0);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, modName, "guiInstance", "Lede/stl/gui/GuiEde;");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "waitForStep", "()V", false);
                mv.visitLabel(l0);
            }
        } else {
            super.codeGenShallowTaskCall(taskCall, mv, modName, moduleWriter);
        }
    }

    protected void codeGenShallowSystemTaskCall(SystemTaskStatement taskCall, MethodVisitor mv, String modName, ClassWriter moduleWriter) throws Exception {
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
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ede/stl/gui/GuiEde", "appendIoText", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                return;
            }
        }
        super.codeGenShallowSystemTaskCall(taskCall, mv, modName, moduleWriter);
    }

    protected void codeGenShallowBlockingAssign(BlockingAssignment assign, String funcName, MethodVisitor mv, String modName, ClassWriter moduleWriter) throws Exception {
        codeGenShallowExpression(assign.rightHandSide, mv, modName, moduleWriter);
        if(assign.leftHandSide instanceof Element){
            Element leftHandSide = (Element)assign.leftHandSide;
            codeGenShallowExpression(leftHandSide.index1, mv, modName, moduleWriter);
            
            if(this.localInScope(leftHandSide.labelIdentifier)){
                 int ptr = this.getFromScope(leftHandSide.labelIdentifier);
                 mv.visitVarInsn(Opcodes.ALOAD, ptr);
            } else if(this.fieldInScope(leftHandSide.labelIdentifier)){
                 mv.visitVarInsn(Opcodes.ALOAD, 0);
                 mv.visitFieldInsn(Opcodes.GETFIELD, modName, leftHandSide.labelIdentifier, "Lede/stl/values/EdeRegVal;");
            } else {
                 Utils.errorAndExit("Variable " + leftHandSide.labelIdentifier + " does not exist in the current scope", leftHandSide.position);
            }

            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ede/stl/common/Utils", "shallowAssignElemEde", "(Lede/stl/values/Value;Lede/stl/values/Value;Lede/stl/values/Value;)V", false);
        } else if(assign.leftHandSide instanceof Slice){
            Slice leftHandSide = (Slice)assign.leftHandSide;

            codeGenShallowExpression(leftHandSide.index1, mv, modName, moduleWriter);
            codeGenShallowExpression(leftHandSide.index2, mv, modName, moduleWriter);

            if(this.localInScope(leftHandSide.labelIdentifier)){
                 int ptr = this.getFromScope(leftHandSide.labelIdentifier);
                 mv.visitVarInsn(Opcodes.ALOAD, ptr);
            } else if(this.fieldInScope(leftHandSide.labelIdentifier)){
                 mv.visitVarInsn(Opcodes.ALOAD, 0);
                 mv.visitFieldInsn(Opcodes.GETFIELD, modName, leftHandSide.labelIdentifier, "Lede/stl/values/EdeRegVal;");
            } else {
                 Utils.errorAndExit("Variable " + leftHandSide.labelIdentifier + " does not exist in the current scope", leftHandSide.position);
            }

            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                 "ede/stl/common/Utils",
                 "shallowAssignSliceEde",
                 "(Lede/stl/values/Value;Lede/stl/values/Value;Lede/stl/values/Value;Lede/stl/values/Value;)V",
                 false);
        } else if(assign.leftHandSide instanceof Identifier){
            Identifier leftHandSide = (Identifier)assign.leftHandSide;
            if(leftHandSide.labelIdentifier.equals(funcName)){
                mv.visitInsn(Opcodes.RETURN);
            } else {
                if(localInScope(leftHandSide.labelIdentifier)){
                     int ptr = this.getFromScope(leftHandSide.labelIdentifier);
                     mv.visitVarInsn(Opcodes.ASTORE, ptr);
                } else if(fieldInScope(leftHandSide.labelIdentifier)){
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, modName, leftHandSide.labelIdentifier, "Lede/stl/values/Value;");
                } else {
                    Utils.errorAndExit("Variable " + leftHandSide.labelIdentifier + " does not exist in the current scope", leftHandSide.position);
                }
            }
        }
    }
}
