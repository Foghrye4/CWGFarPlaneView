package cwgfarplaneview.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class GenLayerClassVisitor extends ClassVisitor {

	private static final String FIELD_NAME = "intCache";
	private boolean firstFieldVisited = false;

	public GenLayerClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (!firstFieldVisited) {
			super.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, FIELD_NAME,
					Type.getDescriptor(NonStaticIntCache.class), null, null);
			firstFieldVisited = true;
		}
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("<init>")) {
			return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
				
				@Override
				public void visitInsn(int opcode) {
					if (opcode == Opcodes.RETURN) {
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitTypeInsn(Opcodes.NEW, Type.getInternalName(NonStaticIntCache.class));
						super.visitInsn(Opcodes.DUP);
						super.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(NonStaticIntCache.class), "<init>", "()V", false);
						super.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/world/gen/layer/GenLayer", FIELD_NAME, Type.getDescriptor(NonStaticIntCache.class));
						super.visitInsn(Opcodes.RETURN);
					} else {
						super.visitInsn(opcode);
					}
				}

			};
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	

}
