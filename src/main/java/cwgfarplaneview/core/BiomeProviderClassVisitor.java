package cwgfarplaneview.core;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BiomeProviderClassVisitor extends ClassVisitor {
	private final static Set<String> allDescriptors = new HashSet<String>();
	static {
		allDescriptors.add("(Let;Lanh;)Lanh;");
		allDescriptors.add("([Lanh;IIII)[Lanh;");
		allDescriptors.add("([Lanh;IIIIZ)[Lanh;");
		allDescriptors.add("(IIILjava/util/List;)Z");
		allDescriptors.add("(IIILjava/util/List;Ljava/util/Random;)Let;");
	}

	public BiomeProviderClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("a") && allDescriptors.contains(desc) || name.equals("b") && desc.equals("()V")) {
			return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {

				@Override
				public void visitCode() {
					super.visitFieldInsn(Opcodes.GETSTATIC, "cwgfarplaneview/core/Lock", "instance",
							"Lcwgfarplaneview/core/Lock;");
					super.visitInsn(Opcodes.DUP);
					super.visitVarInsn(Opcodes.ASTORE, 20);
					super.visitInsn(Opcodes.MONITORENTER);
					super.visitCode();
				}

				@Override
				public void visitInsn(int opcode) {
					if (opcode == Opcodes.ARETURN) {
						super.visitVarInsn(Opcodes.ASTORE, 21);
						super.visitVarInsn(Opcodes.ALOAD, 20);
						super.visitInsn(Opcodes.MONITOREXIT);
						super.visitVarInsn(Opcodes.ALOAD, 21);
						super.visitInsn(Opcodes.ARETURN);
					} else if (opcode == Opcodes.RETURN) {
						super.visitVarInsn(Opcodes.ALOAD, 20);
						super.visitInsn(Opcodes.MONITOREXIT);
						super.visitInsn(Opcodes.RETURN);
					} else {
						super.visitInsn(opcode);
					}
				}
			};
		}
//		System.out.print("\n Visiting name:"+name);
//		System.out.println(" \n Visiting descriptor:"+desc);
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

}
