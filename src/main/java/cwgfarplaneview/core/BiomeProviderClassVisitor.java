package cwgfarplaneview.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class BiomeProviderClassVisitor extends ClassVisitor{
	private final static String getBiomeMethod = "a";
	private final static String getBiomeMethodDescriptor = "(Let;Lanh;)Lanh;";

	public BiomeProviderClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature,
			String[] exceptions) {
		if (name.equals(getBiomeMethod) && desc.equals(getBiomeMethodDescriptor)) {
			return new MethodVisitor(Opcodes.ASM5,
					super.visitMethod(access, name, desc, signature, exceptions)) {
				
				@Override
				public void visitCode() {
					System.out.println("Transforming BiomeProvider getBiome(BlockPos,Biome)");
					super.visitFieldInsn(Opcodes.GETSTATIC, "cwgfarplaneview/core/Lock", "instance",
							"Lcwgfarplaneview/core/Lock;");
					super.visitInsn(Opcodes.DUP);
					super.visitVarInsn(Opcodes.ASTORE, 3);
					super.visitInsn(Opcodes.MONITORENTER);
					super.visitCode();
				}

				@Override
				public void visitInsn(int opcode) {
					if (opcode == Opcodes.ARETURN) {
						super.visitVarInsn(Opcodes.ASTORE, 4);
						super.visitVarInsn(Opcodes.ALOAD, 3);
						super.visitInsn(Opcodes.MONITOREXIT);
						super.visitVarInsn(Opcodes.ALOAD, 4);
						super.visitInsn(Opcodes.ARETURN);
					}else {
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
