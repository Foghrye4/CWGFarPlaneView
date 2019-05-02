package cwgfarplaneview.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class GenLayerClassVisitor extends ClassVisitor {

	static final String GEN_LAYER_CLASS_NAME = "net/minecraft/world/gen/layer/GenLayer";
	static final String INT_CACHE_FIELD_NAME = "intCache";
	static final String THREAD_LOCAL_CHUNK_SEED_FIELD_NAME = "threadLocalChunkSeed";
	private boolean firstFieldVisited = false;

	public GenLayerClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (!firstFieldVisited) {
			super.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, INT_CACHE_FIELD_NAME,
					Type.getDescriptor(NonStaticIntCache.class), null, null);
			super.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, THREAD_LOCAL_CHUNK_SEED_FIELD_NAME,
					Type.getDescriptor(ThreadLocalLong.class), null, null);
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
						super.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(NonStaticIntCache.class),
								"<init>", "()V", false);
						super.visitFieldInsn(Opcodes.PUTFIELD, GEN_LAYER_CLASS_NAME, INT_CACHE_FIELD_NAME,
								Type.getDescriptor(NonStaticIntCache.class));
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ThreadLocalLong.class));
						super.visitInsn(Opcodes.DUP);
						super.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ThreadLocalLong.class),
								"<init>", "()V", false);
						super.visitFieldInsn(Opcodes.PUTFIELD, GEN_LAYER_CLASS_NAME, THREAD_LOCAL_CHUNK_SEED_FIELD_NAME,
								Type.getDescriptor(ThreadLocalLong.class));
						super.visitInsn(Opcodes.RETURN);
					} else {
						super.visitInsn(opcode);
					}
				}
			};
		} else {
			return new ChunkSeedRedirectMethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions));
		}
	}
}
