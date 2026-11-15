import lab.sf7b.core.Catalog;
import lab.sf7b.core.Effect;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Own-algorithm coordinate snapshots for checking a performance change, not original-app parity. */
public class FieldSnapshot {
    public static void main(String[] args) throws Exception {
        Effect base=Catalog.INSTANCE.newEffect("multi_distort");
        Class<?> type=Class.forName("lab.sf7b.core.DistortionField");
        Constructor<?> constructor=type.getDeclaredConstructor(Effect.class,float.class);constructor.setAccessible(true);
        Method map=type.getDeclaredMethod("map",float.class,float.class);map.setAccessible(true);
        List<Map<String,Float>> cases=List.of(Map.of(),Map.of("variability",0f),Map.of("variability",1f),Map.of("layerCount",8f),Map.of("size",.035f,"angle",137f),Map.of("intensity",1.2f,"cx",.1f,"cy",.9f));
        StringBuilder csv=new StringBuilder("case,aspect,points,sha256\n");
        for(int i=0;i<cases.size();i++)for(float aspect:new float[]{.75f,1.3333334f}) {
            Effect effect=base.copy(base.getId(),cases.get(i),i%2==0?7L:823451234L,true,1f,base.getPalette(),base.getCurves(),base.getMask(),base.getAlgorithmVersion());
            Object field=constructor.newInstance(effect,aspect);MessageDigest digest=MessageDigest.getInstance("SHA-256");int points=0;
            for(int y=-2;y<=50;y++)for(int x=-2;x<=66;x++) {
                long value=(Long)map.invoke(field,(x+.37f)/64f,(y+.61f)/48f);
                digest.update(ByteBuffer.allocate(8).putLong(value).array());points++;
            }
            csv.append(i).append(',').append(aspect).append(',').append(points).append(',').append(HexFormat.of().formatHex(digest.digest())).append('\n');
        }
        Files.writeString(Path.of(args[0]),csv);System.out.println("Recorded "+cases.size()*2+" fields in "+args[0]);
    }
}
