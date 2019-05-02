package cwgfarplaneview.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static cwgfarplaneview.core.GenLayerClassVisitor.*;

public class ChunkSeedRedirectMethodVisitor extends MethodVisitor {

	public ChunkSeedRedirectMethodVisitor(int api, MethodVisitor mv) {
		super(api, mv);
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if (owner.equals("bdq") && name.equals("d")) {
			if (opcode == Opcodes.GETFIELD) {
				super.visitFieldInsn(Opcodes.GETFIELD, GEN_LAYER_CLASS_NAME,
						THREAD_LOCAL_CHUNK_SEED_FIELD_NAME, Type.getDescriptor(ThreadLocalLong.class));
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ThreadLocalLong.class),
						"getValue", "()J", false);
			} else if (opcode == Opcodes.PUTFIELD) {
				super.visitVarInsn(Opcodes.LSTORE, 22);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, GEN_LAYER_CLASS_NAME,
						THREAD_LOCAL_CHUNK_SEED_FIELD_NAME, Type.getDescriptor(ThreadLocalLong.class));
				super.visitVarInsn(Opcodes.LLOAD, 22);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ThreadLocalLong.class),
						"setValue", "(J)V", false);
			} else {
				super.visitFieldInsn(opcode, owner, name, desc);
			}
		} else {
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}


}
