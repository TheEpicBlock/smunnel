package nl.theepicblock.smunnel.rendering;

import net.minecraft.SharedConstants;
import nl.theepicblock.smunnel.Smunnel;
import nl.theepicblock.smunnel.SmunnelClient;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ShaderPatcher {
	private static final Pattern SODIUM_POSITION_TRANSFORM = Pattern.compile(Pattern.quote("vec3 position = u_RegionOffset + _draw_translation + _vert_position;\n"));
	private static final Pattern IRIS_POSITION_DEFINE = Pattern.compile("^(#define gl_Vertex )(.+)$", Pattern.MULTILINE);
	private static final Pattern VERSION = Pattern.compile("^#version .+$", Pattern.MULTILINE);

	public static String patchSodium(String source) {
		source = replaceFirst(
				source,
				SODIUM_POSITION_TRANSFORM,
				matchResult -> matchResult.group() + "smunnelCompressVertex(position); // Added by smunnel\n");
		return importSpaceCompression(source, false);
	}

	public static String patchIris(String source) {
		source = replaceFirst(
				source,
				IRIS_POSITION_DEFINE,
				matchResult -> "// Modified by smunnel\n" +
						matchResult.group(1) +
						"smunnelCompressVertex("+matchResult.group(2)+")"
		);
		return importSpaceCompression(source, true);
	}

	public static String importSpaceCompression(String source, boolean iris) {
		return replaceFirst(
				source,
				VERSION,
				matchResult -> matchResult.group() +
						"\n\n// Start of code added by smunnel\n" +
						(iris ? "#define SMUNNEL_IRIS_COMPAT\n" : "") +
						SmunnelClient.getShaderSrc("include/space_compression.vsh") +
						"\n// End of code added by smunnel"
		);
	}

	public static String replaceFirst(String input, Pattern pattern, Function<MatchResult, String> replacer) {
		AtomicInteger replacements = new AtomicInteger();
		var result = pattern.matcher(input).replaceFirst(matchResult -> {
			if (SharedConstants.isDevelopment) {
				Smunnel.LOGGER.info("Successful replace between "+matchResult.start()+":"+matchResult.end()+" in "+input.hashCode());
				replacements.getAndIncrement();
			}
			return replacer.apply(matchResult);
		});

		if (SharedConstants.isDevelopment && replacements.get() != 1) {
			Smunnel.LOGGER.error("Did "+replacements.get()+" replacements in "+input.hashCode()+"!!!!!");
			Thread.dumpStack();
		}
		return result;
	}
}
