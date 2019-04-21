package cwgfarplaneview.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.IClassTransformer;

public class CWGFPVClassTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("net.minecraft.world.biome.BiomeProvider".equals(transformedName)) {
			return transformJM(basicClass);
		}
		return basicClass;
	}

	private byte[] transformJM(byte[] bytes) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new BiomeProviderClassVisitor(Opcodes.ASM5, cw);
		cr.accept(cv, 0);
		return cw.toByteArray();
	}

}
