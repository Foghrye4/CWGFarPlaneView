package cwgfarplaneview.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class GenLayerChildClassVisitor extends ClassVisitor {

	private static final String FIELD_NAME = "intCache";

	public GenLayerChildClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				if (opcode == Opcodes.INVOKESTATIC && owner.equals("bdo")) {
					super.visitVarInsn(Opcodes.ALOAD, 0);
					super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/world/gen/layer/GenLayer", FIELD_NAME,
							Type.getDescriptor(NonStaticIntCache.class));
					super.visitInsn(Opcodes.SWAP);
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(NonStaticIntCache.class),
							"getIntCache", "(I)[I", false);
				} else {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
				}
			}
		};
	}

}
