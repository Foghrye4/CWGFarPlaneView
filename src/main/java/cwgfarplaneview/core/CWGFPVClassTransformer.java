package cwgfarplaneview.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.IClassTransformer;

public class CWGFPVClassTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals("net.minecraft.world.biome.BiomeProvider")) {
			return transformBiomeProvider(basicClass);
		}
		else if(transformedName.equals("net.minecraft.world.gen.layer.GenLayer")) {
			return transformGenLayer(basicClass);
		}
		else if(transformedName.startsWith("net.minecraft.world.gen.layer.")) {
			System.out.println("Transforming GenLayerChild: " + transformedName);
			return transformGenLayerChilds(basicClass);
		}
		return basicClass;
	}

	private byte[] transformBiomeProvider(byte[] bytes) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new BiomeProviderClassVisitor(Opcodes.ASM5, cw);
		cr.accept(cv, 0);
		return cw.toByteArray();
	}

	private byte[] transformGenLayer(byte[] bytes) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new GenLayerClassVisitor(Opcodes.ASM5, cw);
		cr.accept(cv, 0);
		return cw.toByteArray();
	}
	
	private byte[] transformGenLayerChilds(byte[] bytes) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new GenLayerChildClassVisitor(Opcodes.ASM5, cw);
		cr.accept(cv, 0);
		return cw.toByteArray();
	}
}
